package com.neatroots.newdog.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Fragments.ProfileFragment
import com.neatroots.newdog.MainActivity
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(

    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {


        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {


        Log.d("YourTag", "onBindViewHolder: Called for position $position")
        val user = mUser[position]
        holder.userNameTextView.text = user.getUsername()
        val imageUrl = user.getImage()
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.dog).into(holder.userProfileImage)
        } else {

            holder.userProfileImage.setImageResource(R.drawable.user)
        }


        checkFollowingStatus(user.getUID(),holder.followButton)

        // UserAdapter.kt - Inside onBindViewHolder method

        holder.itemView.setOnClickListener(View.OnClickListener {
            Log.d("ClickEvent", "Item clicked")

            if (isFragment){
                if (mContext != null && mContext is FragmentActivity) {
                    Log.d("ClickEvent", "Opening ProfileFragment")

                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", user.getUID())
                    pref.apply()

                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                }
                else
                {
                    Log.d("ClickEvent", "Starting MainActivity")  // เพิ่มบล็อกนี้

                    val intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("publisherId", user.getUID())  // แก้ key เป็น "publisherId"
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK  // เพิ่ม FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }
            }
        })



        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Follower").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }

                                }
                            }
                        }
                }
                addNotification(user.getUID())
            }
            else
            {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }

                                }
                            }
                        }
                }
            }
        }
    }



    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_text_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.image_search_user)
        var followButton: Button = itemView.findViewById(R.id.folow_search_btn)
        init {
            // ตรวจสอบว่า itemView สามารถคลิกได้
            itemView.isClickable = true
            // ตรวจสอบว่า itemView สามารถรับคำสั่งการคลิกได้
            itemView.isFocusable = true
        }
    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {

       var followingRef= firebaseUser?.uid.let { it1 ->
           FirebaseDatabase.getInstance().reference
               .child("Follow").child(it1.toString())
               .child("Following")

       }

        followingRef.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot.child(uid).exists())
                {
                    followButton.text = "Following"
                }
               else
               {
                   followButton.text = "Follow"
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun addNotification(userId: String ){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "start following you"
        notiMap["postid"]= ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }
}
