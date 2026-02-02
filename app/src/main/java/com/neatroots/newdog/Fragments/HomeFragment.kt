package com.neatroots.newdog.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.AddPostActivity
import com.neatroots.newdog.R

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var notificationCount = 0
    private var notiListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // ตั้งค่าปุ่มแจ้งเตือน
        val notiBtn: View = view.findViewById(R.id.noti_fragment_home)
        notiBtn.setOnClickListener {
            clearNotificationBadge() // ล้างสถานะแจ้งเตือนก่อนไปหน้าใหม่
            val fragment = NotificationsFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // ตั้งค่าปุ่มค้นหา
        val search: View = view.findViewById(R.id.search_fragment_home)
        search.setOnClickListener {
            val fragment = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // ตั้งค่าปุ่มเพิ่มโพสต์
        val addPost: FloatingActionButton = view.findViewById(R.id.addPost)
        addPost.setOnClickListener {
            val intent = Intent(requireContext(), AddPostActivity::class.java)
            startActivity(intent)
        }

        // ตั้งค่า TabLayout และ ViewPager2
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)

        val adapter = HomePagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setCustomView(R.layout.custom_tab)
            val textView = tab.customView?.findViewById<TextView>(R.id.custom_text)
            when (position) {
                0 -> textView?.text = "สาธารณะ"
                1 -> textView?.text = "กำลังติดตาม"
            }
        }.attach()

        // โหลดจำนวนแจ้งเตือน
        retrieveNotifications(notiBtn as android.widget.ImageView)

        return view
    }

    override fun onResume() {
        super.onResume()
        val notiBtn = view?.findViewById<View>(R.id.noti_fragment_home) as? android.widget.ImageView
        notiBtn?.let {
            retrieveNotifications(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notiListener?.let {
            FirebaseDatabase.getInstance().reference
                .child("Notifications")
                .child(auth.currentUser?.uid ?: "")
                .removeEventListener(it)
        }
    }

    private fun retrieveNotifications(notiButton: android.widget.ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val notiRef = database.reference.child("Notifications").child(userId)

        notiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationCount = 0
                for (notiSnapshot in snapshot.children) {
                    val notiData = notiSnapshot.value as? Map<String, Any>
                    notiData?.let {
                        val isRead = it["isRead"] as? Boolean ?: false
                        Log.d("HomeFragment", "Notification: ID=${notiSnapshot.key}, Read=$isRead")
                        if (!isRead) {
                            notificationCount++
                        }
                    }
                }
                Log.d("HomeFragment", "Unread notifications count: $notificationCount")
                if (isAdded) {
                    updateNotificationBadge(notiButton)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to retrieve notifications: ${error.message}")
            }
        }.also { listener ->
            notiRef.addValueEventListener(listener)
        }
    }

    private fun updateNotificationBadge(notiButton: android.widget.ImageView) {
        val context = context ?: return
        Log.d("HomeFragment", "Updating badge with count: $notificationCount")
        if (notificationCount > 0) {
            val iconDrawable = context.resources.getDrawable(R.drawable.noti, null)
            val badgeSize = (16 * context.resources.displayMetrics.density).toInt()
            val bitmap = Bitmap.createBitmap(badgeSize, badgeSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val radius = badgeSize / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10 * context.resources.displayMetrics.density
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val countText = notificationCount.toString()
            canvas.drawText(countText, radius, radius + textPaint.textSize / 3, textPaint)
            val badgeDrawable = BitmapDrawable(context.resources, bitmap)
            badgeDrawable.setBounds(0, 0, badgeSize, badgeSize)
            val layeredDrawable = LayerDrawable(arrayOf(iconDrawable, badgeDrawable))
            layeredDrawable.setLayerInset(1, iconDrawable.intrinsicWidth - badgeSize, 0, 0, iconDrawable.intrinsicHeight - badgeSize)
            notiButton.setImageDrawable(layeredDrawable)
        } else {
            notiButton.setImageResource(R.drawable.noti)
        }
    }

    private fun clearNotificationBadge() {
        val userId = auth.currentUser?.uid ?: return
        val notiRef = database.reference.child("Notifications").child(userId)

        notiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("HomeFragment", "No notifications to clear")
                    notificationCount = 0
                    if (isAdded) {
                        val notiButton = view?.findViewById<View>(R.id.noti_fragment_home) as? android.widget.ImageView
                        notiButton?.let { updateNotificationBadge(it) }
                    }
                    return
                }

                val updates = mutableMapOf<String, Any>()
                for (notiSnapshot in snapshot.children) {
                    val notiData = notiSnapshot.value as? Map<String, Any>
                    notiData?.let {
                        val isRead = it["isRead"] as? Boolean ?: false
                        if (!isRead) {
                            updates["${notiSnapshot.key}/isRead"] = true
                        }
                    }
                }

                if (updates.isNotEmpty()) {
                    notiRef.updateChildren(updates)
                        .addOnSuccessListener {
                            Log.d("HomeFragment", "Notifications marked as read")
                            notificationCount = 0
                            if (isAdded) {
                                val notiButton = view?.findViewById<View>(R.id.noti_fragment_home) as? android.widget.ImageView
                                notiButton?.let { updateNotificationBadge(it) }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("HomeFragment", "Failed to mark notifications as read: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to clear notifications: ${error.message}")
            }
        })
    }
}

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PublicTabFragment()
            1 -> FollowingTabFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}