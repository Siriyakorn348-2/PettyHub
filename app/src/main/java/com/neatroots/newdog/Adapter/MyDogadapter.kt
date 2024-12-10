package com.neatroots.newdog.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.neatroots.newdog.DogDetailActivity
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso

class MyDogAdapter(private val mContext: Context, private val mDog: List<Dog>) :
    RecyclerView.Adapter<MyDogAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dogImage: ImageView

        init {
            dogImage = itemView.findViewById(R.id.dog_img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.dog_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDog.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dog: Dog = mDog[position]
        val imageUrl = dog.getDogImage()

        // Load dog image using Picasso
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.dogImage)
        } else {
            // If imageUrl is empty, you can set a placeholder image or handle it as needed
            holder.dogImage.setImageResource(R.drawable.dog)

        }

        holder.dogImage.setOnClickListener {
            val dogId = dog.getDogId()

            // สร้าง Intent และเพิ่ม dogId เข้าไป
            val intent = Intent(mContext, DogDetailActivity::class.java)
            intent.putExtra("dogId", dogId)

            // เรียก startActivity ด้วย Intent
            mContext.startActivity(intent)
        }
    }
}
