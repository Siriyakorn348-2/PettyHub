package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.neatroots.newdog.R

class MedicalRecordFragment : Fragment() {

    private lateinit var dogId: String
    private lateinit var database: DatabaseReference

    private lateinit var vaccineRecyclerView: RecyclerView
    private lateinit var chronicRecyclerView: RecyclerView
    private lateinit var allergyRecyclerView: RecyclerView
    private lateinit var foodAllergyRecyclerView: RecyclerView

    private lateinit var vaccineAdapter: MedicalRecordAdapter
    private lateinit var chronicAdapter: MedicalRecordAdapter
    private lateinit var allergyAdapter: MedicalRecordAdapter
    private lateinit var foodAllergyAdapter: MedicalRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dogId = arguments?.getString("dogId") ?: ""
        database = FirebaseDatabase.getInstance().reference.child("Dogs").child(dogId).child("MedicalRecords")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medical_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        vaccineRecyclerView = view.findViewById(R.id.vaccineRecyclerView)
        chronicRecyclerView = view.findViewById(R.id.chronicRecyclerView)
        allergyRecyclerView = view.findViewById(R.id.allergyRecyclerView)
        foodAllergyRecyclerView = view.findViewById(R.id.foodAllergyRecyclerView)


        setupRecyclerView(vaccineRecyclerView, "vaccines").also { vaccineAdapter = it }
        setupRecyclerView(chronicRecyclerView, "chronicDiseases").also { chronicAdapter = it }
        setupRecyclerView(allergyRecyclerView, "drugAllergies").also { allergyAdapter = it }
        setupRecyclerView(foodAllergyRecyclerView, "foodAllergies").also { foodAllergyAdapter = it }


        view.findViewById<Button>(R.id.addVaccineButton).setOnClickListener {
            showAddDialog("ประวัติการฉีดวัคซีน", "vaccines", vaccineAdapter)
        }
        view.findViewById<Button>(R.id.addChronicButton).setOnClickListener {
            showAddDialog("โรคประจำตัว", "chronicDiseases", chronicAdapter)
        }
        view.findViewById<Button>(R.id.addAllergyButton).setOnClickListener {
            showAddDialog("แพ้ยา", "drugAllergies", allergyAdapter)
        }
        view.findViewById<Button>(R.id.addFoodAllergyButton).setOnClickListener {
            showAddDialog("แพ้อาหาร", "foodAllergies", foodAllergyAdapter)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, path: String): MedicalRecordAdapter {
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MedicalRecordAdapter(mutableListOf(), database.child(path)) { key ->

            database.child(path).child(key).removeValue()
        }
        recyclerView.adapter = adapter


        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Pair<String, String>>() // Pair(key, value)
                for (data in snapshot.children) {
                    val key = data.key ?: continue
                    val value = data.getValue(String::class.java) ?: continue
                    items.add(Pair(key, value))
                }
                adapter.updateData(items)
                recyclerView.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return adapter
    }

    private fun showAddDialog(title: String, path: String, adapter: MedicalRecordAdapter) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medical, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_medical_input)

        AlertDialog.Builder(requireContext())
            .setTitle("เพิ่ม$title")
            .setView(dialogView)
            .setPositiveButton("บันทึก") { _, _ ->
                val input = editText.text.toString().trim()
                if (input.isNotEmpty()) {
                    val newRef = database.child(path).push()
                    newRef.setValue(input)
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    companion object {
        fun newInstance(dogId: String): MedicalRecordFragment {
            val fragment = MedicalRecordFragment()
            val args = Bundle()
            args.putString("dogId", dogId)
            fragment.arguments = args
            return fragment
        }
    }
}


class MedicalRecordAdapter(
    private val items: MutableList<Pair<String, String>>,
    private val dbRef: DatabaseReference,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.medical_text)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (key, value) = items[position]
        holder.textView.text = value
        holder.deleteButton.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("ลบ")
                .setMessage("คุณต้องการลบ '$value' หรือไม่?")
                .setPositiveButton("ลบ") { _, _ ->
                    onDelete(key)
                }
                .setNegativeButton("ยกเลิก", null)
                .show()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Pair<String, String>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}