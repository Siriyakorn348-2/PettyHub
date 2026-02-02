package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Fragments.DogInfoFragment
import com.neatroots.newdog.Fragments.MedicalRecordFragment
import com.neatroots.newdog.databinding.ActivityDogProfileBinding
import com.squareup.picasso.Picasso
import java.util.*

class DogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDogProfileBinding
    private lateinit var dogId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // รับ dogId จาก Intent (ส่งมาจาก DogAdapter)
        dogId = intent.getStringExtra("dogId") ?: ""
        if (dogId.isEmpty()) {
            finish()
            return
        }

        // ตั้งค่า Toolbar
        setSupportActionBar(binding.proToolbar)

        binding.back.setOnClickListener { finish() }

        // ดึงข้อมูลพื้นฐานจาก Firebase
        fetchBasicDogInfo()

        // ตั้งค่า ViewPager2 และ TabLayout
        val adapter = DogProfilePagerAdapter(this, dogId)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabInfo, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "ข้อมูลทั่วไป"
                1 -> "ประวัติการแพทย์"
                else -> null
            }
        }.attach()

        // Listener ปุ่มแก้ไขข้อมูล
        binding.editPro.setOnClickListener {
            val intent = Intent(this, EditDogInfoActivity::class.java)
            intent.putExtra("dogId", dogId)
            startActivity(intent)
        }

    }

    private fun fetchBasicDogInfo() {
        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        dogsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.nameDog.text = snapshot.child("dogName").getValue(String::class.java) ?: "-"
                    binding.breed.text = snapshot.child("dogBreed").getValue(String::class.java) ?: "-"
                    binding.dogGender.text = snapshot.child("dogGender").getValue(String::class.java) ?: "-"
                    val weight = snapshot.child("dogWeight").getValue(Double::class.java)
                    binding.dogWeight.text = if (weight == null || weight == 0.0) "-" else "$weight กก."

                    val birthDate = snapshot.child("dogBirthDate").getValue(Long::class.java) ?: 0L
                    binding.dogAge.text = calculateAge(birthDate)

                    val imageUrl = snapshot.child("dogImage").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(binding.imageDog)
                    } else {
                        binding.imageDog.setImageResource(R.drawable.dog)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // จัดการข้อผิดพลาด
            }
        })
    }

    private fun calculateAge(birthDate: Long): String {
        if (birthDate == 0L) return "-"
        val birthCal = Calendar.getInstance().apply { timeInMillis = birthDate }
        val now = Calendar.getInstance()
        var years = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
        if (months < 0) {
            years--
            months += 12
        }
        return if (years > 0) "$years ปี $months เดือน" else "$months เดือน"
    }
}

class DogProfilePagerAdapter(
    activity: AppCompatActivity,
    private val dogId: String
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DogInfoFragment.newInstance(dogId)
            1 -> MedicalRecordFragment.newInstance(dogId)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}