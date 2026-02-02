package com.neatroots.newdog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter.FilterListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.neatroots.newdog.databinding.SearchRvBinding

class SearchDataAdapter(var dataList: ArrayList<DogData>, var context: Context):RecyclerView.Adapter<SearchDataAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: SearchRvBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = SearchRvBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataChanged")
    fun filterList(filterList: ArrayList<DogData>){
        dataList = filterList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(dataList.get(position).img).into(holder.binding.searchImg)
        holder.binding.searchTxt.text = dataList.get(position).title
        holder.itemView.setOnClickListener{
            var intent = Intent(context, ContentActivity::class.java)
            intent.putExtra("img", dataList.get(position).img)
            intent.putExtra("title",dataList.get(position).title)
            intent.putExtra("des",dataList.get(position).des)
            intent.putExtra("age",dataList.get(position).age)
            intent.putExtra("breed",dataList.get(position).breed)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}