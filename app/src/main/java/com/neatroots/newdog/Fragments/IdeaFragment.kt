package com.neatroots.newdog.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.neatroots.newdog.AppDatabase
import com.neatroots.newdog.CategoryActivity
import com.neatroots.newdog.DataAdapter
import com.neatroots.newdog.DogData

import com.neatroots.newdog.SearchDataActivity
import com.neatroots.newdog.databinding.FragmentIdeaBinding

class IdeaFragment : Fragment() {
    private lateinit var binding: FragmentIdeaBinding
    private lateinit var rvAdapter: DataAdapter
    private lateinit var dataList: ArrayList<DogData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIdeaBinding.inflate(inflater, container, false)
        val view = binding.root

        setUpRecyclerView()


        binding.search.setOnClickListener{
            startActivity(Intent(activity, SearchDataActivity::class.java))
        }
        binding.dogCare.setOnClickListener {
            var myIntent = Intent(activity,CategoryActivity::class.java)
            myIntent.putExtra("TITLE","การดูแล")
            myIntent.putExtra("CATEGORY","การดูแล")
            startActivity(myIntent)
        }

        binding.food.setOnClickListener {
            var myIntent = Intent(activity,CategoryActivity::class.java)
            myIntent.putExtra("TITLE","อาหาร")
            myIntent.putExtra("CATEGORY","อาหาร")
            startActivity(myIntent)
        }
        binding.lifeStyle.setOnClickListener {
            var myIntent = Intent(activity,CategoryActivity::class.java)
            myIntent.putExtra("TITLE","ไลฟ์สไตล์")
            myIntent.putExtra("CATEGORY","ไลฟ์สไตล์")
            startActivity(myIntent)
        }
        binding.hospital.setOnClickListener {
            var myIntent = Intent(activity,CategoryActivity::class.java)
            myIntent.putExtra("TITLE","โรงพยาบาล")
            myIntent.putExtra("CATEGORY","โรงพยาบาล")
            startActivity(myIntent)
        }
        return view

    }

    private fun setUpRecyclerView() {
        dataList = ArrayList()

        binding.rvData.layoutManager = LinearLayoutManager(activity)
        var db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("datadog.db")
            .build()

        var daoObject = db.getDao()
        var datas =daoObject.getAll()
        for (i in datas!!.indices){
            dataList.add(datas[i]!!)
            rvAdapter = DataAdapter(dataList, requireContext())
            binding.rvData.adapter = rvAdapter

        }
    }
}
