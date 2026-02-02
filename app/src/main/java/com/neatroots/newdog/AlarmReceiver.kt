package com.neatroots.newdog

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_STOP_ALARM = "com.neatroots.newdog.STOP_ALARM"
        private const val REPEAT_INTERVAL = 60 * 1000L // 1 นาที
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmDebug", "AlarmReceiver ทำงานแล้ว")

        if (intent.action == ACTION_STOP_ALARM) {
            stopRepeatingAlarm(context, intent)
            return
        }

        val title = intent.getStringExtra("event_title") ?: "อีเว้นท์"
        val description = intent.getStringExtra("event_description") ?: ""
        val eventId = intent.getStringExtra("event_id") ?: System.currentTimeMillis().toString()
        Log.d("AlarmDebug", "รับ title: $title, description: $description, eventId: $eventId")

        val prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        val isStopped = prefs.getBoolean("stopped_$eventId", false)
        if (isStopped) {
            Log.d("AlarmDebug", "แจ้งเตือนสำหรับ $eventId ถูกหยุดแล้ว")
            return
        }

        // ตรวจสอบว่าอีเว้นท์ยังอยู่ใน Firebase หรือไม่
        checkEventExists(context, eventId) { exists ->
            if (!exists) {
                Log.d("AlarmDebug", "อีเว้นท์ $eventId ถูกลบแล้ว หยุดการแจ้งเตือน")
                stopRepeatingAlarm(context, intent)
                return@checkEventExists
            }

            // Intent สำหรับเปิดแอป
            val notificationIntent = Intent(context, EventDogMainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent สำหรับหยุดแจ้งเตือน
            val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_STOP_ALARM
                putExtra("event_id", eventId)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                eventId.hashCode(),
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder = NotificationCompat.Builder(context, "event_channel")
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_stop, "หยุด", stopPendingIntent)

            if (context.checkSelfPermission(android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                builder.setVibrate(longArrayOf(0, 500, 500))
            } else {
                Log.w("AlarmDebug", "ไม่มี VIBRATE permission - ข้ามการสั่น")
            }

            with(NotificationManagerCompat.from(context)) {
                notify(eventId.hashCode(), builder.build())
            }

            scheduleNextAlarm(context, eventId, title, description)
        }
    }

    private fun checkEventExists(context: Context, eventId: String, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            callback(false)
            return
        }

        val database = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("events")

        // ค้นหา eventId ในทุกวันที่อยู่ในฐานข้อมูล
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var eventExists = false
                for (dateSnapshot in snapshot.children) {
                    if (dateSnapshot.child(eventId).exists()) {
                        eventExists = true
                        break
                    }
                }
                Log.d("AlarmDebug", "ตรวจสอบอีเว้นท์ $eventId: exists = $eventExists")
                callback(eventExists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AlarmDebug", "ตรวจสอบอีเว้นท์ล้มเหลว: ${error.message}")
                callback(false)
            }
        })
    }

    private fun scheduleNextAlarm(context: Context, eventId: String, title: String, description: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("event_title", title)
            putExtra("event_description", description)
            putExtra("event_id", eventId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + REPEAT_INTERVAL
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmDebug", "ตั้งค่าแจ้งเตือนซ้ำสำหรับ $title ในอีก 1 นาที")
        } catch (e: SecurityException) {
            Log.e("AlarmDebug", "SecurityException: ${e.message}")
        }
    }

    private fun stopRepeatingAlarm(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra("event_id") ?: return
        val prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("stopped_$eventId", true).apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("event_id", eventId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        with(NotificationManagerCompat.from(context)) {
            cancel(eventId.hashCode())
        }
        Log.d("AlarmDebug", "หยุดแจ้งเตือนสำหรับ eventId: $eventId")
    }
}