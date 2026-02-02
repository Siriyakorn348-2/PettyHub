package com.neatroots.newdog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.inputmethodservice.InputMethodService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.neatroots.newdog.databinding.ActivitySearchDataBinding
import org.w3c.dom.Text

class SearchDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchDataBinding
    private lateinit var rvAdapter:SearchDataAdapter
    private lateinit var dataList:ArrayList<DogData>
    private lateinit var datas:List<DogData?>
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.search.requestFocus()
        var db = Room.databaseBuilder(this@SearchDataActivity, AppDatabase::class.java,"db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("datadog.db")
            .build()

        var daoObject = db.getDao()

        datas = daoObject.getAll()
        setUpRecycleView()
        binding.goBackHome.setOnClickListener {
            finish()
        }
        binding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString()!=""){
                    filterData(p0.toString())
                }else{
                    setUpRecycleView()
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }

    private fun filterData(filterText: String){
        var filterData = ArrayList<DogData>()
        for (i in datas.indices){
            if (datas[i]!!.title.lowercase().contains(filterText.lowercase())){
                filterData.add(datas[i]!!)
            }
            rvAdapter.filterList(filterList = filterData)
        }
    }

    private fun setUpRecycleView() {
        dataList = ArrayList()
        binding.rvSearch.layoutManager = LinearLayoutManager(this)




        for (i in datas!!.indices){
            dataList.add(datas[i]!!)
            rvAdapter=SearchDataAdapter(dataList, this)
            binding.rvSearch.adapter = rvAdapter
        }
    }
}