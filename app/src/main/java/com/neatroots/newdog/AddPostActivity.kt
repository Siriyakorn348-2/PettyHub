package com.neatroots.newdog

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddPostActivity : AppCompatActivity() {
    private val imageUris = mutableListOf<Uri>()
    private var storagePostPicRef: StorageReference? = null
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageAdapter: PostImageAdapter

    private lateinit var addPost: TextView
    private lateinit var description: EditText
    private val GALLERY_PICK = 1
    private lateinit var close: ImageView
    private lateinit var addImageButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")
        description = findViewById(R.id.description)
        imageRecyclerView = findViewById(R.id.image_added_recycler)
        addPost = findViewById(R.id.post)
        close = findViewById(R.id.close)
        addImageButton = findViewById(R.id.add_image_button)

        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = PostImageAdapter(imageUris) { position ->
            imageUris.removeAt(position)
            imageAdapter.notifyDataSetChanged()
            if (imageUris.isEmpty()) imageRecyclerView.visibility = View.GONE
        }
        imageRecyclerView.adapter = imageAdapter

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

        addPost.setOnClickListener {
            uploadImages()
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

    private fun uploadImages() {
        if (TextUtils.isEmpty(description.text.toString()) && imageUris.isEmpty()) {
            Toast.makeText(this, "กรุณาเขียนโพสต์หรือเลือกภาพอย่างน้อยหนึ่งอย่าง", Toast.LENGTH_LONG).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("กำลังเพิ่มโพสต์ใหม่")
        progressDialog.setMessage("กรุณารอสักครู่ เรากำลังเพิ่มโพสต์ของคุณ...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
        val postId = ref.push().key ?: return
        val postMap = HashMap<String, Any>()
        postMap["postid"] = postId
        postMap["description"] = description.text.toString()
        postMap["publisher"] = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        postMap["publisherName"] = ""
        postMap["publisherImage"] = ""
        postMap["dateTime"] = ServerValue.TIMESTAMP

        if (imageUris.isNotEmpty()) {
            val imageUrls = mutableListOf<String>()
            var uploadCount = 0

            for (uri in imageUris) {
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
                        if (uploadCount == imageUris.size) {
                            postMap["postImages"] = imageUrls
                            savePost(ref, postId, postMap, progressDialog)
                        }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "ไม่สามารถอัปโหลดรูปภาพบางรูปได้", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            postMap["postImages"] = emptyList<String>()
            savePost(ref, postId, postMap, progressDialog)
        }
    }

    private fun savePost(ref: DatabaseReference, postId: String, postMap: HashMap<String, Any>, progressDialog: ProgressDialog) {
        ref.child(postId).setValue(postMap)
            .addOnCompleteListener { dbTask ->
                progressDialog.dismiss()
                if (dbTask.isSuccessful) {
                    Toast.makeText(this, "โพสต์สำเร็จแล้ว", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("navigateTo", "HomeFragment")
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "ไม่สามารถโพสต์ได้: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}

class PostImageAdapter(
    private val imageUris: MutableList<Uri>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PostImageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.post_image_item)
        val removeButton: ImageView = itemView.findViewById(R.id.remove_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageURI(imageUris[position])
        holder.removeButton.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = imageUris.size
}