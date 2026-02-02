package com.neatroots.newdog.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
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
    private lateinit var searchEditText: EditText
    private lateinit var backButton: ImageButton
    private var searchListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        backButton = view.findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        searchEditText = view.findViewById(R.id.search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    mUser?.clear()
                    userAdapter?.notifyDataSetChanged()
                    recyclerView?.visibility = View.GONE
                    removeSearchListener()
                } else {
                    recyclerView?.visibility = View.VISIBLE
                    searchUser(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun searchUser(input: String) {
        removeSearchListener()

        val query = FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt("$input\uf8ff")

        searchListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("SearchFragment", "Search result: ${snapshot.childrenCount} users found")
                val newList = mutableListOf<User>()

                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null && user.isValid()) {
                        newList.add(user)
                    } else {
                        Log.e("SearchFragment", "Invalid user data: ${data.value}")
                    }
                }

                mUser?.clear()
                mUser?.addAll(newList)
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchFragment", "Search failed: ${error.message}")
            }
        }.also {
            query.addValueEventListener(it)
        }
    }

    private fun removeSearchListener() {
        searchListener?.let {
            FirebaseDatabase.getInstance().getReference("Users").removeEventListener(it)
            searchListener = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeSearchListener()
    }

}