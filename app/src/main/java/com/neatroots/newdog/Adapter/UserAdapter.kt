package com.neatroots.newdog.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.MainActivity
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.neatroots.newdog.ShowUsersActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val mContext: Context,
    private val mUser: List<User>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mUser.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTextView.text = user.getUsername()

        val imageUrl = user.getImage()
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.dog).into(holder.userProfileImage)
        } else {
            holder.userProfileImage.setImageResource(R.drawable.user)
        }

        // เรียกเช็คสถานะเริ่มต้น และซ่อนปุ่ม Follow ถ้าเป็นตัวเอง
        firebaseUser?.uid?.let { currentUid ->
            if (user.getUID() == currentUid) {
                holder.followButton.visibility = View.GONE
            } else {
                holder.followButton.visibility = View.VISIBLE
                checkFollowingStatus(user.getUID(), holder.followButton)
            }
        }

        // เพิ่มการคลิกเพื่อดูโปรไฟล์
        holder.bindProfileClick(user.getUID())

        // การจัดการปุ่ม Follow
        holder.followButton.setOnClickListener {
            firebaseUser?.uid?.let { uid ->
                // ไม่ให้ดำเนินการถ้าเป็นการติดตามตัวเอง
                if (user.getUID() == uid) return@setOnClickListener

                val followRef = FirebaseDatabase.getInstance().reference.child("Follow")

                if (holder.followButton.text.toString() == "Follow") {
                    holder.followButton.text = "Following"
                    holder.followButton.isEnabled = false

                    followRef.child(uid).child("Following").child(user.getUID()).setValue(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                followRef.child(user.getUID()).child("Followers").child(uid).setValue(true)
                                    .addOnCompleteListener { followerTask ->
                                        holder.followButton.isEnabled = true
                                        if (followerTask.isSuccessful) {
                                            addNotification(user.getUID())
                                        } else {
                                            holder.followButton.text = "Follow"
                                            Log.e("UserAdapter", "Failed to add follower: ${followerTask.exception?.message}")
                                        }
                                    }
                            } else {
                                holder.followButton.text = "Follow"
                                holder.followButton.isEnabled = true
                                Log.e("UserAdapter", "Failed to follow: ${task.exception?.message}")
                            }
                        }
                } else {
                    holder.followButton.text = "Follow"
                    holder.followButton.isEnabled = false

                    followRef.child(uid).child("Following").child(user.getUID()).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                followRef.child(user.getUID()).child("Followers").child(uid).removeValue()
                                    .addOnCompleteListener { followerTask ->
                                        holder.followButton.isEnabled = true
                                        if (!followerTask.isSuccessful) {
                                            holder.followButton.text = "Following"
                                            Log.e("UserAdapter", "Failed to remove follower: ${followerTask.exception?.message}")
                                        }
                                    }
                            } else {
                                holder.followButton.text = "Following"
                                holder.followButton.isEnabled = true
                                Log.e("UserAdapter", "Failed to unfollow: ${task.exception?.message}")
                            }
                        }
                }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_name_text_search)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.image_search_user)
        val followButton: Button = itemView.findViewById(R.id.follow_search_btn)

        fun bindProfileClick(publisherId: String) {
            val profileClickListener = View.OnClickListener {
                (itemView.context as? ShowUsersActivity)?.showProfileFragment(publisherId)
            }
            userProfileImage.setOnClickListener(profileClickListener)
            userNameTextView.setOnClickListener(profileClickListener)
        }
    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {
        firebaseUser?.uid?.let { currentUid ->
            if (uid == currentUid) return

            FirebaseDatabase.getInstance().reference.child("Follow").child(currentUid)
                .child("Following").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isFollowing = snapshot.child(uid).exists()
                        followButton.text = if (isFollowing) "Following" else "Follow"
                        followButton.isEnabled = true
                        Log.d("UserAdapter", "Initial follow status for $uid: $isFollowing")
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("UserAdapter", "Error checking follow status: ${error.message}")
                    }
                })
        }
    }

    private fun addNotification(userId: String) {
        firebaseUser?.uid?.let { currentUid ->
            if (userId == currentUid) return

            val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
            val notiMap = mapOf(
                "userid" to currentUid,
                "text" to "started following you",
                "postid" to "",
                "ispost" to false,
                "isRead" to false
            )
            notiRef.push().setValue(notiMap)
        }
    }
}