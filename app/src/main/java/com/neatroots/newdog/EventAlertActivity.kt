package com.neatroots.newdog

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.Button

class EventAlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlertDebug", "เริ่ม EventAlertActivity")

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_event_alert)

        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.attributes.gravity = Gravity.CENTER
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes.dimAmount = 0.5f


        val eventTitle = intent.getStringExtra("event_title") ?: "อีเว้นท์"
        val eventDescription = intent.getStringExtra("event_description") ?: ""
        Log.d("AlertDebug", "ได้รับ title: $eventTitle, description: $eventDescription")

        val titleTextView: TextView = findViewById(R.id.alert_event_title)
        val descriptionTextView: TextView = findViewById(R.id.alert_event_description)
        val iconImageView: ImageView = findViewById(R.id.alert_icon)
        val closeButton: Button = findViewById(R.id.alert_close_button)

        titleTextView.text = eventTitle
        descriptionTextView.text = eventDescription

        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
        iconImageView.startAnimation(shakeAnimation)



        closeButton.setOnClickListener {
            finish()
        }
    }
}

