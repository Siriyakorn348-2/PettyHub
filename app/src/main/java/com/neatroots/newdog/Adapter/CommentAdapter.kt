package com.neatroots.newdog.Adapter

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.CommentsActivity
import com.neatroots.newdog.Model.Comment
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(
    private val mContext: Context,
    private val mComment: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment[position]

        holder.commentTV.text = comment.getComment()


        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(comment.getTimestamp())
        holder.timestampTV.text = dateFormat.format(date)


        getUserInfo(holder.imageProfile, holder.userNameTV, comment.getPublisher())

        holder.bindProfileClick(comment.getPublisher())

        // แสดงปุ่มตัวเลือกเฉพาะผู้เขียนความคิดเห็น
        if (comment.getPublisher() == firebaseUser?.uid) {
            holder.optionButton.visibility = View.VISIBLE
            holder.optionButton.setOnClickListener {
                showPopupMenu(holder, comment)
            }
        } else {
            holder.optionButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    private fun showPopupMenu(holder: ViewHolder, comment: Comment) {
        val popupMenu = PopupMenu(mContext, holder.optionButton)
        popupMenu.inflate(R.menu.event_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_event -> {
                    editComment(comment)
                    true
                }
                R.id.delete_event -> {
                    deleteComment(comment)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun editComment(comment: Comment) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("แก้ไขความคิดเห็น")

        val input = EditText(mContext)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(comment.getComment())
        builder.setView(input)

        builder.setPositiveButton("บันทึก") { _, _ ->
            val newCommentText = input.text.toString()
            if (newCommentText.isNotBlank()) {
                val commentsRef = FirebaseDatabase.getInstance().reference
                    .child("Comments")
                    .child(comment.getPostId() ?: "")
                    .child(comment.getCommentId())
                commentsRef.child("comment").setValue(newCommentText)
                    .addOnSuccessListener {
                        Toast.makeText(mContext, "แก้ไขความคิดเห็นสำเร็จ", Toast.LENGTH_SHORT).show()
                        comment.setComment(newCommentText)
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(mContext, "ไม่สามารถแก้ไขได้", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(mContext, "กรุณาใส่ข้อความ", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("ยกเลิก") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun deleteComment(comment: Comment) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("ยืนยัน")
        builder.setMessage("คุณแน่ใจหรือไม่ว่าต้องการลบความคิดเห็นนี้?")
        builder.setPositiveButton("ใช่") { _, _ ->
            val commentsRef = FirebaseDatabase.getInstance().reference
                .child("Comments")
                .child(comment.getPostId() ?: "")
                .child(comment.getCommentId())
            commentsRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(mContext, "ลบความคิดเห็นสำเร็จ", Toast.LENGTH_SHORT).show()
                    mComment.remove(comment)
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(mContext, "ไม่สามารถลบได้", Toast.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("ไม่") { dialog, _ -> dialog.dismiss() }
        builder.show()
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
                        Log.d("CommentAdapter", "User Data: $user")
                        val imageUrl = user.getImage()
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(imageProfile)
                        } else {
                            imageProfile.setImageResource(R.drawable.user)
                        }
                        userNameTV.text = user.getUsername()
                    } else {
                        Log.e("CommentAdapter", "User is null")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CommentAdapter", "Database error: ${databaseError.message}")
            }
        })
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile: CircleImageView = itemView.findViewById(R.id.user_profile_image_comment)
        var userNameTV: TextView = itemView.findViewById(R.id.user_name_comment)
        var commentTV: TextView = itemView.findViewById(R.id.comment_comment)
        var timestampTV: TextView = itemView.findViewById(R.id.comment_timestamp)
        var optionButton: ImageView = itemView.findViewById(R.id.option_comment)

        fun bindProfileClick(publisherId: String) {
            val profileClickListener = View.OnClickListener {
                (mContext as? CommentsActivity)?.showProfileFragment(publisherId)
            }
            imageProfile.setOnClickListener(profileClickListener)
            userNameTV.setOnClickListener(profileClickListener)
        }
    }
}