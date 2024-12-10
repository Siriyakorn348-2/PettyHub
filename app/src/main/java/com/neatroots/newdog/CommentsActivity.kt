package com.neatroots.newdog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.neatroots.newdog.Adapter.CommentAdapter
import com.neatroots.newdog.Fragments.HomeFragment
import com.neatroots.newdog.Model.Comment
import com.neatroots.newdog.Model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private  var firebaseUser : FirebaseUser? = null
    private var commentAdapter : CommentAdapter? = null
    private var commmentList : MutableList<Comment>? = null

    var add_comment:EditText? = null
    var post_image_comment : ImageView? = null
    var profile_image_comment: CircleImageView? = null
    var post_comment: TextView? = null

    var back : ImageButton? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        init()

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView : RecyclerView
        recyclerView = findViewById(R.id.comments_recycler)
        val  linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout  = true
        recyclerView.layoutManager = linearLayoutManager


        back =findViewById(R.id.back_btn)
        back!!.setOnClickListener {
            startActivity(Intent(this, HomeFragment::class.java))
        }

        commmentList = ArrayList()
        commentAdapter = CommentAdapter(this, commmentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        post_comment?.setOnClickListener(View.OnClickListener {
            if (add_comment!!.text.toString() == "") {
                Toast.makeText(this@CommentsActivity,"Please write comment first...",Toast.LENGTH_LONG).show()
            } else{
                addComment()
            }
        })

    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        // Check if the comment is not empty
        val commentText = add_comment!!.text.toString()
        if (commentText.isNotBlank()) {
            // Generate a unique comment id
            val commentId = commentsRef.push().key

            // Create a map for the new comment
            val commentsMap = HashMap<String, Any>()
            commentsMap["comment"] = commentText
            commentsMap["publisher"] = firebaseUser!!.uid
            commentsMap["commentid"] = commentId!!

            // Add the new comment
            commentsRef.child(commentId).setValue(commentsMap)

            // Clear the input field
            add_comment!!.text.clear()

            // Add notification
            addNotification()
        } else {
            Toast.makeText(this@CommentsActivity, "Please write a non-empty comment", Toast.LENGTH_LONG).show()
        }
    }

    private fun userInfo() {
        Log.d("YourTag", "FirebaseUser in userInfo(): $firebaseUser")

        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val user = pO.getValue<User>(User::class.java)
                    Log.d("YourTag", "User Data: $user")
                    if (user != null) {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.user).into(profile_image_comment)


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
    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance()
                .reference.child("Posts")
                .child(postId!!).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val image = pO.value.toString()
                    Log.d("YourTag", "User Data: $image")
                    if (image != null) {
                        Picasso.get().load(image).placeholder(R.drawable.user).into(post_image_comment)

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
    fun init(){
        add_comment = findViewById(R.id.add_comment)
        post_image_comment = findViewById(R.id.post_image_comment)
        profile_image_comment = findViewById(R.id.profile_image_comment)
        post_comment = findViewById(R.id.post_comment)


    }
    private fun  readComments(){
        val commentsRef = FirebaseDatabase.getInstance()
            .reference.child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists())
                {
                    commmentList!!.clear()
                    for (snapshot in pO.children)
                    {
                        val comment = snapshot.getValue(Comment::class.java)
                        commmentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId!!)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: " + add_comment!!.text.toString()
        notiMap["postid"]= postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }

}