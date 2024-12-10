package com.neatroots.newdog.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.AccountActivity
import com.neatroots.newdog.Adapter.PostAdapter
import com.neatroots.newdog.AddPostActivity
import com.neatroots.newdog.BarProfileActivity
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.R


class HomeFragment<T> : Fragment() {

    private var postAdapter : PostAdapter? = null
    private  var  postList : MutableList<Post>? = null
    private var followingList :MutableList<Post>? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val notibtn : View = view.findViewById(R.id.noti_fragment_home)
        notibtn.setOnClickListener{
            val fragment = NotificationsFragment() // เปลี่ยน YourFragment() เป็นคลาส Fragment ที่คุณต้องการเปิด
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val search: View = view.findViewById(R.id.search_fragment_home)
        search.setOnClickListener{
            val fragment = SearchFragment() // เปลี่ยน YourFragment() เป็นคลาส Fragment ที่คุณต้องการเปิด
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        val addPost: FloatingActionButton = view.findViewById(R.id.addPost)
        addPost.setOnClickListener {
            val intent = Intent(requireContext(), AddPostActivity::class.java)
            startActivity(intent)
        }


        var recyclerView : RecyclerView? = null
        recyclerView = view.findViewById(R.id.home_recycler)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager


        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        checkFollowing()

        return view
    }

    private fun checkFollowing() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(pO: DataSnapshot)
            {
                if (pO.exists()){
                    (followingList as ArrayList<String>).clear()
                    for(snapshot in pO.children){
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }

                    }
                    retrievePosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.orderByChild("publisher").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList?.clear()

                for (snapshot in dataSnapshot.children) {
                    Log.d("HomeFragment", "Snapshot: $snapshot")
                    val post = snapshot.getValue(Post::class.java)

                    if (post != null && (followingList as ArrayList<String>).contains(post.getPublisher())) {
                        Log.d("HomeFragment", "Adding post: $post")
                        postList!!.add(post)
                    }
                }

                postAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // จัดการกรณีเกิดข้อผิดพลาด
            }
        })
    }


}