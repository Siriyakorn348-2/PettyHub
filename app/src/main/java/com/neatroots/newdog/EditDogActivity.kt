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
import java.util.Date
import java.util.Locale

class EditDogActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var storageDogPicRef: StorageReference? = null
    private var myUrl: String = ""
    private var selectedBirthDate: Long = 0
    private lateinit var progressDialog: ProgressDialog

    private lateinit var saveButton: Button
    private lateinit var nameDog: TextView
    private lateinit var dogBirthDate: TextView
    private lateinit var imageDog: CircleImageView
    private lateinit var addImageDog: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var sexDog: AutoCompleteTextView
    private lateinit var breedDog: AutoCompleteTextView

    private lateinit var dogId: String

    companion object {
        const val GALLERY_PICK = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dog)

        initViews()
        storageDogPicRef = FirebaseStorage.getInstance().reference.child("Dogs Pictures")
        progressDialog = ProgressDialog(this).apply {
            setMessage("กำลังบันทึก...")
            setCancelable(false)
        }


        dogId = intent.getStringExtra("dogId") ?: ""
        nameDog.text = intent.getStringExtra("dogName")
        sexDog.setText(intent.getStringExtra("dogGrender"))
        breedDog.setText(intent.getStringExtra("dogBreed"))
        myUrl = intent.getStringExtra("dogImage") ?: ""
        selectedBirthDate = intent.getLongExtra("dogBirthDate", 0L)
        if (selectedBirthDate != 0L) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dogBirthDate.text = dateFormat.format(Date(selectedBirthDate))
        } else {
            dogBirthDate.hint = "กรุณาเลือกวันเกิด"
        }
        if (!myUrl.isEmpty()) {
            Picasso.get().load(myUrl).into(imageDog)
        }


        val sexOptions = arrayOf("male", "female")
        val sexAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexOptions)
        sexDog.setAdapter(sexAdapter)

        val breedOptions = arrayOf(
            "อลาสกัน มาลามิวท์", "คอลลี่", "ไซบีเรียน ฮัสกี้", "ชามอย", "อัลเซเชี่ยล",
            "ดัลเมเชี่ยน", "อเมริกัน พิทบูล เทอร์เรีย", "โกลเด้น รีทรีฟเวอร์", "อเมริกัน บลูด็อก",
            "บางแก้ว", "ชิบะ อินุ", "ชิวาวา", "ปอมเมอเรเนียน", "ปั๊ก", "คอร์กี้"
        )
        val breedAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, breedOptions)
        breedDog.setAdapter(breedAdapter)


        dogBirthDate.setOnClickListener {
            val calendar = if (selectedBirthDate != 0L) {
                Calendar.getInstance().apply { timeInMillis = selectedBirthDate }
            } else {
                Calendar.getInstance()
            }
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
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        addImageDog.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }

        saveButton.setOnClickListener {
            updateDog()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        saveButton = findViewById(R.id.save_edit)
        nameDog = findViewById(R.id.adddog_name)
        dogBirthDate = findViewById(R.id.dog_age)
        imageDog = findViewById(R.id.image_dog)
        addImageDog = findViewById(R.id.add_image_dag)
        backButton = findViewById(R.id.back_image_add)
        sexDog = findViewById(R.id.sexAutoComplete)
        breedDog = findViewById(R.id.breedAutoComplete)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            Log.d("EditDogActivity", "Image selected: $imageUri")
            Picasso.get().load(imageUri).into(imageDog)
        }
    }

    private fun updateDog() {
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
                saveButton.isEnabled = false
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
                        Log.d("EditDogActivity", "Image uploaded: $myUrl")
                        saveDogToDatabase()
                    }.addOnFailureListener {
                        progressDialog.dismiss()
                        saveButton.isEnabled = true
                        Toast.makeText(this, "ไม่สามารถรับ URL รูปภาพได้", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    saveButton.isEnabled = true
                    Toast.makeText(this, "อัปโหลดรูปภาพล้มเหลว", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
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
        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        val dogAge = calculateAge(selectedBirthDate)
        val dog = Dog(
            dogId = dogId,
            dogName = nameDog.text.toString().trim(),
            dogImage = myUrl,
            dogBreed = breedDog.text.toString().trim(),
            dogAge = dogAge,
            dogGender = sexDog.text.toString().trim(),
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        )

        val updates = hashMapOf<String, Any>(
            "dogId" to dogId,
            "dogName" to dog.getDogName(),
            "dogImage" to myUrl,
            "dogBreed" to dog.getDogBreed(),
            "dogAge" to dogAge,
            "dogGender" to dog.getDogGender(),
            "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
            "dogBirthDate" to selectedBirthDate
        )

        dogsRef.updateChildren(updates).addOnCompleteListener { task ->
            progressDialog.dismiss()
            saveButton.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(this, "แก้ไขข้อมูลสุนัขสำเร็จ", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "แก้ไขข้อมูลล้มเหลว: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}