package com.neatroots.newdog.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
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

class NotificationAdapter (private val mContext : Context,
                           private val mNotification : List<Notification>)
    : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return  mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val notification =mNotification[position]
        if(notification.getText().equals("started following you"))
        {
            holder.text.text = "started following you"
        }
        else if (notification.getText().equals("liked your post "))
        {
            holder.text.text = "liked your post"
        }
        else if (notification.getText().contains("commented: "))
        {
            holder.text.text = notification.getText().replace("commented: ","commented: ")
        }
        else
        {
            holder.text.text = notification.getText()
        }


        userInfo(holder.profileImage,holder.userName,notification.getUserId())

        if(notification.isIspost())
        {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage,notification.getPostId())
        }
        else{
            holder.postImage.visibility = View.GONE
        }
        holder.itemView.setOnClickListener{
            if (notification.isIspost())
            {
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                editor.putString("postId",notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
            }
            else
            {
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                editor.putString("profileId",notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }

    }
    inner class ViewHolder(@NonNull itemView : View) :RecyclerView.ViewHolder(itemView)
    {
        var postImage : ImageView
        var profileImage : CircleImageView
        var userName : TextView
        var text : TextView

        init{
            postImage = itemView.findViewById(R.id.notification_post_image)
            profileImage = itemView.findViewById(R.id.notification_profile_image)
            userName = itemView.findViewById(R.id.username_notification)
            text = itemView.findViewById(R.id.comment_notification)
        }
    }
    private fun userInfo(imageView : ImageView, userName : TextView , publisherId : String) {
        val userRef = FirebaseDatabase.getInstance()
            .getReference().child("Users")
            .child(publisherId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val user = pO.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.user).into(imageView)
                   userName.text = user!!.getUsername()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getPostImage(imageView: ImageView , postID : String) {
        val postRef =
            FirebaseDatabase.getInstance()
                .reference.child("Posts")
                .child(postID!!)
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    val post = pO.getValue<Post>(Post::class.java)

                    if (post != null) {
                        Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.user).into(imageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }

}