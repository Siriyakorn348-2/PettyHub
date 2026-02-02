package com.neatroots.newdog.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.AboutActivity
import com.neatroots.newdog.AccountActivity
import com.neatroots.newdog.Adapter.MyImageAdapter
import com.neatroots.newdog.ChangePasswordActivity
import com.neatroots.newdog.CommentsActivity
import com.neatroots.newdog.LoginActivity
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R
import com.neatroots.newdog.ShowUsersActivity
import com.squareup.picasso.Picasso
import java.util.Collections

class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profileId: String

    private lateinit var proName: TextView
    private lateinit var proEmail: TextView
    private lateinit var proDit: Button
    private lateinit var imageDog: ImageView
    private lateinit var totalFollow: TextView
    private lateinit var totalFollowing: TextView
    private lateinit var totalPosts: TextView
    private lateinit var profileFragmentUsername: TextView
    private lateinit var menuButton: ImageView
    private lateinit var backButton: ImageButton

    private var followListener: ValueEventListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        profileId = arguments?.getString("profileId") ?: getProfileIdFromPrefs()

        // Initialize views
        profileFragmentUsername = view.findViewById(R.id.profile_fragment_user)
        totalFollow = view.findViewById(R.id.total_follow)
        totalFollowing = view.findViewById(R.id.total_following)
        proDit = view.findViewById(R.id.edit_pro)
        totalPosts = view.findViewById(R.id.total_posts)
        proName = view.findViewById(R.id.username)
        proEmail = view.findViewById(R.id.email_pro)
        imageDog = view.findViewById(R.id.image_dog)
        menuButton = view.findViewById(R.id.menu_button)
        backButton = view.findViewById(R.id.back_button_profile)

        // Navigation Drawer
        val drawerLayout: androidx.drawerlayout.widget.DrawerLayout = view.findViewById(R.id.drawer_layout)
        val navView: NavigationView = view.findViewById(R.id.nav_view)

        val headerView = navView.getHeaderView(0)
        val navHeaderProfileImage: ImageView = headerView.findViewById(R.id.nav_header_profile_image)
        val navHeaderUsername: TextView = headerView.findViewById(R.id.nav_header_username)
        val navHeaderEmail: TextView = headerView.findViewById(R.id.nav_header_email)

        // ซ่อน menuButton ถ้าไม่ใช่โปรไฟล์ของตัวเอง
        menuButton.visibility = if (profileId == firebaseAuth.currentUser?.uid) View.VISIBLE else View.GONE

        // ตรวจสอบว่าเป็นโปรไฟล์ของตัวเองหรือไม่
        if (profileId == firebaseAuth.currentUser?.uid) {
            // ถ้าเป็นโปรไฟล์ของตัวเอง ซ่อนปุ่มกลับ
            backButton.visibility = View.GONE
            profileFragmentUsername.setPadding(15, 0, 0, 0) // ปรับ padding ชื่อให้ชิดซ้ายเมื่อไม่มีปุ่ม
        } else {
            // ถ้าเป็นโปรไฟล์ของคนอื่น แสดงปุ่มกลับ
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener {
                // ตรวจสอบว่า fragment นี้อยู่ใน back stack หรือไม่
                if (activity is ShowUsersActivity) {
                    // เรียก onBackPressed() ของ ShowUsersActivity เพื่อจัดการการย้อนกลับ
                    requireActivity().onBackPressed()
                } else if (parentFragmentManager.backStackEntryCount > 0) {
                    // ถ้ามี fragment ใน back stack ให้ย้อนกลับไป fragment ก่อนหน้า
                    parentFragmentManager.popBackStack()
                } else {
                    // ถ้าไม่มี fragment ใน back stack ให้ปิด activity
                    requireActivity().finish()
                }
            }
        }

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_editpro -> {
                    startActivity(Intent(requireContext(), AccountActivity::class.java))
                }
                R.id.nav_change_password -> {
                    startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
                }
                R.id.nav_about -> {
                    startActivity(Intent(requireContext(), AboutActivity::class.java))
                }
                R.id.nav_logout -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("ยืนยัน")
                        .setMessage("คุณแน่ใจหรือไม่ว่าต้องการออกจากระบบ?")
                        .setPositiveButton("ใช่") { _, _ ->
                            firebaseAuth.signOut()
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finish()
                        }
                        .setNegativeButton("ไม่") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }

        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        val viewPager: ViewPager2 = view.findViewById(R.id.view_pager)
        val nestedScrollView: NestedScrollView = view.findViewById(R.id.nested_scroll_view)

        viewPager.apply {
            adapter = ProfilePagerAdapter(this@ProfileFragment)
            isNestedScrollingEnabled = false
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> resources.getDrawable(R.drawable.ic_dashboard_black_24dp, null)
                1 -> resources.getDrawable(R.drawable.save_tab, null)
                else -> null
            }
        }.attach()

        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val triggerPoint = view.findViewById<LinearLayout>(R.id.mid).bottom
            val coordinatorLayout = nestedScrollView.parent as? CoordinatorLayout
            coordinatorLayout?.let {
                if (scrollY >= triggerPoint && tabLayout.parent is LinearLayout) {
                    (nestedScrollView.getChildAt(0) as LinearLayout).removeView(tabLayout)
                    it.addView(tabLayout, CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { behavior = com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior() })
                } else if (scrollY < triggerPoint && tabLayout.parent is CoordinatorLayout) {
                    it.removeView(tabLayout)
                    (nestedScrollView.getChildAt(0) as LinearLayout).addView(tabLayout, 2)
                }
            }
        })

        totalFollow.setOnClickListener {
            Intent(context, ShowUsersActivity::class.java).apply {
                putExtra("id", profileId)
                putExtra("title", "followers")
                startActivity(this)
            }
        }
        totalFollowing.setOnClickListener {
            Intent(context, ShowUsersActivity::class.java).apply {
                putExtra("id", profileId)
                putExtra("title", "following")
                startActivity(this)
            }
        }

        proDit.text = if (profileId == firebaseAuth.currentUser?.uid) "แก้ไขโปรไฟล์" else ""
        if (profileId != firebaseAuth.currentUser?.uid) checkFollowAndFollowingButtonStatus()

        // ดึงข้อมูลผู้ใช้และอัปเดตทั้งหน้าโปรไฟล์และ nav_header
        userInfo(profileId, navHeaderProfileImage, navHeaderUsername, navHeaderEmail)
        getFollowers()
        getFollowing()
        getTotalNumberOfPosts()

        proDit.setOnClickListener {
            when (proDit.text.toString()) {
                "แก้ไขโปรไฟล์" -> startActivity(Intent(requireContext(), AccountActivity::class.java))
                "Follow" -> {
                    firebaseAuth.currentUser?.uid?.let { uid ->
                        if (uid != profileId) {
                            val followRef = FirebaseDatabase.getInstance().reference.child("Follow")
                            followRef.child(uid).child("Following").child(profileId).setValue(true)
                            followRef.child(profileId).child("Followers").child(uid).setValue(true)
                            addNotification()
                            proDit.text = "Following"
                            getFollowers()
                        }
                    }
                }
                "Following" -> {
                    firebaseAuth.currentUser?.uid?.let { uid ->
                        val followRef = FirebaseDatabase.getInstance().reference.child("Follow")
                        followRef.child(uid).child("Following").child(profileId).removeValue()
                        followRef.child(profileId).child("Followers").child(uid).removeValue()
                        proDit.text = "Follow"
                        getFollowers()
                    }
                }
            }
        }

        return view
    }

    inner class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> UploadFragment(profileId)
            1 -> SaveFragment(profileId)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    class UploadFragment(private val profileId: String) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_upload, container, false)
            val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_upload)
            val postList: MutableList<Post> = mutableListOf()
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = context?.let { MyImageAdapter(it, postList) }
            }

            FirebaseDatabase.getInstance().reference.child("Posts")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        postList.clear()
                        if (snapshot.exists()) {
                            snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                                .filter { it.publisher == profileId }
                                .forEach { postList.add(it) }
                            postList.reverse()
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            return view
        }
    }

    class SaveFragment(private val profileId: String) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_save, container, false)
            val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_save)
            val postListSaved: MutableList<Post> = mutableListOf()
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = context?.let { MyImageAdapter(it, postListSaved) }
            }

            val mySaveImg: MutableList<String> = mutableListOf()
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                FirebaseDatabase.getInstance().reference.child("Saves").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            mySaveImg.clear()
                            if (snapshot.exists()) {
                                snapshot.children.forEach { mySaveImg.add(it.key!!) }
                                FirebaseDatabase.getInstance().reference.child("Posts")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            postListSaved.clear()
                                            if (snapshot.exists()) {
                                                snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                                                    .filter { mySaveImg.contains(it.postid) }
                                                    .forEach { postListSaved.add(it) }
                                                postListSaved.reverse()
                                                recyclerView.adapter?.notifyDataSetChanged()
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            return view
        }
    }

    private fun checkFollowAndFollowingButtonStatus() {
        firebaseAuth.currentUser?.uid?.let { uid ->
            val followRef = FirebaseDatabase.getInstance().reference.child("Follow").child(uid).child("Following")
            followListener?.let { followRef.removeEventListener(it) }
            followListener = followRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    proDit.text = if (snapshot.child(profileId).exists()) "Following" else "Follow"
                    proDit.isEnabled = true
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun getFollowers() {
        FirebaseDatabase.getInstance().reference.child("Follow").child(profileId).child("Followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserId = firebaseAuth.currentUser?.uid
                    val followerCount = snapshot.children.count { it.key != currentUserId }
                    totalFollow.text = followerCount.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getFollowing() {
        FirebaseDatabase.getInstance().reference.child("Follow").child(profileId).child("Following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserId = firebaseAuth.currentUser?.uid
                    val followingCount = snapshot.children.count { it.key != currentUserId }
                    totalFollowing.text = followingCount.toString()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun userInfo(profileId: String, navImage: ImageView, navUsername: TextView, navEmail: TextView) {
        FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(User::class.java)?.let { user ->
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.user).into(imageDog)
                        profileFragmentUsername.text = user.getUsername()
                        proName.text = user.getUsername()
                        proEmail.text = user.getEmail()

                        // อัปเดต nav_header
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.user).into(navImage)
                        navUsername.text = user.getUsername()
                        navEmail.text = user.getEmail()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getProfileIdFromPrefs(): String {
        return requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .getString("profileId", firebaseAuth.currentUser?.uid ?: "") ?: ""
    }

    private fun getTotalNumberOfPosts() {
        FirebaseDatabase.getInstance().reference.child("Posts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postCount = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                        .count { it.publisher == profileId }
                    totalPosts.text = " $postCount"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addNotification() {
        firebaseAuth.currentUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance().reference.child("Notifications").child(profileId)
                .push().setValue(hashMapOf(
                    "userid" to uid,
                    "text" to "started following you",
                    "postid" to "",
                    "ispost" to false
                ))
        }
    }

    override fun onStop() {
        super.onStop()
        saveProfileIdToPrefs()
    }

    override fun onPause() {
        super.onPause()
        saveProfileIdToPrefs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        followListener?.let {
            firebaseAuth.currentUser?.uid?.let { uid ->
                FirebaseDatabase.getInstance().reference.child("Follow").child(uid)
                    .child("Following").removeEventListener(it)
            }
        }
        saveProfileIdToPrefs()
    }

    private fun saveProfileIdToPrefs() {
        firebaseAuth.currentUser?.uid?.let { uid ->
            requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                .putString("profileId", uid)
                .apply()
        }
    }
}