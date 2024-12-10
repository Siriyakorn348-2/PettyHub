package com.neatroots.pettyhub.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.DogAdapter
import com.neatroots.newdog.AddDogActivity
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.R

class DogFragment : Fragment() {
    private var dogAdapter: DogAdapter? = null
    private var dogList: MutableList<Dog> = mutableListOf() // ใช้ mutableListOf() แทน null
    private lateinit var dogRecyclerView: RecyclerView // ประกาศ RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_dog, container, false)
        val addDogButton: FloatingActionButton = view.findViewById(R.id.addDog)



        // เมื่อกดปุ่ม FloatingActionButton
        addDogButton.setOnClickListener {
            // เรียก Intent ไปยังหน้า AddDogActivity
            val intent = Intent(requireContext(), AddDogActivity::class.java)
            startActivity(intent)
        }


        return view
    }


}
