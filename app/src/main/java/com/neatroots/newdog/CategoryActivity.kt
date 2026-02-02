package com.neatroots.newdog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room

import com.neatroots.newdog.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {
    private lateinit var rvAdapter: CategoryAdapter
    private lateinit var dataList: ArrayList<DogData>
    private val  binding by lazy {
        ActivityCategoryBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        title = intent.getStringExtra("TITLE")

        val titleFromIntent = intent.getStringExtra("TITLE")

        // ตั้งค่า title ของ Activity
        title = titleFromIntent

        // อัปเดต TextView ใน Layout ด้วยค่า title
        binding.title.text = titleFromIntent
        setUpRecycleView()

        binding.back.setOnClickListener {
            finish()
        }
    }

    private fun setUpRecycleView() {
        dataList = ArrayList()
        binding.rvCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        var db = Room.databaseBuilder(this@CategoryActivity,AppDatabase::class.java,"db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("datadog.db")
            .build()
        var daoObject = db.getDao()
        var datas =daoObject.getAll()
        for (i in datas!!.indices){
            if (datas[i]!!.category.contains(intent.getStringExtra("CATEGORY")!!)){
                dataList.add(datas[i]!!)
            }
            rvAdapter=CategoryAdapter(dataList, this)
            binding.rvCategory.adapter = rvAdapter
        }
    }
}