package com.neatroots.newdog.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.CommentsActivity
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.neatroots.newdog.ShowUsersActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MyImageAdapter(
    private val mContext: Context,
    private val mPosts: MutableList<Post>
) : RecyclerView.Adapter<MyImageAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPosts[position]


        val imageUrls = post.postImages ?: emptyList()
        holder.postImagesRecycler.apply {
            visibility = if (imageUrls.isNotEmpty()) {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                adapter = ImageListAdapter(mContext, imageUrls)
                PagerSnapHelper().attachToRecyclerView(this)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visiblePosition = layoutManager.findFirstVisibleItemPosition()
                        holder.imageIndicator.text = "${visiblePosition + 1}/${imageUrls.size}"
                    }
                })
                View.VISIBLE
            } else {
                View.GONE
            }
        }


        holder.imageIndicator.apply {
            visibility = if (imageUrls.size > 1) View.VISIBLE else View.GONE
            text = "1/${imageUrls.size}"
        }


        holder.description.apply {
            visibility = if (post.description.isEmpty()) View.GONE else View.VISIBLE
            text = post.description
        }


        holder.postTime.text = post.getTimeAgo() ?: "ไม่ทราบเวลา"

        holder.optionButton.visibility = if (post.publisher == firebaseUser?.uid) View.VISIBLE else View.GONE

        getUserInfo(holder.profileImage, holder.userName, post.publisher)
        isLikes(post.postid, holder.likeButton)
        numberOfLikes(holder.likes, post.postid)
        getTotalComments(holder.comments, post.postid)
        checkSaveStatus(post.postid, holder.saveButton)


        holder.likeButton.setOnClickListener {
            firebaseUser?.let { user ->
                val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                if (holder.likeButton.tag == "Like") {
                    likesRef.child(user.uid).setValue(true)
                    if (post.publisher != user.uid) {
                        addNotification(post.publisher, post.postid)
                    }
                } else {
                    likesRef.child(user.uid).removeValue()
                }
            }
        }

        val commentClickListener = View.OnClickListener {
            Intent(mContext, CommentsActivity::class.java).apply {
                putExtra("postId", post.postid)
                putExtra("publisherId", post.publisher)
                mContext.startActivity(this)
            }
        }
        holder.commentButton.setOnClickListener(commentClickListener)
        holder.comments.setOnClickListener(commentClickListener)

        holder.saveButton.setOnClickListener {
            firebaseUser?.let { user ->
                val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(user.uid).child(post.postid)
                if (holder.saveButton.tag == "Save") {
                    savesRef.setValue(true)
                } else {
                    savesRef.removeValue()
                }
            }
        }

        holder.likes.setOnClickListener {
            Intent(mContext, ShowUsersActivity::class.java).apply {
                putExtra("id", post.postid)
                putExtra("title", "likes")
                mContext.startActivity(this)
            }
        }

        holder.optionButton.setOnClickListener { showPopupMenu(holder, post.postid) }
    }

    override fun getItemCount(): Int = mPosts.size

    private fun showPopupMenu(holder: ViewHolder, postId: String) {
        PopupMenu(mContext, holder.optionButton).apply {
            inflate(R.menu.menupost)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.post_delete -> {
                        deletePost(postId)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun deletePost(postId: String) {
        firebaseUser?.let { currentUser ->
            AlertDialog.Builder(mContext).apply {
                setTitle("ยืนยัน")
                setMessage("คุณแน่ใจหรือไม่ว่าต้องการลบโพสต์นี้?")
                setPositiveButton("ใช่") { _, _ ->
                    val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
                    postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val post = snapshot.getValue(Post::class.java)
                            if (post?.publisher == currentUser.uid) {
                                postRef.removeValue()
                                mPosts.removeAll { it.postid == postId }
                                notifyDataSetChanged()
                                Toast.makeText(mContext, "ลบโพสต์สำเร็จ", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(mContext, "คุณไม่มีสิทธิ์ลบโพสต์นี้", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                setNegativeButton("ไม่") { dialog, _ -> dialog.dismiss() }
                show()
            }
        }
    }

    private fun numberOfLikes(likes: TextView, postid: String) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    likes.text = if (snapshot.exists()) "${snapshot.childrenCount} ถูกใจ" else "0 ถูกใจ"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getTotalComments(comments: TextView, postid: String) {
        FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.text = if (snapshot.exists()) "ดูทั้งหมด ${snapshot.childrenCount} ความคิดเห็น" else "0 ความคิดเห็น"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        firebaseUser?.let { user ->
            FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(user.uid).exists()) {
                            likeButton.setImageResource(R.drawable.like)
                            likeButton.tag = "Liked"
                        } else {
                            likeButton.setImageResource(R.drawable.animals)
                            likeButton.tag = "Like"
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun getUserInfo(profileImage: CircleImageView, userName: TextView, publisherId: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            val imageUrl = it.getImage()
                            profileImage.setImageResource(R.drawable.user)
                            if (!imageUrl.isNullOrEmpty()) {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(profileImage)
                            }
                            userName.text = it.getUsername() ?: "ไม่ระบุชื่อ"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkSaveStatus(postid: String, imageView: ImageView) {
        firebaseUser?.let { user ->
            FirebaseDatabase.getInstance().reference.child("Saves").child(user.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(postid).exists()) {
                            imageView.setImageResource(R.drawable.save_tab)
                            imageView.tag = "Saved"
                        } else {
                            imageView.setImageResource(R.drawable.save_line)
                            imageView.tag = "Save"
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun addNotification(userId: String, postId: String) {
        firebaseUser?.let { user ->
            FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
                .push().setValue(
                    hashMapOf(
                        "userid" to user.uid,
                        "text" to "ถูกใจโพสต์ของคุณ",
                        "postid" to postId,
                        "ispost" to true,
                        "isRead" to false
                    )
                )
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        val postImagesRecycler: RecyclerView = itemView.findViewById(R.id.post_images_recycler)
        val imageIndicator: TextView = itemView.findViewById(R.id.image_indicator)
        val likeButton: ImageView = itemView.findViewById(R.id.post_image_like_btn)
        val commentButton: ImageView = itemView.findViewById(R.id.post_image_comment_btn)
        val saveButton: ImageView = itemView.findViewById(R.id.post_save_comment_btn)
        val userName: TextView = itemView.findViewById(R.id.user_name_post)
        val likes: TextView = itemView.findViewById(R.id.likes)
        val description: TextView = itemView.findViewById(R.id.description)
        val comments: TextView = itemView.findViewById(R.id.comments)
        val optionButton: ImageView = itemView.findViewById(R.id.option_post)
        val postTime: TextView = itemView.findViewById(R.id.post_time)
    }
}