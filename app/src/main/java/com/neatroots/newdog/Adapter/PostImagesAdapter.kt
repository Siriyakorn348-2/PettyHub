package com.neatroots.newdog.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neatroots.newdog.R
import com.neatroots.newdog.databinding.ImageItemLayoutBinding
import com.squareup.picasso.Picasso

class PostImagesAdapter(
    private val context: Context,
    private val imageList: List<String>
) : RecyclerView.Adapter<PostImagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageList[position]
        Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(holder.binding.imageView)
    }

    override fun getItemCount(): Int = imageList.size

    class ViewHolder(val binding: ImageItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}