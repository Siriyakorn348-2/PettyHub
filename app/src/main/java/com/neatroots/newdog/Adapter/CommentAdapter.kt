package com.neatroots.newdog.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Model.Comment
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.TextView

class CommentAdapter(
    private val mContext: Context,
    private val mComment: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment[position]
        holder.commentTV.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.userNameTV, comment.getPublisher())
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: CircleImageView = itemView.findViewById(R.id.user_profile_image_comment)
        var userNameTV: TextView = itemView.findViewById(R.id.user_name_comment)
        var commentTV: TextView = itemView.findViewById(R.id.comment_comment)
    }

    private fun getUserInfo(
        imageProfile: CircleImageView,
        userNameTV: TextView,
        publisher: String
    ) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Debugging: ตรวจสอบข้อมูลที่ถูกดึงมา
                        Log.d("YourTag", "User Data: $user")

                        // Set the profile image
                        val imageUrl = user.getImage()
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.user)
                                .into(imageProfile)
                        } else {
                            imageProfile.setImageResource(R.drawable.user)
                        }

                        userNameTV.text = user.getUsername()
                    } else {
                        Log.e("YourTag", "User is null")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }
}
