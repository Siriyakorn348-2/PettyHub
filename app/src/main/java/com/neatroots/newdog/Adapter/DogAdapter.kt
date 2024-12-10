package com.neatroots.newdog.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.DogDetailActivity
import com.neatroots.newdog.Fragments.ProfileFragment
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.R
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class DogAdapter(
    private val mContext: Context,
    private var mDog: List<Dog>,
    private val profileFragment: ProfileFragment,

    ) : RecyclerView.Adapter<DogAdapter.ViewHolder>() {



    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dogImg: CircleImageView
        var dogName: TextView

        init {
            dogImg = itemView.findViewById(R.id.dog_img)
            dogName = itemView.findViewById(R.id.dog_name_show)

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
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val dog = mDog[position]


        holder.itemView.setOnClickListener {
            val dogId = dog.getDogId()
            Log.d("MyDogAdapter", "Clicked on dog with ID: $dogId")

            // สร้าง Intent และเพิ่ม dogId เข้าไป
            val intent = Intent(mContext, DogDetailActivity::class.java)
            intent.putExtra("dogId", dogId)

            // เรียก startActivity ด้วย Intent
            mContext.startActivity(intent)
        }

        // ตรวจสอบว่าหมาเป็นของเจ้าของแอคหรือไม่
        if (dog.getUserId() == firebaseUser?.uid) {
            holder.dogName.text = dog.getDogName()

            val imageUrl = dog.getDogImage()
            Log.d("YourTag", "Dog Image URL: $imageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .placeholder(R.drawable.dog)
                    .into(holder.dogImg, object : Callback {
                        override fun onSuccess() {
                            Log.d("YourTag", "Image loaded successfully")
                        }

                        override fun onError(e: Exception?) {
                            Log.e("YourTag", "Failed to load image. Exception: ${e?.message}")
                        }
                    })
            } else {
                holder.dogImg.setImageResource(R.drawable.dog)
            }

            getDogInfo(holder.dogImg, holder.dogName, dog.getUserId())
        } else {

            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }

    private fun getDogInfo(imageDog: CircleImageView, dogName: TextView, userId: String) {
        val dogRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(userId)

        dogRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("YourTag", "DataSnapshot: $dataSnapshot")

                if (dataSnapshot.exists()) {
                    val dog = dataSnapshot.getValue(Dog::class.java)
                    if (dog != null) {
                        Log.d("YourTag", "Dog Data: $dog")

                        // แสดงข้อมูลตัวหน้าหมาที่ถูกคลิก
                        val imageUrl = dog.getDogImage()
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .placeholder(R.drawable.dog)
                                .into(imageDog, object : Callback {
                                    override fun onSuccess() {
                                        Log.d("YourTag", "Image loaded successfully")
                                    }

                                    override fun onError(e: Exception?) {
                                        Log.e("YourTag", "Failed to load image. Exception: ${e?.message}")
                                    }
                                })
                        } else {
                            imageDog.setImageResource(R.drawable.dog)
                        }
                        dogName.text = dog.getDogName()
                    } else {
                        Log.e("YourTag", "Dog is null")
                    }
                } else {
                    Log.d("YourTag", "DataSnapshot does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("YourTag", "Database error: ${error.message}")
            }
        })
    }

    fun updateData(newList: List<Dog>) {
        mDog = newList
        notifyDataSetChanged()
    }
}
