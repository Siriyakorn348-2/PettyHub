package com.neatroots.newdog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.UserAdapter
import com.neatroots.newdog.Model.User

class ShowUsersActivity : AppCompatActivity() {
    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val inline = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: Toolbar = findViewById(R.id.toolbar_show)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view_showuser)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when (title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }


    }

    private fun getViews() {

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in pO.children) {
                    (idList as ArrayList<String>).add((snapshot.key!!))
                }
                showUser()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in pO.children) {
                    (idList as ArrayList<String>).add((snapshot.key!!))
                }
                showUser()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun getLikes() {
        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id!!)

        LikesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snapshot in pO.children) {
                        (idList as ArrayList<String>).add((snapshot.key!!))
                    }
                    showUser()

                } else {

                }
            }

            override fun onCancelled(pO: DatabaseError) {

            }
        })
    }

    private fun showUser() {
        val usersRef = FirebaseDatabase.getInstance()
            .getReference()
            .child("Users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    // ตรวจสอบว่าข้อมูลที่ได้เป็น String หรือไม่
                    val userData = snapshot.getValue()
                    if (userData is String) {
                        // ข้ามข้อมูลที่เป็น String
                        continue
                    }

                    // ตรวจสอบว่าข้อมูลที่ได้เป็น User และกำลังติดตามหรือไม่
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.isValid() && isFollowing(user.getUID())) {
                        (userList as ArrayList<User>).add(user)
                    }
                }

                // เพิ่มการแสดงจำนวนผู้ติดตามและคนที่กำลังติดตาม
                val followersCount = idList?.size ?: 0
                val followingCount = userList?.size ?: 0
                Log.d("YourTag", "Followers Count: $followersCount, Following Count: $followingCount")

                userAdapter?.notifyDataSetChanged()
                Log.d("YourTag", "showUser Adapter notifyDataSetChanged called")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun isFollowing(userId: String): Boolean {
        return idList?.contains(userId) == true
    }

}