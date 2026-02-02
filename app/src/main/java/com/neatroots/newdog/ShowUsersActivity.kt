package com.neatroots.newdog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.UserAdapter
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.Fragments.ProfileFragment

class ShowUsersActivity : AppCompatActivity() {
    private var id: String = ""
    private var title: String = ""
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()
    private var idList: MutableList<String> = mutableListOf()
    private var idListener: ValueEventListener? = null
    private var userListener: ValueEventListener? = null
    private lateinit var currentUserId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        // ดึง ID ของผู้ใช้ปัจจุบัน
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        id = intent.getStringExtra("id").orEmpty()
        title = intent.getStringExtra("title").orEmpty()

        val toolbar: Toolbar = findViewById(R.id.toolbar_show)
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_showuser)
        val progressBar: ProgressBar = findViewById(R.id.progress_bar)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this, userList, true)
        recyclerView.adapter = userAdapter

        progressBar.visibility = View.VISIBLE
        when (title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id).child("Followers")

        idListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUser()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShowUsersActivity", "ข้อผิดพลาดในการดึงผู้ติดตาม: ${error.message}")
                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }.also { followersRef.addValueEventListener(it) }
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id).child("Following")

        idListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUser()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShowUsersActivity", "ข้อผิดพลาดในการดึงการติดตาม: ${error.message}")
                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }.also { followingRef.addValueEventListener(it) }
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id)

        idListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    idList.clear()
                    for (snapshot in dataSnapshot.children) {
                        idList.add(snapshot.key!!)
                    }
                    showUser()
                } else {
                    findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShowUsersActivity", "ข้อผิดพลาดในการดึงถูกใจ: ${error.message}")
                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }.also { likesRef.addValueEventListener(it) }
    }

    private fun showUser() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        userListener?.let { usersRef.removeEventListener(it) }

        userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newUserList = mutableListOf<User>()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.isValid() && idList.contains(user.getUID())) {
                        // กรองผู้ใช้ปัจจุบันออก เฉพาะในกรณี following และ followers
                        if (title == "following" || title == "followers") {
                            if (user.getUID() != currentUserId) {
                                newUserList.add(user)
                            }
                        } else {
                            // สำหรับ likes และ views ให้แสดงทุกคนรวมถึงตัวเอง
                            newUserList.add(user)
                        }
                    }
                }

                val diffResult = DiffUtil.calculateDiff(UserDiffCallback(userList, newUserList))
                userList.clear()
                userList.addAll(newUserList)
                diffResult.dispatchUpdatesTo(userAdapter)
                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE

                // แสดงข้อความ "No users found" ถ้าไม่มีผู้ใช้
                findViewById<TextView>(R.id.no_results_text).visibility =
                    if (userList.isEmpty()) View.VISIBLE else View.GONE

                Log.d("ShowUsersActivity", "อัปเดตรายการผู้ใช้: ขนาด=${userList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShowUsersActivity", "ข้อผิดพลาดในการดึงผู้ใช้: ${error.message}")
                findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }.also { usersRef.addValueEventListener(it) }
    }

    private class UserDiffCallback(
        private val oldList: List<User>,
        private val newList: List<User>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].getUID() == newList[newItemPosition].getUID()
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun showProfileFragment(profileId: String) {
        // ซ่อน Toolbar
        findViewById<Toolbar>(R.id.toolbar_show)?.visibility = View.GONE

        // ซ่อนเนื้อหาเดิมทั้งหมด
        findViewById<View>(R.id.recycler_view_showuser)?.visibility = View.GONE
        findViewById<View>(R.id.no_results_text)?.visibility = View.GONE

        // แสดง fragment_container
        findViewById<View>(R.id.fragment_container)?.visibility = View.VISIBLE

        val fragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("profileId", profileId)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            findViewById<RecyclerView>(R.id.recycler_view_showuser).visibility = View.VISIBLE
            findViewById<TextView>(R.id.no_results_text).visibility =
                if (userList.isEmpty()) View.VISIBLE else View.GONE
            findViewById<View>(R.id.fragment_container).visibility = View.GONE

            findViewById<Toolbar>(R.id.toolbar_show)?.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        idListener?.let {
            when (title) {
                "likes" -> FirebaseDatabase.getInstance().reference.child("Likes").child(id).removeEventListener(it)
                "following" -> FirebaseDatabase.getInstance().reference.child("Follow").child(id).child("Following").removeEventListener(it)
                "followers" -> FirebaseDatabase.getInstance().reference.child("Follow").child(id).child("Followers").removeEventListener(it)
            }
        }
        userListener?.let {
            FirebaseDatabase.getInstance().reference.child("Users").removeEventListener(it)
        }
    }
}