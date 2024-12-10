package com.neatroots.newdog

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.neatroots.newdog.Model.User
import com.squareup.picasso.Picasso

class AccountActivity : AppCompatActivity() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    var back_acc_edit: ImageButton? = null
    private lateinit var profile_fragment_username: TextInputEditText

    private lateinit var email_acc: TextInputEditText
    private lateinit var password_acc: TextInputEditText
    private lateinit var profile_Image: ImageView
    private lateinit var save_btn_acc: Button
    var change_img_text_btn: TextView? = null

    val STORAGE_REQUEST = 200
    private var checker = ""
    private var myUrl = ""
    var imageUri: Uri? = null
    var storagePermission : ArrayList<String>? = null
    private var storageProfilePicRef: StorageReference? = null

    val GALLERY_PICK = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        init()
        //userInfo()



        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        if (firebaseUser != null) {
            userInfo()
        } else {
            Log.e("YourTag", "FirebaseUser is null")
        }

        back_acc_edit?.setOnClickListener {
            onBackPressed()
        }


        change_img_text_btn?.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }




        save_btn_acc.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
        userInfo()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data

            // ใช้ Picasso ในการแสดงรูปภาพที่เลือก
            if (selectedImage != null) {
                Picasso.get().load(selectedImage).into(profile_Image)
            }

            // เก็บ Uri ไว้ในตัวแปร imageUri หากต้องการใช้งานต่อ
            imageUri = selectedImage

            // กำหนดค่า checker เพื่อบอกว่าผู้ใช้ได้ทำการเลือกรูปภาพใหม่
            checker = "clicked"
        }
    }





    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(profile_fragment_username.text.toString()) ->
                Toast.makeText(this, "Please write username", Toast.LENGTH_LONG).show()
            email_acc.text.toString() == " " ->
                Toast.makeText(this, "Please write email", Toast.LENGTH_LONG).show()
            password_acc.text.toString() == "" ->
                Toast.makeText(this, "Please write password", Toast.LENGTH_LONG).show()

            else -> {
                val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")

                val userMap = HashMap<String, Any>()
                userMap["username"] = profile_fragment_username.text.toString().toLowerCase()
                userMap["email"] = email_acc.text.toString().toLowerCase()
                userMap["password"] = password_acc.text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Account Information has been updated successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this@AccountActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {
        Log.d("YourTag", "FirebaseUser in userInfo(): $firebaseUser")

        val userRef =
            FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val user = pO.getValue<User>(User::class.java)
                    Log.d("YourTag", "User Data: $user")
                    if (user != null) {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.user).into(profile_Image)
                        profile_fragment_username.setText(user.getUsername())
                        email_acc.setText(user.getEmail())
                        password_acc.setText(user.getPassword())
                    } else {
                        Log.e("YourTag", "User object is null")
                    }
                } else {
                    Log.e("YourTag", "DataSnapshot is null or does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }


    fun init() {
        back_acc_edit = findViewById(R.id.back_acc)
        profile_fragment_username = findViewById(R.id.username_acc_input)
        email_acc = findViewById(R.id.email_acc_input)
        password_acc = findViewById(R.id.password_acc_input)
        profile_Image = findViewById(R.id.profile_Image)
        save_btn_acc = findViewById(R.id.save_btn_acc)
        change_img_text_btn = findViewById(R.id.change_img_text_btn)
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            TextUtils.isEmpty(profile_fragment_username.text.toString()) ->
                Toast.makeText(this, "Please write username", Toast.LENGTH_LONG).show()
            email_acc.text.toString() == " " ->
                Toast.makeText(this, "Please write email", Toast.LENGTH_LONG).show()
            password_acc.text.toString() == "" ->
                Toast.makeText(this, "Please write password", Toast.LENGTH_LONG).show()
            imageUri == null ->
                Toast.makeText(this, "Please Select image", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile ... ")
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child("${firebaseUser.uid}.jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        myUrl = downloadUri.toString()

                        // เพิ่ม Log เพื่อตรวจสอบ URL
                        Log.d("YourTag", "Downloaded URL: $myUrl")

                        val userMap = HashMap<String, Any>()
                        userMap["username"] = profile_fragment_username.text.toString().toLowerCase()
                        userMap["email"] = email_acc.text.toString().toLowerCase()
                        userMap["password"] = password_acc.text.toString()
                        userMap["image"] = myUrl

                        // แก้ไขโครงสร้าง ref เพื่ออัปเดตเฉพาะผู้ใช้ปัจจุบัน
                        val ref = FirebaseDatabase.getInstance()
                            .reference.child("Users").child(firebaseUser.uid)

                        ref.updateChildren(userMap)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Account Information has been updated successfully", Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this@AccountActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to update account information", Toast.LENGTH_LONG
                                    ).show()
                                }
                                progressDialog.dismiss()
                            }
                    } else {
                        Log.e("YourTag", "Failed to download URL")
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

}



