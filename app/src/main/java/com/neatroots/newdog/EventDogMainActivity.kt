package com.neatroots.newdog

import EventDecorator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.neatroots.newdog.Adapter.EventAdapter
import com.neatroots.newdog.Model.EventModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.*

class EventDogMainActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addEventButton: FloatingActionButton
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var eventAdapter: EventAdapter
    private val eventDates = mutableSetOf<CalendarDay>()
    private var selectedDate: String = ""
    private lateinit var rootLayout: View
    private lateinit var toggleCalendarModeButton: ImageButton
    private var isExpanded = true

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_dog_main)

        // สร้าง Notification Channel
        createNotificationChannel()

        val toolbar = findViewById<Toolbar>(R.id.comments_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.eventRecyclerView)
        addEventButton = findViewById(R.id.addEventButton)
        rootLayout = findViewById(R.id.rootLayout)
        toggleCalendarModeButton = findViewById(R.id.toggleCalendarMode)

        database = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("events")

        eventAdapter = EventAdapter(
            emptyList(),
            onEdit = { event -> showEditEventDialog(event) },
            onDelete = { event -> confirmDeleteEvent(event) },
            onClick = { event -> showEventDetails(event) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = eventAdapter

        toggleCalendarModeButton.setOnClickListener { toggleCalendarMode() }

        // รับวันที่จาก Intent ถ้ามี
        val intentDate = intent.getStringExtra("SELECTED_DATE")
        val today = CalendarDay.today()
        selectedDate = intentDate ?: "${today.year}-${today.month + 1}-${today.day}"

        calendarView.setCurrentDate(today)

        // ถ้ามีวันที่จาก Intent ให้ตั้งค่าเป็นวันที่นั้น
        if (intentDate != null) {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val date = sdf.parse(intentDate)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                val calendarDay = CalendarDay.from(calendar)
                calendarView.setSelectedDate(calendarDay)
                calendarView.setCurrentDate(calendarDay)
            }
        } else {
            calendarView.setSelectedDate(today)
        }

        calendarView.setSelectionColor(Color.TRANSPARENT)

        loadEventDates()
        loadEvents(selectedDate)

        calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = "${date.year}-${date.month + 1}-${date.day}"
            Log.d("EventDebug", "เลือกวันที่: $selectedDate")
            loadEvents(selectedDate)
            highlightEventDates()
        }

        addEventButton.setOnClickListener { showAddEventDialog() }

        calendarView.state().edit()
            .setCalendarDisplayMode(com.prolificinteractive.materialcalendarview.CalendarMode.WEEKS)
            .commit()

        val backButton = findViewById<ImageButton>(R.id.back_btn)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showEventDetails(event: EventModel) {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_event_details)
            .create()

        dialog.show()

        dialog.findViewById<TextView>(R.id.event_title)?.text = event.title
        dialog.findViewById<TextView>(R.id.event_time)?.text = "เวลา : ${event.time}"
        dialog.findViewById<TextView>(R.id.event_description)?.text = "รายละเอียด : ${event.description}"
        dialog.findViewById<TextView>(R.id.event_notification)?.text =
            "แจ้งเตือน : ${if (event.notification) "เปิด" else "ปิด"}"

        dialog.findViewById<Button>(R.id.ok_button)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.gravity = Gravity.CENTER
    }

    private fun showEditEventDialog(event: EventModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("แก้ไขอีเว้นท์")
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        builder.setView(dialogView)

        val eventNameInput: EditText = dialogView.findViewById(R.id.eventNameInput)
        val eventTimeInput: EditText = dialogView.findViewById(R.id.eventTimeInput)
        val eventDescriptionInput: EditText = dialogView.findViewById(R.id.eventDescriptionInput)
        val notificationSwitch: Switch = dialogView.findViewById(R.id.notificationSwitch)
        val colorGroup: LinearLayout = dialogView.findViewById(R.id.colorGroup)

        eventNameInput.setText(event.title)
        eventTimeInput.setText(event.time)
        eventDescriptionInput.setText(event.description)
        notificationSwitch.isChecked = event.notification

        eventTimeInput.setOnClickListener { showTimePickerDialog(eventTimeInput) }

        var selectedColor = event.color
        val colorViews = listOf(
            dialogView.findViewById<ImageView>(R.id.colorRed),
            dialogView.findViewById<ImageView>(R.id.colorGreen),
            dialogView.findViewById<ImageView>(R.id.colorBlue),
            dialogView.findViewById<ImageView>(R.id.colorYellow),
            dialogView.findViewById<ImageView>(R.id.colorPurple)
        )

        colorViews.forEach { colorView ->
            when (colorView.id) {
                R.id.colorRed -> if (selectedColor == ContextCompat.getColor(this, R.color.red)) colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                R.id.colorGreen -> if (selectedColor == ContextCompat.getColor(this, R.color.green)) colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                R.id.colorBlue -> if (selectedColor == ContextCompat.getColor(this, R.color.blue)) colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                R.id.colorYellow -> if (selectedColor == ContextCompat.getColor(this, R.color.color7)) colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                R.id.colorPurple -> if (selectedColor == ContextCompat.getColor(this, R.color.purple)) colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
            }
        }

        colorViews.forEach { colorView ->
            colorView.setOnClickListener {
                colorViews.forEach { it.foreground = null }
                colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                selectedColor = when (colorView.id) {
                    R.id.colorRed -> ContextCompat.getColor(this, R.color.red)
                    R.id.colorGreen -> ContextCompat.getColor(this, R.color.green)
                    R.id.colorBlue -> ContextCompat.getColor(this, R.color.blue)
                    R.id.colorYellow -> ContextCompat.getColor(this, R.color.color7)
                    R.id.colorPurple -> ContextCompat.getColor(this, R.color.purple)
                    else -> ContextCompat.getColor(this, R.color.red)
                }
            }
        }

        builder.setPositiveButton("บันทึก") { _, _ ->
            val eventName = eventNameInput.text.toString().trim()
            val eventTime = eventTimeInput.text.toString().trim()
            val eventDescription = eventDescriptionInput.text.toString().trim()
            val isNotificationEnabled = notificationSwitch.isChecked

            if (eventName.isNotEmpty() && eventTime.isNotEmpty()) {
                val updatedEvent = EventModel(
                    event.id,
                    eventName,
                    eventTime,
                    eventDescription,
                    isNotificationEnabled,
                    selectedColor
                )
                database.child(selectedDate).child(event.id).setValue(updatedEvent)
                    .addOnSuccessListener {
                        Toast.makeText(this, "แก้ไขอีเว้นท์เรียบร้อย", Toast.LENGTH_SHORT).show()
                        if (isNotificationEnabled) {
                            scheduleNotification(updatedEvent, selectedDate)
                        }
                        loadEvents(selectedDate)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        builder.setNegativeButton("ยกเลิก") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun confirmDeleteEvent(event: EventModel) {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการลบ")
            .setMessage("คุณต้องการลบอีเว้นท์ '${event.title}' นี้หรือไม่?")
            .setPositiveButton("ลบ") { _, _ ->
                // ยกเลิกการแจ้งเตือนใน AlarmManager
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java).apply {
                    putExtra("event_id", event.id)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    event.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("AlarmDebug", "ยกเลิก AlarmManager สำหรับ ${event.title}")

                // ลบสถานะ stopped_${event.id} ออกจาก SharedPreferences
                val prefs = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
                prefs.edit().remove("stopped_${event.id}").apply()
                Log.d("AlarmDebug", "ลบสถานะ stopped_${event.id} ออกจาก SharedPreferences")

                // ลบอีเว้นท์จาก Firebase
                database.child(selectedDate).child(event.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "ลบอีเว้นท์เรียบร้อย", Toast.LENGTH_SHORT).show()
                        eventDates.remove(parseToCalendarDay(selectedDate))
                        highlightEventDates()
                        loadEvents(selectedDate)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("ยกเลิก") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("เพิ่มอีเว้นท์")
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        builder.setView(dialogView)

        val eventNameInput: EditText = dialogView.findViewById(R.id.eventNameInput)
        val eventTimeInput: EditText = dialogView.findViewById(R.id.eventTimeInput)
        val eventDescriptionInput: EditText = dialogView.findViewById(R.id.eventDescriptionInput)
        val notificationSwitch: Switch = dialogView.findViewById(R.id.notificationSwitch)
        val colorGroup: LinearLayout = dialogView.findViewById(R.id.colorGroup)

        eventTimeInput.setOnClickListener { showTimePickerDialog(eventTimeInput) }

        var selectedColor = ContextCompat.getColor(this, R.color.red)
        val colorViews = listOf(
            dialogView.findViewById<ImageView>(R.id.colorRed),
            dialogView.findViewById<ImageView>(R.id.colorGreen),
            dialogView.findViewById<ImageView>(R.id.colorBlue),
            dialogView.findViewById<ImageView>(R.id.colorYellow),
            dialogView.findViewById<ImageView>(R.id.colorPurple)
        )

        colorViews.forEach { colorView ->
            colorView.setOnClickListener {
                colorViews.forEach { it.foreground = null }
                colorView.foreground = ContextCompat.getDrawable(this, R.drawable.ic_check)
                selectedColor = when (colorView.id) {
                    R.id.colorRed -> ContextCompat.getColor(this, R.color.red)
                    R.id.colorGreen -> ContextCompat.getColor(this, R.color.green)
                    R.id.colorBlue -> ContextCompat.getColor(this, R.color.blue)
                    R.id.colorYellow -> ContextCompat.getColor(this, R.color.color7)
                    R.id.colorPurple -> ContextCompat.getColor(this, R.color.purple)
                    else -> ContextCompat.getColor(this, R.color.red)
                }
            }
        }

        builder.setPositiveButton("บันทึก") { _, _ ->
            val eventName = eventNameInput.text.toString().trim()
            val eventTime = eventTimeInput.text.toString().trim()
            val eventDescription = eventDescriptionInput.text.toString().trim()
            val isNotificationEnabled = notificationSwitch.isChecked

            if (eventName.isNotEmpty() && eventTime.isNotEmpty()) {
                saveEventToDatabase(eventName, eventTime, eventDescription, isNotificationEnabled, selectedColor)
            } else {
                Toast.makeText(this, "กรุณาป้อนชื่ออีเว้นท์และเวลา", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("ยกเลิก") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun saveEventToDatabase(eventName: String, eventTime: String, eventDescription: String, isNotificationEnabled: Boolean, color: Int) {
        val eventId = database.child(selectedDate).push().key
        if (eventId != null) {
            val event = EventModel(eventId, eventName, eventTime, eventDescription, isNotificationEnabled, color)
            database.child(selectedDate).child(eventId).setValue(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "เพิ่มอีเว้นท์เรียบร้อย", Toast.LENGTH_SHORT).show()
                    val date = parseToCalendarDay(selectedDate)
                    if (date != null) {
                        eventDates.add(date)
                        val events = eventMap.getOrPut(date) { mutableListOf() }
                        if (!events.any { it.id == event.id }) {
                            events.add(event)
                        }
                        highlightEventDates()
                        loadEvents(selectedDate)
                        if (isNotificationEnabled) {
                            scheduleNotification(event, selectedDate)
                            saveEventNotification(event, selectedDate)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveEventNotification(event: EventModel, date: String) {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("NotificationsEvents")
            .child(auth.currentUser!!.uid)
        val notiId = notiRef.push().key
        if (notiId != null) {
            val notificationData = mapOf(
                "eventId" to event.id,
                "title" to event.title,
                "time" to event.time,
                "date" to date,
                "description" to event.description,
                "isRead" to false
            )
            notiRef.child(notiId).setValue(notificationData)
                .addOnSuccessListener {
                    Log.d("EventDebug", "บันทึกแจ้งเตือนสำเร็จ: ${event.title}")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกแจ้งเตือน", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(event: EventModel, date: String) {
        // ตรวจสอบ permission
        if (!hasPermissions()) {
            requestPermissions()
            return
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
            putExtra("event_id", event.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
        val dateTimeString = "$date ${event.time}"
        val triggerTime = sdf.parse(dateTimeString)?.time ?: return

        Log.d("AlarmDebug", "ตั้งค่าแจ้งเตือนสำหรับ ${event.title} ที่ $dateTimeString")
        try {
            if (triggerTime > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )
                Log.d("AlarmDebug", "ตั้งค่าแจ้งเตือนทันทีสำหรับ ${event.title}")
            }
        } catch (e: SecurityException) {
            Log.e("AlarmDebug", "SecurityException: ${e.message}")
            Toast.makeText(this, "ไม่สามารถตั้งค่าแจ้งเตือนได้ กรุณาอนุญาตการเข้าถึง", Toast.LENGTH_SHORT).show()
        }
    }

    // ฟังก์ชันตรวจสอบ permission
    private fun hasPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED)
    }

    // ฟังก์ชันขออนุญาต
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.VIBRATE
            ),
            REQUEST_CODE_PERMISSIONS
        )
    }

    // จัดการผลลัพธ์การขออนุญาต
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "อนุญาตเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "กรุณาอนุญาตเพื่อใช้งานการแจ้งเตือน", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for event reminders"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(time)
            },
            hour,
            minute,
            true
        ).show()
    }

    private fun toggleCalendarMode() {
        isExpanded = !isExpanded
        if (isExpanded) {
            calendarView.state().edit()
                .setCalendarDisplayMode(com.prolificinteractive.materialcalendarview.CalendarMode.WEEKS)
                .commit()
            toggleCalendarModeButton.setImageResource(R.drawable.ic_expand)
        } else {
            calendarView.state().edit()
                .setCalendarDisplayMode(com.prolificinteractive.materialcalendarview.CalendarMode.MONTHS)
                .commit()
            toggleCalendarModeButton.setImageResource(R.drawable.ic_collapse)
        }
    }

    private val eventMap = mutableMapOf<CalendarDay, MutableList<EventModel>>()

    private fun loadEventDates() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventDates.clear()
                eventMap.clear()
                for (dateSnapshot in snapshot.children) {
                    val dateString = dateSnapshot.key ?: continue
                    val date = parseToCalendarDay(dateString) ?: continue
                    eventDates.add(date)
                    val events = mutableListOf<EventModel>()
                    for (eventSnapshot in dateSnapshot.children) {
                        val event = eventSnapshot.getValue(EventModel::class.java)
                        event?.let {
                            if (!events.any { existing -> existing.id == it.id }) {
                                events.add(it)
                            }
                        }
                    }
                    eventMap[date] = events
                    Log.d("EventDebug", "โหลด $dateString: ${events.size} อีเวนต์")
                }
                highlightEventDates()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EventDogMainActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun highlightEventDates() {
        calendarView.removeDecorators()
        if (eventMap.isNotEmpty()) {
            val decorator = EventDecorator(this, eventMap)
            val decorators = decorator.getDecorators()
            Log.d("EventDebug", "เพิ่ม ${decorators.size} decorators")
            decorators.forEach { calendarView.addDecorator(it) }
        }
        calendarView.invalidateDecorators()
    }

    private fun loadEvents(date: String) {
        database.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<EventModel>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(EventModel::class.java)
                    if (event != null) eventList.add(event)
                }
                eventAdapter.updateEvents(eventList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EventDogMainActivity, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseToCalendarDay(date: String): CalendarDay? {
        return try {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val parsedDate = sdf.parse(date)
            if (parsedDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                CalendarDay.from(calendar)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}