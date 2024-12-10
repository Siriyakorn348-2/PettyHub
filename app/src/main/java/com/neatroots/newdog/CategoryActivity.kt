package com.neatroots.newdog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room

import com.neatroots.newdog.databinding.ActivityCategoryBinding

class CategoryActivity : AppCompatActivity() {


    private lateinit var rvAdapter: CategoryAdapter
    private lateinit var dataList:ArrayList<Petdata>
    private val binding by lazy {
        ActivityCategoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.headerTxt!!.text =intent.getStringExtra("TITLE")
        setUpRecyclerView()
        binding.gotoHome!!.setOnClickListener {
            finish()
        }
    }

    private fun setUpRecyclerView() {
        dataList= ArrayList()

        binding.rvCategoty.layoutManager= LinearLayoutManager(this)
        var db= Room.databaseBuilder(this@CategoryActivity, AppDatabase::class.java,"db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("petdata.db")
            .build()

        var daoObject=db.getDao()
        var petdatas=daoObject.getAll()
        for (i in petdatas!!.indices){
            if (petdatas[i]!!.category.contains(intent.getStringExtra("CATEGORY")!!)){
                dataList.add(petdatas[i]!!)
            }

            rvAdapter= CategoryAdapter(dataList,this)
            binding.rvCategoty.adapter=rvAdapter
        }
    }
}