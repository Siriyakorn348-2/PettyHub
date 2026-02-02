package com.neatroots.newdog

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.DogAdapter
import com.neatroots.newdog.Model.Dog
import java.text.SimpleDateFormat
import java.util.*

class DogFragment : Fragment() {
    private lateinit var firebaseUser: FirebaseUser
    private var dogAdapter: DogAdapter? = null
    private var dogList: MutableList<Dog> = mutableListOf()
    private var dogId: String = ""
    private var notiDog: ImageButton? = null
    private var notificationCount = 0
    private var notiListener: ValueEventListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dog, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser ?: run {
            Log.e("DogFragment", "User not logged in")
            Toast.makeText(context, "กรุณาล็อกอินเพื่อดูข้อมูลสุนัข", Toast.LENGTH_SHORT).show()
            return view
        }
        Log.d("DogFragment", "Current Firebase UID: ${firebaseUser.uid}")

        val preferences = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        dogId = preferences.getString("dogId", "none") ?: "none"
        Log.d("DogFragment", "Current dogId from SharedPrefs: $dogId")

        val eventDog = view.findViewById<CardView>(R.id.event_dog)
        val dogCare = view.findViewById<CardView>(R.id.dog_care)
        val addDogButton = view.findViewById<FloatingActionButton>(R.id.addDog)
        notiDog = view.findViewById<ImageButton>(R.id.noti_dog)

        val recyclerView = view.findViewById<RecyclerView>(R.id.dog_recycler)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        dogList = mutableListOf()
        dogAdapter = DogAdapter(requireContext(), dogList)
        recyclerView.adapter = dogAdapter

        retrieveDogs(recyclerView)
        retrieveNotifications()

        eventDog.setOnClickListener {
            startActivity(Intent(requireContext(), EventDogMainActivity::class.java))
        }

        addDogButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddDogActivity::class.java))
        }

        dogCare.setOnClickListener {
            startActivity(Intent(requireContext(), DecisionTreeActivity::class.java))
        }

        notiDog?.setOnClickListener {
            clearNotificationBadge()
            startActivity(Intent(requireContext(), NotiDogActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        retrieveNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notiListener?.let {
            FirebaseDatabase.getInstance().reference
                .child("NotificationsEvents")
                .child(firebaseUser.uid)
                .removeEventListener(it)
        }
        notiDog = null
    }

    private fun retrieveNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("NotificationsEvents")
            .child(firebaseUser.uid)

        notiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationCount = 0
                val currentTime = System.currentTimeMillis()

                for (notiSnapshot in snapshot.children) {
                    val notiData = notiSnapshot.value as? Map<String, Any>
                    notiData?.let {
                        val isRead = it["isRead"] as? Boolean ?: false
                        val date = it["date"] as? String
                        val time = it["time"] as? String
                        val title = it["title"] as? String

                        if (date != null && time != null) {
                            // แปลง date และ time เป็น timestamp
                            val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
                            val eventDateTimeString = "$date $time"
                            val eventTimestamp = try {
                                sdf.parse(eventDateTimeString)?.time ?: 0
                            } catch (e: Exception) {
                                Log.e("DogFragment", "Error parsing date/time: $eventDateTimeString", e)
                                0
                            }

                            Log.d("DogFragment", "Notification: Title=$title, DateTime=$eventDateTimeString, Timestamp=$eventTimestamp, CurrentTime=$currentTime, Read=$isRead")

                            // แสดงจุดแดงเฉพาะเมื่อถึงเวลาที่กำหนดและยังไม่ได้อ่าน
                            if (!isRead && eventTimestamp > 0 && currentTime >= eventTimestamp) {
                                notificationCount++
                            }
                        }
                    } ?: Log.e("DogFragment", "Failed to parse notification: ${notiSnapshot.key}")
                }

                Log.d("DogFragment", "Unread notifications count (after time check): $notificationCount")
                if (isAdded) {
                    updateNotificationBadge()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DogFragment", "Failed to retrieve notifications: ${error.message}")
            }
        }.also { listener ->
            notiRef.addValueEventListener(listener)
        }
    }

    private fun updateNotificationBadge() {
        val context = context ?: return
        notiDog?.let { button ->
            Log.d("DogFragment", "Updating badge with count: $notificationCount")
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
                button.setImageDrawable(layeredDrawable)
            } else {
                button.setImageResource(R.drawable.noti)
            }
        }
    }

    private fun clearNotificationBadge() {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("NotificationsEvents")
            .child(firebaseUser.uid)

        notiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("DogFragment", "No notifications to clear")
                    notificationCount = 0
                    if (isAdded) {
                        updateNotificationBadge()
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
                            Log.d("DogFragment", "Notifications marked as read")
                            notificationCount = 0
                            if (isAdded) {
                                updateNotificationBadge()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DogFragment", "Failed to mark notifications as read: ${e.message}")
                        }
                } else {
                    notificationCount = 0
                    if (isAdded) {
                        updateNotificationBadge()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DogFragment", "Failed to clear notifications: ${error.message}")
            }
        })
    }

    private fun retrieveDogs(recyclerView: RecyclerView) {
        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs")
        val loggedInUserId = firebaseUser.uid
        Log.d("DogFragment", "Retrieving dogs for user: $loggedInUserId")

        dogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dogList.clear()
                Log.d("DogFragment", "Total snapshot children count: ${snapshot.childrenCount}")
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    Log.d("DogFragment", "No data found in Dogs node")
                    return
                }

                for (dogSnapshot in snapshot.children) {
                    val dog = dogSnapshot.getValue(Dog::class.java)
                    dog?.let {
                        Log.d("DogFragment", "Retrieved Dog: ID=${it.getDogId()}, Name=${it.getDogName()}, Age=${it.getDogAge()}, Image=${it.getDogImage()}, Breed=${it.getDogBreed()}, Gender=${it.getDogGender()}, UserId=${it.getUserId()}")
                        if (it.getUserId() == loggedInUserId) {
                            dogList.add(it)
                        }
                    } ?: Log.e("DogFragment", "Failed to parse dog: ${dogSnapshot.key}, Data: ${dogSnapshot.value}")
                }

                Log.d("DogFragment", "Filtered dogList size: ${dogList.size}")
                dogAdapter?.notifyDataSetChanged()
                Log.d("DogFragment", "Adapter notified, item count should be: ${dogList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DogFragment", "Failed to retrieve dogs: ${error.message}")
            }
        })
    }
}