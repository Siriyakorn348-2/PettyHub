package com.neatroots.newdog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.neatroots.newdog.databinding.SearchRvBinding
import java.util.logging.Filter

class SearchAdapter(var dataList: ArrayList<Petdata>,var context: Context):RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class  ViewHolder(var binding: SearchRvBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view=SearchRvBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun  filterList(filterList: ArrayList<Petdata>){
        dataList=filterList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       Glide.with(context).load(dataList.get(position).img).into(holder.binding.searchRvImg)
        holder.binding.searchRvTxt.text=dataList.get(position).title
        holder.itemView.setOnClickListener {
            var intent = Intent(context,DataPetActivity::class.java)
            intent.putExtra("img",dataList.get(position).img)
            intent.putExtra("title",dataList.get(position).title)
            intent.putExtra("age",dataList.get(position).age)
            intent.putExtra("des",dataList.get(position).des)
            intent.putExtra("breed",dataList.get(position).breed)
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}