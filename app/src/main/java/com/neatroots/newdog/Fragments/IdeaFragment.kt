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
import com.neatroots.newdog.Petdata

import com.neatroots.newdog.R
import com.neatroots.newdog.RecommendAdapter
import com.neatroots.newdog.SearchActivity
import com.neatroots.newdog.databinding.FragmentIdeaBinding

class IdeaFragment : Fragment() {
    private lateinit var binding: FragmentIdeaBinding
    private lateinit var rvAdapter: RecommendAdapter
    private lateinit var dataList: ArrayList<Petdata>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIdeaBinding.inflate(inflater, container, false)
        val view = binding.root

        setUpRecyclerView()

        binding.searchTxt.setOnClickListener {
            startActivity(Intent(activity, SearchActivity::class.java))
        }

        binding.health.setOnClickListener {
            var myIntent = Intent(activity, CategoryActivity::class.java)
            myIntent.putExtra("TITLE", "สุขภาพ")
            myIntent.putExtra("CATEGORY", "สุขภาพ")
            startActivity(myIntent)
        }

        binding.care.setOnClickListener {
            var myIntent = Intent(activity, CategoryActivity::class.java)
            myIntent.putExtra("TITLE", "การดูแล")
            myIntent.putExtra("CATEGORY", "การดูแล")
            startActivity(myIntent)
        }

        binding.food.setOnClickListener {
            var myIntent = Intent(activity, CategoryActivity::class.java)
            myIntent.putExtra("TITLE", "อาหาร")
            myIntent.putExtra("CATEGORY", "อาหาร")
            startActivity(myIntent)
        }

        binding.lifeStyle.setOnClickListener {
            var myIntent = Intent(activity, CategoryActivity::class.java)
            myIntent.putExtra("TITLE", "ไลฟ์สไตล์")
            myIntent.putExtra("CATEGORY", "ไลฟ์สไตล์")
            startActivity(myIntent)
        }

        return view
    }

    private fun setUpRecyclerView() {
        dataList = ArrayList()

        binding.rvRecommend.layoutManager = LinearLayoutManager(activity)
        var db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("petdata.db")
            .build()

        var daoObject = db.getDao()
        var petdatas = daoObject.getAll()
        for (i in petdatas!!.indices) {
            dataList.add(petdatas[i]!!)
            rvAdapter = RecommendAdapter(dataList, requireContext())
            binding.rvRecommend.adapter = rvAdapter
        }
    }
}
