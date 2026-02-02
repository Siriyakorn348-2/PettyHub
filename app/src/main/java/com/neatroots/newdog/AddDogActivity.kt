package com.neatroots.newdog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDogActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var storageDogPicRef: StorageReference? = null
    private var myUrl: String = ""
    private lateinit var progressDialog: ProgressDialog
    private var selectedBirthDate: Long = 0

    private lateinit var addDogSave: Button
    private lateinit var nameDog: TextView
    private lateinit var dogBirthDate: TextView
    private lateinit var imageDog: CircleImageView
    private lateinit var addImageDog: ImageButton
    private lateinit var backImgAddDog: ImageButton
    private lateinit var sexDog: AutoCompleteTextView
    private lateinit var breedDog: AutoCompleteTextView

    companion object {
        const val GALLERY_PICK = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dog)

        initViews()
        storageDogPicRef = FirebaseStorage.getInstance().reference.child("Dogs Pictures")
        progressDialog = ProgressDialog(this).apply {
            setMessage("กำลังบันทึก...")
            setCancelable(false)
        }

        // Setup gender dropdown
        val sexOptions = arrayOf("เพศผู้", "เพศเมีย")
        val sexAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexOptions)
        sexDog.setAdapter(sexAdapter)

        // Setup breed dropdown
        val breedOptions = arrayOf(
            "อลาสกัน มาลามิวท์", "คอลลี่", "ไซบีเรียน ฮัสกี้", "ชามอย", "อัลเซเชี่ยล",
            "ดัลเมเชี่ยน", "อเมริกัน พิทบูล เทอร์เรีย", "โกลเด้น รีทรีฟเวอร์", "อเมริกัน บลูด็อก",
            "บางแก้ว", "ชิบะ อินุ", "ชิวาวา", "ปอมเมอเรเนียน", "ปั๊ก", "คอร์กี้"
        )
        val breedAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, breedOptions)
        breedDog.setAdapter(breedAdapter)

        // Setup DatePicker for birth date
        dogBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    selectedBirthDate = selectedCalendar.timeInMillis
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dogBirthDate.text = dateFormat.format(selectedCalendar.time)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // จำกัดไม่ให้เลือกวันในอนาคต
            datePickerDialog.show()
        }

        // Button listeners
        addImageDog.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }

        addDogSave.setOnClickListener {
            addDog()
        }

        backImgAddDog.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        addDogSave = findViewById(R.id.save_add)
        nameDog = findViewById(R.id.adddog_name)
        dogBirthDate = findViewById(R.id.dog_age)
        imageDog = findViewById(R.id.image_dog)
        addImageDog = findViewById(R.id.add_image_dag)
        backImgAddDog = findViewById(R.id.back_image_add)
        sexDog = findViewById(R.id.sexAutoComplete)
        breedDog = findViewById(R.id.breedAutoComplete)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            Picasso.get().load(imageUri).into(imageDog)
        }
    }

    private fun addDog() {
        val dogNameText = nameDog.text.toString().trim()
        val breedText = breedDog.text.toString().trim()
        val birthDateText = dogBirthDate.text.toString().trim()
        val genderText = sexDog.text.toString().trim()

        when {
            TextUtils.isEmpty(dogNameText) -> Toast.makeText(this, "กรุณาใส่ชื่อสุนัข", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(breedText) -> Toast.makeText(this, "กรุณาเลือกสายพันธุ์", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(birthDateText) -> Toast.makeText(this, "กรุณาเลือกวันเกิด", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(genderText) -> Toast.makeText(this, "กรุณาเลือกเพศ", Toast.LENGTH_SHORT).show()
            else -> {
                progressDialog.show()
                addDogSave.isEnabled = false
                uploadImageToFirebaseStorage()
            }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            val filePath = storageDogPicRef?.child("${System.currentTimeMillis()}.jpg")
            filePath?.putFile(imageUri!!)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        myUrl = uri.toString()
                        saveDogToDatabase()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        addDogSave.isEnabled = true
                        Toast.makeText(this, "ไม่สามารถรับ URL รูปภาพได้", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    addDogSave.isEnabled = true
                    Toast.makeText(this, "อัปโหลดรูปภาพล้มเหลว", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            myUrl = ""
            saveDogToDatabase()
        }
    }

    private fun calculateAge(birthDate: Long): String {
        if (birthDate == 0L) return "ไม่ระบุ"

        val currentTime = Calendar.getInstance()
        val birthTime = Calendar.getInstance().apply { timeInMillis = birthDate }

        var years = currentTime.get(Calendar.YEAR) - birthTime.get(Calendar.YEAR)
        var months = currentTime.get(Calendar.MONTH) - birthTime.get(Calendar.MONTH)
        val daysInMonth = currentTime.getActualMaximum(Calendar.DAY_OF_MONTH)
        var days = currentTime.get(Calendar.DAY_OF_MONTH) - birthTime.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            days += daysInMonth
        }
        if (months < 0) {
            years--
            months += 12
        }

        return when {
            years > 0 -> "$years ปี $months เดือน"
            months > 0 -> "$months เดือน $days วัน"
            else -> "$days วัน"
        }
    }

    private fun saveDogToDatabase() {
        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs")
        val dogId = dogsRef.push().key ?: return

        val dogAge = calculateAge(selectedBirthDate) // คำนวณอายุ
        val dog = Dog(
            dogId = dogId,
            dogName = nameDog.text.toString().trim(),
            dogImage = myUrl,
            dogBreed = breedDog.text.toString().trim(),
            dogAge = dogAge,
            dogGender = sexDog.text.toString().trim(),
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )

        // บันทึกข้อมูลทั้งหมดรวมถึง dogBirthDate ในคราวเดียว
        val dogData = hashMapOf<String, Any>(
            "dogId" to dogId,
            "dogName" to dog.getDogName(),
            "dogImage" to myUrl,
            "dogBreed" to dog.getDogBreed(),
            "dogAge" to dogAge,
            "dogGender" to dog.getDogGender(),
            "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
            "dogBirthDate" to selectedBirthDate
        )

        Log.d("AddDogActivity", "Saving dog to Firebase: ID=${dog.getDogId()}, Name=${dog.getDogName()}, Age=$dogAge, BirthDate=$selectedBirthDate")

        dogsRef.child(dogId).setValue(dogData).addOnCompleteListener { task ->
            progressDialog.dismiss()
            addDogSave.isEnabled = true
            if (task.isSuccessful) {
                Log.d("AddDogActivity", "Dog added successfully with ID: $dogId")
                Toast.makeText(this, "เพิ่มสุนัขสำเร็จ", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.e("AddDogActivity", "Failed to add dog: ${task.exception?.message}")
                Toast.makeText(this, "เพิ่มสุนัขล้มเหลว: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}