package com.neatroots.newdog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.neatroots.newdog.databinding.ActivityDataPetBinding

class DataPetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataPetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDataPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this).load(intent.getStringExtra("img")).into(binding.petImg!!)
        binding.titleTxt!!.text=intent.getStringExtra("title")
        binding.petAge!!.text=intent.getStringExtra("age")
        binding.desTxt!!.text=intent.getStringExtra("des")
        binding.petBreed!!.text=intent.getStringExtra("breed")

        binding.backBtn!!.setOnClickListener {
            finish()
        }
    }
}