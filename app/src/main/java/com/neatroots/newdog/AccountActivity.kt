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
import com.google.firebase.storage.UploadTask
import com.neatroots.newdog.Model.User
import com.squareup.picasso.Picasso

class AccountActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var storageProfilePicRef: StorageReference

    private lateinit var backAccEdit: ImageButton
    private lateinit var profileFragmentUsername: TextInputEditText
    private lateinit var profileImage: ImageView
    private lateinit var saveBtnAcc: Button
    private lateinit var changeImgTextBtn: TextView

    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null

    private val GALLERY_PICK = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        init()

        firebaseUser = FirebaseAuth.getInstance().currentUser ?: run {
            Log.e("AccountActivity", "FirebaseUser is null")
            finish()
            return
        }

        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        userInfo()

        backAccEdit.setOnClickListener {
            onBackPressed()
        }

        changeImgTextBtn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }

        saveBtnAcc.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let {
                Picasso.get().load(it).into(profileImage)
                checker = "clicked"
            }
        }
    }

    private fun init() {
        backAccEdit = findViewById(R.id.back_acc)
        profileFragmentUsername = findViewById(R.id.username_acc_input)
        profileImage = findViewById(R.id.profile_Image)
        saveBtnAcc = findViewById(R.id.save_btn_acc)
        changeImgTextBtn = findViewById(R.id.change_img_text_btn)
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        Picasso.get().load(it.getImage()).placeholder(R.drawable.user).into(profileImage)
                        profileFragmentUsername.setText(it.getUsername())
                    } ?: Log.e("AccountActivity", "User object is null")
                } else {
                    Log.e("AccountActivity", "DataSnapshot does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AccountActivity", "Database error: ${error.message}")
                Toast.makeText(this@AccountActivity, "เกิดข้อผิดพลาดในการโหลดข้อมูล", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserInfoOnly() {
        if (TextUtils.isEmpty(profileFragmentUsername.text.toString())) {
            Toast.makeText(this, "กรุณากรอกชื่อผู้ใช้", Toast.LENGTH_LONG).show()
        } else {
            // แสดง ProgressDialog
            val progressDialog = ProgressDialog(this).apply {
                setTitle("การตั้งค่าบัญชี")
                setMessage("กรุณารอสักครู่ กำลังอัปเดตชื่อผู้ใช้...")
                setCanceledOnTouchOutside(false) // ป้องกันการแตะนอกเพื่อยกเลิก
                show()
            }

            val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
            val userMap = HashMap<String, Any>().apply {
                put("username", profileFragmentUsername.text.toString().toLowerCase())
            }

            usersRef.updateChildren(userMap).addOnCompleteListener { task ->
                progressDialog.dismiss() // ปิด ProgressDialog เมื่อเสร็จสิ้น
                if (task.isSuccessful) {
                    Toast.makeText(this, "อัปเดตชื่อผู้ใช้สำเร็จ", Toast.LENGTH_LONG).show()

                    // ส่ง Intent โดยระบุให้ไปที่ ProfileFragment
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("selectedNavItemId", R.id.nav_profile) // ระบุ ID ของ Bottom Navigation
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "อัปเดตชื่อผู้ใช้ไม่สำเร็จ", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadImageAndUpdateInfo() {
        if (TextUtils.isEmpty(profileFragmentUsername.text.toString())) {
            Toast.makeText(this, "กรุณากรอกชื่อผู้ใช้", Toast.LENGTH_LONG).show()
        } else if (imageUri == null) {
            Toast.makeText(this, "กรุณาเลือกรูปภาพ", Toast.LENGTH_LONG).show()
        } else {
            // แสดง ProgressDialog
            val progressDialog = ProgressDialog(this).apply {
                setTitle("การตั้งค่าบัญชี")
                setMessage("กรุณารอสักครู่ กำลังอัปเดตโปรไฟล์ของคุณ...")
                setCanceledOnTouchOutside(false)
                show()
            }

            val fileRef = storageProfilePicRef.child("${firebaseUser.uid}.jpg")
            fileRef.putFile(imageUri!!).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    myUrl = task.result.toString()
                    Log.d("AccountActivity", "Download URL: $myUrl")

                    val userMap = HashMap<String, Any>().apply {
                        put("username", profileFragmentUsername.text.toString().toLowerCase())
                        put("image", myUrl)
                    }

                    FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
                        .updateChildren(userMap)
                        .addOnCompleteListener { updateTask ->
                            progressDialog.dismiss() // ปิด ProgressDialog เมื่อเสร็จสิ้น
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "อัปเดตโปรไฟล์สำเร็จ", Toast.LENGTH_LONG).show()
                                // ส่ง Intent โดยระบุให้ไปที่ ProfileFragment
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("selectedNavItemId", R.id.nav_profile)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "อัปเดตโปรไฟล์ไม่สำเร็จ", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    Log.e("AccountActivity", "Failed to get download URL")
                    Toast.makeText(this, "อัปโหลดรูปภาพไม่สำเร็จ", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}