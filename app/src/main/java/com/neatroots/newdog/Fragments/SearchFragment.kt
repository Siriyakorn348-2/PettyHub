package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.Adapter.UserAdapter
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.R


class SearchFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null
    private lateinit var search_edit_text: EditText
    private lateinit  var back_comment : ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = requireContext().applicationContext.let { UserAdapter(it, mUser as ArrayList<User>, true) }

        recyclerView?.adapter = userAdapter

        back_comment = view.findViewById(R.id.back_btn)
        back_comment!!.setOnClickListener {
            requireActivity().onBackPressed()
        }

        search_edit_text = view.findViewById(R.id.search_edit_text)
        search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edit_text.text.toString() == "") {
                } else {
                    recyclerView?.visibility = View.VISIBLE

                    Log.d("YourTag", "Calling retrieveUser()")
                    retrieveUser()
                    Log.d("YourTag", "Calling searchUser()")
                    searchUser(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("YourTag", "searchUser onDataChange: $dataSnapshot")
                Log.d("YourTag", "searchUser onDataChange: Called")

                val newList = mutableListOf<User>()

                for (snapshot in dataSnapshot.children) {

                    val userData = snapshot.getValue()
                    if (userData is String) {

                        continue
                    }

                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.isValid()) {
                        newList.add(user)
                    } else {
                        Log.e("YourTag", "Invalid User data: ${snapshot.value}")
                    }
                }

                mUser?.clear()
                mUser?.addAll(newList)

                userAdapter?.notifyDataSetChanged()
                Log.d("YourTag", "searchUser Adapter notifyDataSetChanged called")
            }

            override fun onCancelled(pO: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun retrieveUser() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("YourTag", "retrieveUser onDataChange: $dataSnapshot")
                Log.d("YourTag", "retrieveUser onDataChange: Called")

                val newList = mutableListOf<User>()

                for (snapshot in dataSnapshot.children) {
                    // ตรวจสอบว่าข้อมูลที่ได้เป็น String หรือไม่
                    val userData = snapshot.getValue()
                    if (userData is String) {
                        // ข้ามข้อมูลที่เป็น String
                        continue
                    }

                    val user = snapshot.getValue(User::class.java)
                    if (user != null && user.isValid()) {
                        newList.add(user)
                    } else {
                        Log.e("YourTag", "Invalid User data: ${snapshot.value}")
                    }
                }

                mUser?.clear()
                mUser?.addAll(newList)

                userAdapter?.notifyDataSetChanged()
                Log.d("YourTag", "retrieveUser Adapter notifyDataSetChanged called")
            }

            override fun onCancelled(error: DatabaseError) {
                val errorMessage = error.toException().message
                Log.e("YourTag", "retrieveUser onCancelled: $errorMessage")
                Log.e("YourTag", "retrieveUser onCancelled - Full Details: $error")
            }
        })
    }
}