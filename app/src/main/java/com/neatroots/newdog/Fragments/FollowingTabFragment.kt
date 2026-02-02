package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.PostAdapter
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.R

class FollowingTabFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post> = mutableListOf()
    private var followingList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_following_tab, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.following_recycler)
        recyclerView.apply {
            setHasFixedSize(true) // ปรับปรุง performance
            layoutManager = LinearLayoutManager(context)
            postAdapter = context?.let { PostAdapter(it, postList) }
            adapter = postAdapter
        }

        checkFollowing()

        return view
    }

    private fun checkFollowing() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) return // ออกถ้าไม่มีผู้ใช้ล็อกอิน

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(currentUser.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.key?.let { followingList.add(it) }
                    }
                    retrievePosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FollowingTabFragment", "Failed to load following list: ${error.message}")
            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val post = child.getValue(Post::class.java)
                        if (post != null && followingList.contains(post.publisher)) {
                            postList.add(post)
                        }
                    }
                    postList.sortByDescending { it.dateTime }
                    postAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FollowingTabFragment", "Failed to load posts: ${error.message}")
            }
        })
    }
}