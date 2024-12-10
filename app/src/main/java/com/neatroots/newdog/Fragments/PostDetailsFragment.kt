package com.neatroots.newdog.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.PostAdapter
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.R



class PostDetailsFragment : Fragment() {
    private var  postAdapter : PostAdapter? = null
    private  var  postList : MutableList<Post>? = null
    private var  postId : String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_post_details, container, false)

        val preferences = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if (preferences != null)
        {
            postId = preferences.getString("postId","none").toString()
        }

        val recyclerView : RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_post_detail)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager


        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        retrievePosts()
        return view
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)

        postsRef.orderByChild("publisher").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                postList?.clear()
                val post = pO.getValue(Post::class.java)

                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // จัดการกรณีเกิดข้อผิดพลาด
            }
        })
    }


}