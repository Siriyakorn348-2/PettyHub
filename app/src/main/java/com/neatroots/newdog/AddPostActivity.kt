package com.neatroots.newdog

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.canhub.cropper.CropImage



class AddPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    var addPost: TextView? = null
    var image_added : ImageView? = null
    var description: TextView? = null
    val GALLERY_PICK = 1
    var close : ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)


        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")
        description = findViewById(R.id.description)
        image_added = findViewById(R.id.image_added)
        addPost = findViewById(R.id.post)
        close = findViewById(R.id.close)


        close?.setOnClickListener{
            onBackPressed()
        }

        image_added?.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }

        addPost?.setOnClickListener {
            uploadImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            // รับ URI ของรูปภาพที่ผู้ใช้เลือก
            imageUri = data?.data

            image_added?.setImageURI(imageUri)
        }
    }


    private fun uploadImage(){
        when{
            imageUri == null ->
                Toast.makeText(this, "Please Select image", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description?.text.toString()) ->
                Toast.makeText(this, "Please write description", Toast.LENGTH_LONG).show()
            else->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are adding your picture post ... ")
                progressDialog.show()
                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString()+"jpg")

                val uploadTask : StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot , Task<Uri>> { task ->
                    if (task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl

                }).addOnCompleteListener (OnCompleteListener<Uri>{ task ->
                    if (task.isSuccessful)
                    {
                        val downloadUri = task.result
                        myUrl = downloadUri.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")

                        val postId = ref.push().key
                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postId!!
                        postMap["description"] = description?.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl
                        postMap["publisherName"] = "" // ตั้งค่าเป็นค่าว่าง เนื่องจากจะดึงข้อมูลในภายหลัง
                        postMap["publisherImage"] = "" // ตั้งค่าเป็นค่าว่าง เนื่องจากจะดึงข้อมูลในภายหลัง


                        ref.child(postId).updateChildren(postMap)
                        Toast.makeText(
                            this,
                            "Account Information has been updated successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@AddPostActivity,
                            "Failed to add post. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                })

            }
        }
    }
}
