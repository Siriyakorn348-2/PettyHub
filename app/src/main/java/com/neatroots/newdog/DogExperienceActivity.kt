package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.neatroots.newdog.Fragments.IdeaFragment
import com.neatroots.newdog.databinding.ActivityDogExperienceBinding

class DogExperienceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDogExperienceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogExperienceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ปุ่ม "เคยเลี้ยง" - ไปหน้าแรก
        binding.btnYes.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ปุ่ม "ไม่เคยเลี้ยง" - ไปหน้าคำแนะนำ
        binding.btnNo.setOnClickListener {
            val intent = Intent(this, NewOwnerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}