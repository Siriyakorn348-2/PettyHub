package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.PostAdapter
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.R

class PublicTabFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_public_tab, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.public_recycler)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            postAdapter = context?.let { PostAdapter(it, postList) }
            adapter = postAdapter
        }

        retrievePublicPosts()

        return view
    }

    private fun retrievePublicPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        try {
                            val post = snapshot.getValue(Post::class.java)
                            post?.let { postList.add(it) }
                        } catch (e: Exception) {
                            Log.e("PublicTabFragment", "Error deserializing post: ${e.message}")
                            // กรณี postimage เป็น String เดียว (ข้อมูลเก่า)
                            val postMap = snapshot.value as? Map<String, Any>
                            if (postMap != null) {
                                val postimage = postMap["postimage"]
                                val correctedPostimage = when (postimage) {
                                    is String -> listOf(postimage) // แปลง String เป็น List
                                    is List<*> -> postimage as List<String>
                                    else -> null
                                }
                                val post = Post(
                                    postid = postMap["postid"] as? String ?: "",
                                    postImages  = correctedPostimage,
                                    publisher = postMap["publisher"] as? String ?: "",
                                    description = postMap["description"] as? String ?: "",
                                    username = postMap["username"] as? String ?: "",
                                    dateTime = postMap["dateTime"] as? Long ?: 0L
                                )
                                postList.add(post)
                            }
                        }
                    }
                    postList.sortByDescending { it.dateTime }
                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("PublicTabFragment", "Database error: ${databaseError.message}")
            }
        })
    }
}