package com.neatroots.newdog

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditPostActivity : AppCompatActivity() {
    private val imageUris = mutableListOf<Uri>()
    private var storagePostPicRef: StorageReference? = null
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageAdapter: PostImageAdapter
    private lateinit var savePost: TextView
    private lateinit var description: EditText
    private lateinit var close: ImageView
    private lateinit var addImageButton: ImageView
    private lateinit var postId: String
    private val GALLERY_PICK = 1
    private var currentUserUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)


        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")
        description = findViewById(R.id.description)
        imageRecyclerView = findViewById(R.id.image_added_recycler)
        savePost = findViewById(R.id.post)
        close = findViewById(R.id.close)
        addImageButton = findViewById(R.id.add_image_button)


        postId = intent.getStringExtra("postId") ?: ""
        description.setText(intent.getStringExtra("description"))
        val postImages = intent.getStringArrayListExtra("postImages") ?: arrayListOf()
        imageUris.addAll(postImages.map { Uri.parse(it) })

        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = PostImageAdapter(imageUris) { position ->
            imageUris.removeAt(position)
            imageAdapter.notifyDataSetChanged()
            if (imageUris.isEmpty()) imageRecyclerView.visibility = View.GONE
        }
        imageRecyclerView.adapter = imageAdapter
        imageRecyclerView.visibility = if (imageUris.isEmpty()) View.GONE else View.VISIBLE

        close.setOnClickListener {
            onBackPressed()
        }

        addImageButton.setOnClickListener {
            if (imageUris.size < 10) {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryIntent.type = "image/*"
                startActivityForResult(galleryIntent, GALLERY_PICK)
            } else {
                Toast.makeText(this, "สามารถเพิ่มได้สูงสุด 10 รูป", Toast.LENGTH_SHORT).show()
            }
        }

        savePost.setOnClickListener {
            updatePost()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null && imageUris.size < 10) {
                imageUris.add(uri)
                imageAdapter.notifyDataSetChanged()
                imageRecyclerView.visibility = View.VISIBLE
            } else if (imageUris.size >= 10) {
                Toast.makeText(this, "ถึงขีดจำกัด 10 รูปแล้ว", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePost() {
        if (TextUtils.isEmpty(description.text.toString()) && imageUris.isEmpty()) {
            Toast.makeText(this, "กรุณาเขียนโพสต์หรือเลือกภาพอย่างน้อยหนึ่งอย่าง", Toast.LENGTH_LONG).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("กำลังแก้ไขโพสต์")
        progressDialog.setMessage("กรุณารอสักครู่ เรากำลังตรวจสอบและอัปเดตโพสต์ของคุณ...")
        progressDialog.show()


        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val publisherId = snapshot.child("publisher").getValue(String::class.java)
                    if (publisherId == currentUserUid) {
                        updatePostData(postRef, progressDialog)
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this@EditPostActivity, "คุณไม่มีสิทธิ์แก้ไขโพสต์นี้", Toast.LENGTH_LONG).show()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this@EditPostActivity, "ไม่พบโพสต์นี้", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EditPostActivity, "เกิดข้อผิดพลาด: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updatePostData(ref: com.google.firebase.database.DatabaseReference, progressDialog: ProgressDialog) {
        val postMap = HashMap<String, Any>()
        postMap["description"] = description.text.toString()

        if (imageUris.isNotEmpty()) {
            val imageUrls = mutableListOf<String>()
            var uploadCount = 0
            val newImageUris = imageUris.filter { it.scheme != "https" && it.scheme != "http" }


            imageUrls.addAll(imageUris.filter { it.scheme == "https" || it.scheme == "http" }.map { it.toString() })

            if (newImageUris.isNotEmpty()) {
                for (uri in newImageUris) {
                    val fileRef = storagePostPicRef!!.child("${System.currentTimeMillis()}_${imageUris.indexOf(uri)}.jpg")
                    val uploadTask = fileRef.putFile(uri)

                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        fileRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            imageUrls.add(task.result.toString())
                            uploadCount++
                            if (uploadCount == newImageUris.size) {
                                postMap["postImages"] = imageUrls
                                saveUpdatedPost(ref, postMap, progressDialog)
                            }
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this, "ไม่สามารถอัปโหลดรูปภาพบางรูปได้", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                postMap["postImages"] = imageUrls
                saveUpdatedPost(ref, postMap, progressDialog)
            }
        } else {
            postMap["postImages"] = emptyList<String>()
            saveUpdatedPost(ref, postMap, progressDialog)
        }
    }

    private fun saveUpdatedPost(ref: com.google.firebase.database.DatabaseReference, postMap: HashMap<String, Any>, progressDialog: ProgressDialog) {
        ref.updateChildren(postMap)
            .addOnCompleteListener { dbTask ->
                progressDialog.dismiss()
                if (dbTask.isSuccessful) {
                    Toast.makeText(this, "แก้ไขโพสต์สำเร็จ", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "ไม่สามารถแก้ไขโพสต์ได้: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}