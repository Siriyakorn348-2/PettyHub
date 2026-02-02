package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DogInfoFragment : Fragment() {

    private lateinit var dogId: String
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dogId = arguments?.getString("dogId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dog_info, container, false)


        val nameText: TextView = view.findViewById(R.id.dogNameText)
        val breedText: TextView = view.findViewById(R.id.dogBreedText)
        val colorText: TextView = view.findViewById(R.id.dogColorText)
        val markingsText: TextView = view.findViewById(R.id.dogMarkingsText)
        val genderText: TextView = view.findViewById(R.id.dogGenderText)
        val birthDateText: TextView = view.findViewById(R.id.dogBirthDateText)
        val ageText: TextView = view.findViewById(R.id.dogAgeText)
        val weightText: TextView = view.findViewById(R.id.dogWeightText)
        val groupText: TextView = view.findViewById(R.id.dogGroupText)
        val neuteredText: TextView = view.findViewById(R.id.dogNeuteredText)


        val dogsRef = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId)
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    nameText.text = snapshot.child("dogName").getValue(String::class.java) ?: "-"
                    breedText.text = snapshot.child("dogBreed").getValue(String::class.java) ?: "-"
                    colorText.text = snapshot.child("dogColor").getValue(String::class.java) ?: "-"
                    markingsText.text = snapshot.child("dogMarkings").getValue(String::class.java) ?: "-"
                    genderText.text = snapshot.child("dogGender").getValue(String::class.java) ?: "-"

                    val birthDate = snapshot.child("dogBirthDate").getValue(Long::class.java) ?: 0L
                    birthDateText.text = convertTimestampToDate(birthDate)
                    ageText.text = calculateAge(birthDate)

                    val weight = snapshot.child("dogWeight").getValue(Double::class.java)
                    weightText.text = if (weight == null || weight == 0.0) "-" else "$weight กก."
                    groupText.text = snapshot.child("dogGroup").getValue(String::class.java) ?: "-"
                    neuteredText.text = if (snapshot.child("dogNeutered").getValue(Boolean::class.java) == true) "ใช่" else "ไม่"

                    Log.d("DogInfoFragment", " Data updated: Breed=${snapshot.child("dogBreed").getValue(String::class.java)}, Gender=${snapshot.child("dogGender").getValue(String::class.java)}, Group=${snapshot.child("dogGroup").getValue(String::class.java)}")
                } else {
                    Log.d("DogInfoFragment", "ไม่พบข้อมูลสำหรับ dogId: $dogId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DogInfoFragment", "ดึงข้อมูลล้มเหลว: ${error.message}")
            }
        }
        dogsRef.addValueEventListener(valueEventListener!!)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let {
            FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId).removeEventListener(it)
        }
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        return if (timestamp == 0L) "-" else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
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

    companion object {
        fun newInstance(dogId: String): DogInfoFragment {
            val fragment = DogInfoFragment()
            val args = Bundle()
            args.putString("dogId", dogId)
            fragment.arguments = args
            return fragment
        }
    }
}