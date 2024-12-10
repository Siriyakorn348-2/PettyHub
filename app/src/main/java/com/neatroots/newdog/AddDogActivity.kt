package com.neatroots.newdog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.neatroots.newdog.Fragments.ProfileFragment
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddDogActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageDogPicRef: StorageReference? = null

    var addDog_save: Button? = null
    var nameDog: TextView? = null
    var dog_age: TextView? = null
    var dog_grender: TextView? = null
    var image_dog: CircleImageView? = null
    var add_image_dog: ImageButton? = null
    var delete_dog: Button? = null
    val GALLERY_PICK = 1

    lateinit var sex_Dog: AutoCompleteTextView
    lateinit var breed_Dog: AutoCompleteTextView
    var back_img_addDog: ImageButton? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dog)
        init()

        //เพศ
        val sex_dog = arrayOf(
            "male ",
            "female "
        )

        // กำหนดค่า AutoCompleteTextView
        sex_Dog = findViewById(R.id.sexAutoComplete)

        val adapter_sex = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sex_dog)
        sex_Dog.setAdapter(adapter_sex)

        // รายการสายพันธุ์ของสุนัข
        val breedDog = arrayOf(
            "อลาสกัน มาลามิวท์ ",
            "คอลลี่ ",
            "ไซบีเรียน ฮัสกี้ ",
            "ชามอย ",
            "อัลเซเชี่ยล",
            "ดัลเมเชี่ยน ",
            "อเมริกัน พิทบูล เทอร์เรีย ",
            "โกลเด้น รีทรีฟเวอร์ ",
            "อเมริกัน บลูด็อก ",
            "บางแก้ว ",
            "ชิบะ อินุ ",
            "ชิวาวา ",
            "ปอมเมอเรเนียน ",
            "ปั๊ก ",
            "คอร์กี้ "
        )
        storageDogPicRef = FirebaseStorage.getInstance().reference.child("Dogs Pictures")

        addDog_save?.setOnClickListener {
            addDog()
        }

        // กำหนดค่า AutoCompleteTextView
        breed_Dog = findViewById(R.id.breedAutoComplete)

        // สร้าง ArrayAdapter โดยใช้รายการสายพันธุ์ของสุนัขและเลเอาท์ AutoCompleteTextView ตามค่าเริ่มต้น
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, breedDog)

        // กำหนด Adapter ให้กับ AutoCompleteTextView
        breed_Dog.setAdapter(adapter)

        add_image_dog?.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }

        back_img_addDog?.setOnClickListener { onBackPressed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data

            if (selectedImage != null) {
                // ใช้ Picasso ในการแสดงรูปภาพที่เลือก
                Picasso.get().load(selectedImage).into(image_dog)
            }

            imageUri = selectedImage
            // ไม่ต้อง upload ทันที ให้ upload เมื่อกดบันทึก
        }
    }

    private fun addDog() {
        when {
            TextUtils.isEmpty(nameDog?.text.toString()) ->
                Toast.makeText(this, "Please write dog name", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(breed_Dog?.text.toString()) ->
                Toast.makeText(this, "Please write breed Dog", Toast.LENGTH_LONG).show()
            else -> {
                // ย้าย uploadImageToFirebaseStorage() ไปในส่วนนี้
                uploadImageToFirebaseStorage()
            }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            val filePath = storageDogPicRef?.child(System.currentTimeMillis().toString() + ".jpg")

            filePath?.putFile(imageUri!!)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        filePath.downloadUrl.addOnSuccessListener { uri ->
                            myUrl = uri.toString()
                            saveDogToDatabase()
                        }
                    } else {
                        Toast.makeText(
                            this@AddDogActivity,
                            "Failed to upload image.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // ถ้าไม่มีการเลือกรูปภาพ
            saveDogToDatabase()
        }
    }

    private fun saveDogToDatabase() {

        val dogsRef = FirebaseDatabase.getInstance().reference
            .child("Dogs")

        val dogId = dogsRef.push().key
        val dogMap = HashMap<String, Any>()
        dogMap["postid"] = dogId.toString()
        dogMap["dogname"] = nameDog?.text.toString().toLowerCase()
        dogMap["breed"] = breed_Dog?.text.toString()
        dogMap["age"] = dog_age?.text.toString()
        dogMap["grender"] = sex_Dog?.text.toString()
        dogMap["dogimg"] = myUrl
        dogMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid

        if (dogId != null) {
            dogsRef.child(dogId).setValue(dogMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this, "Dog added successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AddDogActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to add dog. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    fun init() {
        back_img_addDog = findViewById(R.id.back_image_add)

        addDog_save = findViewById(R.id.save_add)
        delete_dog = findViewById(R.id.delete_dog)

        image_dog = findViewById(R.id.image_dog)
        add_image_dog = findViewById(R.id.add_image_dag)
        nameDog = findViewById(R.id.adddog_name)
        dog_age = findViewById(R.id.dog_age)
        // dog_grender = findViewById(R.id.dog_grender)
    }

    companion object {
        const val GALLERY_PICK = 1
    }
}

