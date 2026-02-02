package com.neatroots.newdog.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso

class ImageListAdapter(
    private val mContext: Context,
    private val mImageUrls: List<String>
) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = mImageUrls[position]
        Picasso.get().load(imageUrl).placeholder(R.drawable.addimage).into(holder.imageView)
    }

    override fun getItemCount(): Int = mImageUrls.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }
}