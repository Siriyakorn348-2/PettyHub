package com.neatroots.newdog.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Fragments.PostDetailsFragment
import com.neatroots.newdog.Fragments.ProfileFragment
import com.neatroots.newdog.Model.Notification
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(
    private val mContext: Context,
    private val mNotification: List<Notification>,
    private val currentUserId: String
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mNotification.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        if (notification.getUserId() == currentUserId && notification.getText() == "liked your post ") {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        holder.text.text = when {
            notification.getText() == "started following you" -> "started following you"
            notification.getText() == "liked your post " -> "liked your post"
            notification.getText().contains("commented: ") -> notification.getText().replace("commented: ", "commented: ")
            else -> notification.getText()
        }

        holder.timestamp.text = notification.getTimeAgo()

        Log.d("NotificationAdapter", "Text: ${notification.getText()}, Timestamp: ${notification.getTimestamp()}, TimeAgo: ${notification.getTimeAgo()}")

        userInfo(holder.profileImage, holder.userName, notification.getUserId())

        holder.postImage.visibility = if (notification.isIspost() && !notification.getPostId().isNullOrEmpty()) {
            getPostImage(holder.postImage, notification.getPostId())
            View.VISIBLE
        } else View.GONE

        holder.itemView.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            if (notification.isIspost()) {
                editor.putString("postId", notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment())
                    .commit()
            } else {
                editor.putString("profileId", notification.getUserId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.notification_post_image)
        val profileImage: CircleImageView = itemView.findViewById(R.id.notification_profile_image)
        val userName: TextView = itemView.findViewById(R.id.username_notification)
        val text: TextView = itemView.findViewById(R.id.comment_notification)
        val timestamp: TextView = itemView.findViewById(R.id.notification_timestamp)
    }

    private fun userInfo(imageView: CircleImageView, userName: TextView, publisherId: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            if (!it.getImage().isNullOrEmpty()) {
                                Picasso.get().load(it.getImage()).placeholder(R.drawable.user).into(imageView)
                            }
                            userName.text = it.getUsername() ?: ""
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationAdapter", "Failed to load user info: ${error.message}")
                }
            })
    }

    private fun getPostImage(imageView: ImageView, postId: String?) {
        if (postId.isNullOrEmpty()) return

        FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val post = snapshot.getValue(Post::class.java)
                        val postImageUrls = post?.postImages
                        if (!postImageUrls.isNullOrEmpty()) {
                            Picasso.get()
                                .load(postImageUrls[0])
                                .placeholder(R.drawable.user)
                                .error(R.drawable.user)
                                .into(imageView)
                        } else {
                            imageView.visibility = View.GONE
                        }
                    } else {
                        imageView.visibility = View.GONE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationAdapter", "Failed to load post image: ${error.message}")
                    imageView.visibility = View.GONE
                }
            })
    }
}