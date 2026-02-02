package com.neatroots.newdog.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.DogProfileActivity
import com.neatroots.newdog.EditDogActivity
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.R
import com.squareup.picasso.Picasso
import java.util.Calendar

class DogAdapter(
    private val context: Context,
    private val dogList: MutableList<Dog>
) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dogImageView: ImageView = itemView.findViewById(R.id.img)
        val nameTextView: TextView = itemView.findViewById(R.id.title)
        val genderTextView: TextView = itemView.findViewById(R.id.gender_value)
        val breedTextView: TextView = itemView.findViewById(R.id.breed)
        val menuButton: ImageButton = itemView.findViewById(R.id.menu_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("DogAdapter", "จำนวนรายการ: ${dogList.size}")
        return dogList.size
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogList[position]
        Log.d("DogAdapter", "กำลังผูกข้อมูล: ตำแหน่ง=$position, ชื่อ=${dog.getDogName()}, สายพันธุ์=${dog.getDogBreed()}")


        val dogImageUrl = dog.getDogImage()
        if (!dogImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(dogImageUrl)
                .placeholder(R.drawable.dog)
                .into(holder.dogImageView)
        } else {
            holder.dogImageView.setImageResource(R.drawable.dog)
        }


        holder.nameTextView.text = "${dog.getDogName() ?: "ไม่ทราบชื่อ"}"


        val genderText = "เพศ       ${dog.getDogGender() ?: "ไม่ระบุ"}"
        val genderSpannable = SpannableString(genderText)
        genderSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.brow_light)),
            0,
            "เพศ       ".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.genderTextView.text = genderSpannable


        val breedText = "สายพันธุ์   ${dog.getDogBreed() ?: "ไม่ระบุ"}"
        val breedSpannable = SpannableString(breedText)
        breedSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.brow_light)),
            0,
            "สายพันธุ์   ".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.breedTextView.text = breedSpannable


        holder.itemView.setOnClickListener {
            val intent = Intent(context, DogProfileActivity::class.java)
            intent.putExtra("dogId", dog.getDogId())
            context.startActivity(intent)
        }


        holder.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.menuButton)
            popupMenu.menu.add("ลบ")
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "ลบ" -> {
                        AlertDialog.Builder(context)
                            .setTitle("ลบข้อมูลสุนัข")
                            .setMessage("คุณแน่ใจหรือไม่ที่จะลบ ${dog.getDogName()}?")
                            .setPositiveButton("ใช่") { _, _ ->
                                deleteDog(dog.getDogId(), position)
                            }
                            .setNegativeButton("ไม่") { dialog, _ -> dialog.dismiss() }
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun calculateAge(birthDate: Long): String {
        if (birthDate == 0L) return "ไม่ระบุ"

        val currentTime = Calendar.getInstance()
        val birthTime = Calendar.getInstance().apply { timeInMillis = birthDate }

        var years = currentTime.get(Calendar.YEAR) - birthTime.get(Calendar.YEAR)
        var months = currentTime.get(Calendar.MONTH) - birthTime.get(Calendar.MONTH)
        val daysInMonth = currentTime.getActualMaximum(Calendar.DAY_OF_MONTH)
        var days = currentTime.get(Calendar.DAY_OF_MONTH) - birthTime.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            days += daysInMonth
        }
        if (months < 0) {
            years--
            months += 12
        }

        return when {
            years > 0 -> "$years ปี $months เดือน"
            months > 0 -> "$months เดือน $days วัน"
            else -> "$days วัน"
        }
    }

    private fun deleteDog(dogId: String, position: Int) {
        if (position < 0 || position >= dogList.size) {
            Log.e("DogAdapter", "ตำแหน่งไม่ถูกต้อง: $position, ขนาดลิสต์: ${dogList.size}")
            return
        }

        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        dogsRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("DogAdapter", "ลบข้อมูลสุนัขสำเร็จ: $dogId")
                if (position < dogList.size) {
                    dogList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, dogList.size)
                }
            } else {
                Log.e("DogAdapter", "ลบข้อมูลล้มเหลว: ${task.exception?.message}")
            }
        }
    }
}