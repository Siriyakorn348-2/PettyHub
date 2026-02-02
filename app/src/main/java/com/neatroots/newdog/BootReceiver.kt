package com.neatroots.newdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.app.AlarmManager
import android.app.PendingIntent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.neatroots.newdog.Model.EventModel
import java.text.SimpleDateFormat
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d("BootDebug", "อุปกรณ์รีสตาร์ท - ตั้งค่าแจ้งเตือนใหม่")
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                val database = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(auth.currentUser!!.uid)
                    .child("events")

                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dateSnapshot in snapshot.children) {
                            val date = dateSnapshot.key ?: continue
                            for (eventSnapshot in dateSnapshot.children) {
                                val event = eventSnapshot.getValue(EventModel::class.java)
                                if (event?.notification == true) {
                                    scheduleNotification(context, event, date)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BootDebug", "โหลดข้อมูลล้มเหลว: ${error.message}")
                    }
                })
            }
        }
    }

    private fun scheduleNotification(context: Context, event: EventModel, date: String) {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            Log.w("BootDebug", "ไม่มี WAKE_LOCK permission - ข้ามการตั้งค่าแจ้งเตือน")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val sdf = SimpleDateFormat("yyyy-M-d HH:mm", Locale.getDefault())
        val triggerTime = sdf.parse("$date ${event.time}")?.time ?: return

        try {
            if (triggerTime > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("BootDebug", "ตั้งค่าแจ้งเตือนใหม่สำหรับ ${event.title}")
            }
        } catch (e: SecurityException) {
            Log.e("BootDebug", "SecurityException: ${e.message}")
        }
    }
}