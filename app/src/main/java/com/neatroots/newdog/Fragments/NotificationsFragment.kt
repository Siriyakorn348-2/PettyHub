package com.neatroots.newdog.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.neatroots.newdog.Adapter.NotificationAdapter
import com.neatroots.newdog.Model.Notification
import com.neatroots.newdog.R
import com.neatroots.newdog.utils.SpacingItemDecoration
import java.util.Collections

class NotificationsFragment : Fragment() {
    private var notificationList: List<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null
    private var currentUserId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        var recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_notification)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.addItemDecoration(SpacingItemDecoration(spacing = 20, topSpacing = 10))

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        notificationList = ArrayList()
        notificationAdapter = NotificationAdapter(
            requireContext(),
            notificationList as ArrayList<Notification>,
            currentUserId ?: ""
        )
        recyclerView.adapter = notificationAdapter

        readNotifications()

        return view
    }

    private fun readNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        notiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (notificationList as ArrayList<Notification>).clear()
                    for (snapshot in dataSnapshot.children) {
                        val notification = snapshot.getValue(Notification::class.java)
                        notification?.let {
                            (notificationList as ArrayList<Notification>).add(it)
                        }
                    }
                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsFragment", "Failed to read notifications: ${error.message}")
            }
        })
    }
}