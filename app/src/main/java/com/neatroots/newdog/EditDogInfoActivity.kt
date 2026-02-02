package com.neatroots.newdog

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.neatroots.newdog.databinding.ActivityEditDogInfoBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class EditDogInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDogInfoBinding
    private lateinit var dogId: String
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var imageUri: Uri? = null
    private var currentImageUrl: String? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDogInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dogId = intent.getStringExtra("dogId") ?: ""
        if (dogId.isEmpty()) {
            finish()
            return
        }

        setupGenderSpinner()
        setupBreedSpinner()
        setupGroupSpinner()
        setupDatePicker()

        binding.backIcon.setOnClickListener {
            finish()
        }

        binding.changeImageButton.setOnClickListener {
            pickImageFromGallery()
        }



        fetchDogInfo()

        binding.saveButton.setOnClickListener {
            saveDogInfo()
        }


        setSupportActionBar(binding.toolbar)
        binding.backIcon.setOnClickListener { finish() }
    }

    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("เพศผู้", "เพศเมีย")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editDogGender.adapter = adapter
        binding.editDogGender.setSelection(0)
    }

    private fun setupBreedSpinner() {
        val breedOptions = arrayOf(
            "อลาสกัน มาลามิวท์", "คอลลี่", "ไซบีเรียน ฮัสกี้", "ชามอย", "อัลเซเชี่ยล",
            "ดัลเมเชี่ยน", "อเมริกัน พิทบูล เทอร์เรีย", "โกลเด้น รีทรีฟเวอร์", "อเมริกัน บลูด็อก",
            "บางแก้ว", "ชิบะ อินุ", "ชิวาวา", "ปอมเมอเรเนียน", "ปั๊ก", "คอร์กี้"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, breedOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editDogBreed.adapter = adapter
        binding.editDogBreed.setSelection(0)
    }

    private fun setupGroupSpinner() {
        val groupOptions = arrayOf("ไม่ระบุ", "กรุ๊ปเลือด A", "กรุ๊ปเลือด B", "กรุ๊ปเลือด AB", "กรุ๊ปเลือด O")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editDogGroup.adapter = adapter
        binding.editDogGroup.setSelection(0)
    }

    private fun setupDatePicker() {
        binding.editDogBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                binding.editDogBirthDate.setText(dateFormat.format(selectedDate.time))
            }, year, month, day).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.editDogImage.setImageURI(imageUri)
        }
    }

    private fun fetchDogInfo() {
        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        dogsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.editDogName.setText(snapshot.child("dogName").getValue(String::class.java) ?: "")

                    val breed = snapshot.child("dogBreed").getValue(String::class.java) ?: ""
                    val breedAdapter = binding.editDogBreed.adapter as ArrayAdapter<String>
                    val breedPosition = breedAdapter.getPosition(breed)
                    binding.editDogBreed.setSelection(if (breedPosition >= 0) breedPosition else 0)

                    binding.editDogColor.setText(snapshot.child("dogColor").getValue(String::class.java) ?: "")
                    binding.editDogMarkings.setText(snapshot.child("dogMarkings").getValue(String::class.java) ?: "")

                    val gender = snapshot.child("dogGender").getValue(String::class.java) ?: "เพศผู้"
                    binding.editDogGender.setSelection(
                        when (gender) {
                            "เพศผู้" -> 0
                            "เพศเมีย" -> 1
                            else -> 0
                        }
                    )

                    val birthDate = snapshot.child("dogBirthDate").getValue(Long::class.java) ?: 0L
                    if (birthDate != 0L) {
                        binding.editDogBirthDate.setText(dateFormat.format(Date(birthDate)))
                    }

                    binding.editDogWeight.setText(snapshot.child("dogWeight").getValue(Double::class.java)?.toString() ?: "")

                    val group = snapshot.child("dogGroup").getValue(String::class.java) ?: "ไม่ระบุ"
                    val groupAdapter = binding.editDogGroup.adapter as ArrayAdapter<String>
                    val groupPosition = groupAdapter.getPosition(group)
                    binding.editDogGroup.setSelection(if (groupPosition >= 0) groupPosition else 0)

                    binding.editDogNeutered.isChecked = snapshot.child("dogNeutered").getValue(Boolean::class.java) ?: false

                    currentImageUrl = snapshot.child("dogImage").getValue(String::class.java)
                    if (!currentImageUrl.isNullOrEmpty()) {
                        Picasso.get().load(currentImageUrl).into(binding.editDogImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditDogInfoActivity, "ดึงข้อมูลล้มเหลว: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveDogInfo() {
        val dogName = binding.editDogName.text.toString().trim()
        if (dogName.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกชื่อสุนัข", Toast.LENGTH_SHORT).show()
            return
        }

        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        val updatedData = mutableMapOf<String, Any>()

        updatedData["dogName"] = dogName
        updatedData["dogBreed"] = binding.editDogBreed.selectedItem.toString()
        updatedData["dogColor"] = binding.editDogColor.text.toString().trim()
        updatedData["dogMarkings"] = binding.editDogMarkings.text.toString().trim()
        updatedData["dogGender"] = binding.editDogGender.selectedItem.toString()
        val birthDateText = binding.editDogBirthDate.text.toString().trim()
        updatedData["dogBirthDate"] = if (birthDateText.isNotEmpty()) dateFormat.parse(birthDateText)?.time ?: 0L else 0L
        updatedData["dogWeight"] = binding.editDogWeight.text.toString().trim().toDoubleOrNull() ?: 0.0
        updatedData["dogGroup"] = binding.editDogGroup.selectedItem.toString()
        updatedData["dogNeutered"] = binding.editDogNeutered.isChecked

        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("dog_images/$dogId.jpg")
            storageRef.putFile(imageUri!!).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updatedData["dogImage"] = uri.toString()
                    saveToDatabase(dogsRef, updatedData)
                }.addOnFailureListener {
                    Toast.makeText(this, "อัปโหลดรูปภาพล้มเหลว", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "อัปโหลดรูปภาพล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        } else {

            if (!currentImageUrl.isNullOrEmpty()) {
                updatedData["dogImage"] = currentImageUrl!!
            }
            saveToDatabase(dogsRef, updatedData)
        }
    }

    private fun saveToDatabase(dogsRef: com.google.firebase.database.DatabaseReference, updatedData: Map<String, Any>) {
        dogsRef.updateChildren(updatedData).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("EditDogInfo", "บันทึกสำเร็จ: $updatedData")
                Toast.makeText(this, "บันทึกสำเร็จ", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Log.e("EditDogInfo", "บันทึกข้อมูลล้มเหลว: ${it.exception?.message}")
                Toast.makeText(this, "บันทึกข้อมูลล้มเหลว: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}