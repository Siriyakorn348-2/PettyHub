package com.neatroots.newdog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.NotiDogAdapter
import com.neatroots.newdog.Model.EventModel
import com.neatroots.newdog.Model.NotiDog
import android.widget.ImageButton
import java.text.SimpleDateFormat
import java.util.*

class NotiDogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotiDogAdapter
    private val notificationList = mutableListOf<NotiDog>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val CHANNEL_ID = "event_notification_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noti_dog)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = findViewById(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotiDogAdapter(notificationList)
        recyclerView.adapter = notificationAdapter


        val backButton: ImageButton = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            finish()
        }

        createNotificationChannel()
        loadNotifications()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "การแจ้งเตือนอีเวนต์"
            val descriptionText = "การแจ้งเตือนสำหรับอีเวนต์ของสุนัขของคุณ"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        val eventsRef = database.reference.child("users").child(userId).child("events")

        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
                val currentTime = System.currentTimeMillis()

                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: continue
                    for (eventSnapshot in dateSnapshot.children) {
                        val event = eventSnapshot.getValue(EventModel::class.java)
                        event?.let {
                            if (it.notification) {
                                val dateTimeString = "$date ${it.time}"
                                val triggerTime = sdf.parse(dateTimeString)?.time ?: return
                                if (triggerTime <= currentTime) {
                                    val message = "อีเวนต์: ${it.title}   เวลา: ${it.time}"
                                    val notification = NotiDog(it.id, message, triggerTime)
                                    notificationList.add(notification)
                                }
                            }
                        }
                    }
                }
                notificationList.sortByDescending { it.timestamp }
                notificationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}