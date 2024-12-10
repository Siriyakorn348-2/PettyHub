package com.neatroots.newdog

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.neatroots.newdog.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var rvAdapter: SearchAdapter
    private lateinit var dataList:ArrayList<Petdata>
    private lateinit var petdatas:List<Petdata?>

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySearchBinding.inflate(layoutInflater)
        binding.goHome.setOnClickListener {
            finish()
        }
        setContentView(binding.root)
        binding.search.requestFocus()
        var db= Room.databaseBuilder(this@SearchActivity, AppDatabase::class.java,"db_name")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("petdata.db")
            .build()
        var daoObject=db.getDao()
        petdatas = daoObject.getAll()!!
        setUpRecyclerView()
        binding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               if (s.toString()!=""){
                   filterData(s.toString())
               }else{
                   setUpRecyclerView()
               }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }
    private fun filterData(filterText: String) {
        var filterData=ArrayList<Petdata>()
        for (i in petdatas.indices){
            if (petdatas[i]!!.title.lowercase().contains(filterText.lowercase())){
                filterData.add(petdatas[i]!!)
            }
            rvAdapter.filterList(filterList = filterData)
        }

    }

    private fun setUpRecyclerView() {
        dataList= ArrayList()

        binding.rvSearch.layoutManager= LinearLayoutManager(this)

        for (i in petdatas!!.indices){
            dataList.add(petdatas[i]!!)
            rvAdapter= SearchAdapter(dataList,this)
            binding.rvSearch.adapter=rvAdapter
        }

    }
}