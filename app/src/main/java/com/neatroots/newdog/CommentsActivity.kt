package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.CommentAdapter
import com.neatroots.newdog.Adapter.PostImagesAdapter
import com.neatroots.newdog.Model.Comment
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.Fragments.ProfileFragment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentAdapter? = null
    private var commentList: MutableList<Comment>? = null
    private var postImagesAdapter: PostImagesAdapter? = null

    private var addCommentEditText: EditText? = null
    private var postImagesRecyclerView: RecyclerView? = null
    private var postTextView: TextView? = null
    private var profileImageView: CircleImageView? = null
    private var userNameTextView: TextView? = null
    private var postTimeTextView: TextView? = null
    private var postCommentButton: TextView? = null
    private var backButton: ImageButton? = null
    private var imageIndicator: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        init()

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val recyclerView: RecyclerView = findViewById(R.id.comments_recycler)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        backButton = findViewById(R.id.back_btn)
        backButton!!.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("navigateTo", "HomeFragment")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostDetails()

        postCommentButton?.setOnClickListener {
            if (addCommentEditText!!.text.toString() == "") {
                Toast.makeText(this@CommentsActivity, "กรุณาเขียนคอมเม้นต์ก่อน...", Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }
    }

    private fun init() {
        addCommentEditText = findViewById(R.id.add_comment)
        postImagesRecyclerView = findViewById(R.id.post_images_recycler)
        postTextView = findViewById(R.id.description)
        profileImageView = findViewById(R.id.user_profile_image_post)
        userNameTextView = findViewById(R.id.user_name_post)
        postTimeTextView = findViewById(R.id.post_time)
        postCommentButton = findViewById(R.id.post_comment)
        imageIndicator = findViewById(R.id.image_indicator)
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        val commentText = addCommentEditText!!.text.toString()
        if (commentText.isNotBlank()) {
            val commentId = commentsRef.push().key
            val timestamp = System.currentTimeMillis()
            val commentsMap = HashMap<String, Any>()
            commentsMap["comment"] = commentText
            commentsMap["publisher"] = firebaseUser!!.uid
            commentsMap["commentId"] = commentId!!
            commentsMap["postId"] = postId
            commentsMap["timestamp"] = timestamp

            commentsRef.child(commentId).setValue(commentsMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        addCommentEditText!!.text.clear()
                        if (publisherId != firebaseUser!!.uid) {
                            addNotification(timestamp)
                        }
                    } else {
                        Toast.makeText(this@CommentsActivity, "ไม่สามารถเพิ่มคอมเม้นต์ได้", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this@CommentsActivity, "กรุณาเขียนคอมเม้นต์ที่ไม่ว่างเปล่า", Toast.LENGTH_LONG).show()
        }
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.user).into(profileImageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Failed to load user info: ${error.message}")
            }
        })
    }

    private fun getPostDetails() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post != null) {
                        // แก้ไขส่วน description
                        postTextView?.apply {
                            text = post.description ?: "" // ถ้า null ให้ใช้ empty string
                            visibility = if (post.description.isNullOrBlank()) View.GONE else View.VISIBLE
                        }

                        // ส่วนจัดการรูปภาพ
                        post.postImages?.let { imageUrls ->
                            if (imageUrls.isNotEmpty()) {
                                postImagesRecyclerView?.visibility = View.VISIBLE
                                postImagesAdapter = PostImagesAdapter(this@CommentsActivity, imageUrls)
                                postImagesRecyclerView?.adapter = postImagesAdapter
                                val layoutManager = LinearLayoutManager(
                                    this@CommentsActivity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                postImagesRecyclerView?.layoutManager = layoutManager

                                imageIndicator?.apply {
                                    visibility = if (imageUrls.size > 1) View.VISIBLE else View.GONE
                                    text = "1/${imageUrls.size}"
                                }

                                postImagesRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                        super.onScrolled(recyclerView, dx, dy)
                                        val visiblePosition = layoutManager.findFirstVisibleItemPosition()
                                        if (visiblePosition != -1) {
                                            imageIndicator?.text = "${visiblePosition + 1}/${imageUrls.size}"
                                        }
                                    }
                                })
                            } else {
                                postImagesRecyclerView?.visibility = View.GONE
                                imageIndicator?.visibility = View.GONE
                            }
                        }

                        // ส่วนข้อมูลผู้โพสต์
                        if (!post.publisher.isNullOrBlank()) {
                            FirebaseDatabase.getInstance().reference.child("Users").child(post.publisher)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (userSnapshot.exists()) {
                                            val user = userSnapshot.getValue(User::class.java)
                                            if (user != null) {
                                                userNameTextView?.text = user.getUsername()
                                                Picasso.get().load(user.getImage()).placeholder(R.drawable.user).into(profileImageView)

                                                // เพิ่มการคลิกไปยังโปรไฟล์สำหรับทั้งรูปและชื่อ
                                                val profileClickListener = View.OnClickListener {
                                                    showProfileFragment(post.publisher)
                                                }
                                                profileImageView?.setOnClickListener(profileClickListener)
                                                userNameTextView?.setOnClickListener(profileClickListener)
                                            }
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("CommentsActivity", "Failed to load publisher info: ${error.message}")
                                    }
                                })
                        }

                        if (post.dateTime != 0L) {
                            postTimeTextView?.text = post.getTimeAgo()
                        }
                    } else {
                        Log.e("CommentsActivity", "Failed to parse post data for postId: $postId")
                    }
                } else {
                    Log.e("CommentsActivity", "Post not found for postId: $postId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Failed to load post details: ${error.message}")
            }
        })
    }

    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentList!!.clear()
                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        comment?.let { commentList!!.add(it) }
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CommentsActivity", "Failed to read comments: ${error.message}")
            }
        })
    }

    private fun addNotification(timestamp: Long) {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: " + addCommentEditText!!.text.toString()
        notiMap["postid"] = postId
        notiMap["ispost"] = true
        notiMap["timestamp"] = timestamp
        notiMap["isRead"] = false

        notiRef.push().setValue(notiMap)
    }

    fun showProfileFragment(profileId: String) {
        // ซ่อนเนื้อหาเดิมทั้งหมด
        findViewById<View>(R.id.app_bar_layout_comments)?.visibility = View.GONE
        findViewById<View>(R.id.post_container)?.visibility = View.GONE
        findViewById<View>(R.id.comments_recycler)?.visibility = View.GONE
        findViewById<View>(R.id.commentRelative)?.visibility = View.GONE

        // แสดง fragment_container
        findViewById<View>(R.id.fragment_container)?.visibility = View.VISIBLE

        val fragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("profileId", profileId)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit() // ไม่ต้อง addToBackStack เพราะ CommentsActivity ไม่จัดการ back stack
    }
}