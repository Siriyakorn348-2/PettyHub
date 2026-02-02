package com.neatroots.newdog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.bumptech.glide.Glide
import com.neatroots.newdog.databinding.ActivityContentBinding

class ContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContentBinding
    private lateinit var recommendedAdapter: SearchDataAdapter
    private lateinit var recommendedList: ArrayList<DogData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // แสดงข้อมูลหลัก
        Glide.with(this).load(intent.getStringExtra("img")).into(binding.itemImage)
        binding.title.text = intent.getStringExtra("title")
        binding.desText.text = intent.getStringExtra("des")
        binding.dogAge.text = intent.getStringExtra("age")
        binding.dogBreed.text = intent.getStringExtra("breed")

        // ตั้งค่า Database
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("datadog.db")
            .build()

        val daoObject = db.getDao()

        // ดึงข้อมูลสำหรับรายการแนะนำ
        val currentBreed = intent.getStringExtra("breed") ?: ""
        val allData = daoObject.getAll()
        recommendedList = ArrayList()

        // กรองข้อมูลที่มีพันธุ์เดียวกัน (ยกเว้นรายการปัจจุบัน)
        val currentTitle = intent.getStringExtra("title")
        allData.forEach { dog ->
            if (dog?.breed == currentBreed && dog.title != currentTitle) {
                dog?.let { recommendedList.add(it) }
            }
        }

        // ตั้งค่า RecyclerView สำหรับรายการแนะนำ
        setupRecommendedRecyclerView()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecommendedRecyclerView() {
        binding.recommendedRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recommendedAdapter = SearchDataAdapter(recommendedList, this)
        binding.recommendedRecyclerView.adapter = recommendedAdapter

        // ซ่อน RecyclerView ถ้าไม่มีรายการแนะนำ
        if (recommendedList.isEmpty()) {
            binding.recommendedTitle.visibility = android.view.View.GONE
            binding.recommendedRecyclerView.visibility = android.view.View.GONE
        } else {
            binding.recommendedTitle.visibility = android.view.View.VISIBLE
            binding.recommendedRecyclerView.visibility = android.view.View.VISIBLE
        }
    }
}