package com.neatroots.newdog.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.CommentsActivity
import com.neatroots.newdog.MainActivity
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.neatroots.newdog.ShowUsersActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

import android.widget.PopupMenu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnull
import com.neatroots.newdog.AddPostActivity


class PostAdapter(
    private val mContext : Context,
    private val mPost : List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
 {
        private var firebaseUser :FirebaseUser? = null
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
     {

         val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
         return ViewHolder(view)
     }

     override fun getItemCount(): Int {
         return mPost.size
     }

     @SuppressLint("SuspiciousIndentation")

     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         firebaseUser = FirebaseAuth.getInstance().currentUser
         val post = mPost[position]
         Picasso.get().load(post.getPostimage()).into(holder.postImage)

         if (post.getDescription().equals("")) {
             holder.description.visibility == View.GONE
         } else
         {
             holder.description.visibility == View.VISIBLE
             holder.description.setText(post.getDescription())
         }

         publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())
         isLikes(post.getPostid(),holder.likeButton)
         numberOfLikes(holder.likes,post.getPostid())
         getTotalComments(holder.comments,post.getPostid())
         checkSaveStatus(post.getPostid(),holder.saveButton)


         holder.likeButton.setOnClickListener{
             if (holder.likeButton.tag == "Like"){
                 FirebaseDatabase.getInstance().reference
                     .child("Likes")
                     .child(post.getPostid())
                     .child(firebaseUser!!.uid)
                     .setValue(true)
                 addNotification(post.getPublisher(), post.getPostid())

             }else{
                 FirebaseDatabase.getInstance().reference
                     .child("Likes")
                     .child(post.getPostid())
                     .child(firebaseUser!!.uid)
                     .removeValue()

                 val intent = Intent(mContext,MainActivity::class.java)
                 mContext.startActivity(intent)
             }
         }
         holder.commentButton.setOnClickListener{
             val intentComment = Intent(mContext,CommentsActivity::class.java)
             intentComment.putExtra("postId",post.getPostid())
             intentComment.putExtra("publisherId",post.getPublisher())
             mContext.startActivity(intentComment)
         }
         holder.comments.setOnClickListener{
             val intentComment = Intent(mContext,CommentsActivity::class.java)
             intentComment.putExtra("postId",post.getPostid())
             intentComment.putExtra("publisherId",post.getPublisher())
             mContext.startActivity(intentComment)
         }

         holder.saveButton.setOnClickListener{
             if (holder.saveButton.tag == "Save") {
                 FirebaseDatabase.getInstance().reference
                     .child("Saves")
                     .child(firebaseUser!!.uid)
                     .child(post.getPostid())
                     .setValue(true)

             }else{
                 FirebaseDatabase.getInstance().reference
                     .child("Saves")
                     .child(firebaseUser!!.uid)
                     .child(post.getPostid())
                     .removeValue()

             }

         }
         holder.likes.setOnClickListener{
             val intent = Intent(mContext, ShowUsersActivity::class.java)
             intent.putExtra("id",post.getPostid())
             intent.putExtra("title","likes")
             mContext.startActivity(intent)
         }
         holder.optionButton.setOnClickListener {
             showPopupMenu(holder, post.getPostid())
         }


     }

     private fun showPopupMenu(holder: PostAdapter.ViewHolder, postId: String) {

         val popupMenu = PopupMenu(mContext, holder.optionButton)
         popupMenu.inflate(R.menu.menupost)


         popupMenu.setOnMenuItemClickListener { item: MenuItem ->
             when (item.itemId) {

                 R.id.post_delete -> {

                     deletePost(postId)
                     true
                 }
                 else -> false
             }
         }


         popupMenu.show()
     }



     private fun deletePost(postId: String) {
         val currentUser = FirebaseAuth.getInstance().currentUser

         if (currentUser != null) {
             val alertDialog = AlertDialog.Builder(mContext)
             alertDialog.setTitle("Confirmation")
             alertDialog.setMessage("Are you sure you want to delete this post?")

             alertDialog.setPositiveButton("Yes") { _, _ ->
                 val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

                 // ตรวจสอบว่าผู้ใช้ที่กำลังทำการลบเป็นเจ้าของโพสต์หรือไม่
                 postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                     override fun onDataChange(dataSnapshot: DataSnapshot) {
                         val post = dataSnapshot.getValue(Post::class.java)

                         if (post != null && post.getPublisher() == currentUser.uid) {
                             // เฉพาะเจ้าของโพสต์เท่านั้นที่สามารถลบได้
                             postRef.removeValue()
                             Toast.makeText(mContext, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                         } else {
                             Toast.makeText(mContext, "You are not allowed to delete this post", Toast.LENGTH_SHORT).show()
                         }
                     }

                     override fun onCancelled(databaseError: DatabaseError) {
                         // Handle onCancelled event if needed
                     }
                 })
             }

             alertDialog.setNegativeButton("No") { dialog, _ ->
                 dialog.dismiss()
             }

             alertDialog.show()
         }
     }



     private fun numberOfLikes(likes: TextView, postid: String) {
         val  firebaseUser = FirebaseAuth.getInstance().currentUser
         val LikesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
         LikesRef.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(pO: DataSnapshot) {
                 if (pO.exists())
                 {
                    likes.text = pO.childrenCount.toString() + " likes"
                 }
                 else
                 {

                 }
             }

             override fun onCancelled(pO: DatabaseError) {

             }
         })
     }

     private fun getTotalComments(comments: TextView, postid: String) {
         val  firebaseUser = FirebaseAuth.getInstance().currentUser
         val CommentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
         CommentsRef.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(pO: DataSnapshot) {
                 if (pO.exists())
                 {
                     comments.text = "view all " + pO.childrenCount.toString() + " comments"
                 }
                 else
                 {

                 }
             }

             override fun onCancelled(pO: DatabaseError) {

             }
         })
     }

     private fun isLikes(postid: String, likeButton: ImageView)
     {
         val  firebaseUser = FirebaseAuth.getInstance().currentUser
         val LikesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

         LikesRef.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(pO: DataSnapshot) {
                 if (pO.child(firebaseUser!!.uid).exists()){
                     likeButton.setImageResource(R.drawable.like)
                     likeButton.tag = "Liked"
                 }
                 else
                 {
                     likeButton.setImageResource(R.drawable.animals)
                     likeButton.tag = "Like"
                 }
             }

             override fun onCancelled(pO: DatabaseError) {

             }


         })



     }


     inner class ViewHolder(@Nonnull itemView: View) : RecyclerView.ViewHolder(itemView)
        {
            var profileImage : CircleImageView
            var postImage : ImageView
            var likeButton : ImageView
            var commentButton : ImageView
            var saveButton : ImageView
            var userName : TextView
            var likes : TextView
            var publisher : TextView
            var description : TextView
            var comments : TextView
            var optionButton: ImageView
      init{
          profileImage = itemView.findViewById(R.id.user_profile_image_post)
          postImage = itemView.findViewById(R.id.post_image_home)
          likeButton = itemView.findViewById(R.id.post_image_like_btn)
          commentButton = itemView.findViewById(R.id.post_image_comment_btn)
          saveButton = itemView.findViewById(R.id.post_save_comment_btn)
          userName = itemView.findViewById(R.id.user_name_post)
          likes = itemView.findViewById(R.id.likes)
          publisher = itemView.findViewById(R.id.publisher)
          description = itemView.findViewById(R.id.description)
          comments = itemView.findViewById(R.id.comments)
          optionButton = itemView.findViewById(R.id.option_post)
      }



     }
     private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView,publisherID : String) {

         val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
         usersRef.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(pO: DataSnapshot) {
                 if (pO.exists()){
                     val user = pO.getValue<User>(User::class.java)

                     val imageUrl = user!!.getImage()
                     if (!imageUrl.isNullOrEmpty()) {
                         Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(profileImage)
                     } else {

                      }
                     userName.text = user?.getUsername()
                     publisher.text = user?.getUsername()

                 }
              }

             override fun onCancelled(pO: DatabaseError) {

             }
         })

     }
     private fun checkSaveStatus(postid: String,imageView: ImageView)
     {
         val savesRef = FirebaseDatabase.getInstance().reference
             .child("Saves")
             .child(firebaseUser!!.uid)
         savesRef.addValueEventListener(object  : ValueEventListener{

             override fun onDataChange(pO: DataSnapshot) {
                 if (pO.child(postid).exists())
                 {
                     imageView.setImageResource(R.drawable.save_tab)
                     imageView.tag = "Saved"
                 }
                 else
                 {
                     imageView.setImageResource(R.drawable.save_line)
                     imageView.tag = "Save"

                 }
             }
             override fun onCancelled(pO: DatabaseError) {

             }

         })
     }
     private fun addNotification(userId: String , postId: String){
         val notiRef = FirebaseDatabase.getInstance().reference
             .child("Notifications")
             .child(userId)

         val notiMap = HashMap<String, Any>()
         notiMap["userid"] = firebaseUser!!.uid
         notiMap["text"] = "liked your post"
         notiMap["postid"]= postId
         notiMap["ispost"] = true

         notiRef.push().setValue(notiMap)
     }

 }