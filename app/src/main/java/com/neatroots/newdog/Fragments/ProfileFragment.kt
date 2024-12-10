package com.neatroots.newdog.Fragments


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.neatroots.newdog.AccountActivity
import com.neatroots.newdog.Adapter.DogAdapter
import com.neatroots.newdog.Adapter.MyImageAdapter
import com.neatroots.newdog.AddDogActivity
import com.neatroots.newdog.LoginActivity
import com.neatroots.newdog.Model.Dog
import com.neatroots.newdog.Model.Post
import com.neatroots.newdog.R
import com.neatroots.newdog.Model.User
import com.neatroots.newdog.ShowUsersActivity
import com.squareup.picasso.Picasso
import java.util.Collections




class ProfileFragment : Fragment()  {


    private lateinit var firebaseUser: FirebaseUser
    private var isProfileIdInitialized = false
    private lateinit var profileId: String

    private lateinit var pro_name: TextView
    private lateinit var pro_email: TextView
    private lateinit var pro_dit: Button
    private lateinit var image_dog : ImageView

    private lateinit var total_follow: TextView
    private lateinit var total_following: TextView


    private lateinit var total_posts: TextView

    private lateinit var profile_fragment_username: TextView

    var postList : List<Post>? = null
    var  myImageAdapter : MyImageAdapter? = null

    var postListSaved : List<Post>? = null
    var  myImageAdapterSaveImg : MyImageAdapter? = null
    var mySaveImg : List<String>? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("YourTag", "ProfileFragment onCreateView: Called")


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        //menu
        val optionsView: ImageView = view.findViewById(R.id.options_view)
        optionsView.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.acc_menu, popupMenu.menu)


            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_editpro -> {
                        // กระทำเมื่อคลิกที่ "Edit Profile"
                        startActivity(Intent(requireContext(), AccountActivity::class.java))
                        true
                    }
                    R.id.nav_logout -> {
                        // กระทำเมื่อคลิกที่ "Logout"
                        val alertDialog = AlertDialog.Builder(requireContext())
                        alertDialog.setTitle("Confirmation")
                        alertDialog.setMessage("Are you sure you want to logout?")


                        alertDialog.setPositiveButton("Yes") { _, _ ->
                            // กระทำเมื่อผู้ใช้กด "Yes"
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finish()
                        }


                        alertDialog.setNegativeButton("No") { dialog, _ ->
                            // กระทำเมื่อผู้ใช้กด "No"
                            dialog.dismiss()
                        }


                        alertDialog.show()


                        true
                    }
                    else -> false
                }
            }


            popupMenu.show()
        }


        profile_fragment_username = view.findViewById(R.id.profile_fragment_user)
        total_follow = view.findViewById(R.id.total_follow)
        total_following = view.findViewById(R.id.total_following)


        pro_dit = view.findViewById(R.id.edit_pro)
        total_posts = view.findViewById(R.id.total_posts)

        pro_name = view.findViewById(R.id.username)
        pro_email = view.findViewById(R.id.email_pro)
        image_dog = view.findViewById(R.id.image_dog)


        //upload image
        var recyclerViewUploadImage: RecyclerView
        recyclerViewUploadImage = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImage.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImage.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImageAdapter = context?.let { MyImageAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadImage.adapter = myImageAdapter

        //save image
        var recyclerViewSavedImage : RecyclerView
        recyclerViewSavedImage = view.findViewById(R.id.recycler_view_save_pic)
        val linearLayoutManager2 : LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewSavedImage.layoutManager = linearLayoutManager2
        recyclerViewSavedImage.setHasFixedSize(true)

        postListSaved = ArrayList()
        myImageAdapterSaveImg = context?.let { MyImageAdapter(it, postListSaved as ArrayList<Post>) }
        recyclerViewSavedImage.adapter = myImageAdapterSaveImg

        recyclerViewSavedImage.visibility = View.GONE
        recyclerViewUploadImage.visibility = View.VISIBLE


        val  uploadedImageBtn : ImageButton
        uploadedImageBtn = view.findViewById(R.id.upload_btn_image)
        uploadedImageBtn.setOnClickListener{
            recyclerViewSavedImage.visibility = View.GONE
            recyclerViewUploadImage.visibility = View.VISIBLE


        }

        val  saveImageBtn : ImageButton
        saveImageBtn = view.findViewById(R.id.save_btn_image)
        saveImageBtn.setOnClickListener{
            recyclerViewSavedImage.visibility = View.VISIBLE
            recyclerViewUploadImage.visibility = View.GONE


        }

        total_follow.setOnClickListener{
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","followers")
            startActivity(intent)
        }
        total_following.setOnClickListener{
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","following")
            startActivity(intent)
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if(pref != null)
        {
            this.profileId = pref.getString("profileId","none").toString()

        }

        if (profileId == firebaseUser.uid) {
            pro_dit.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid){
            checkFollowAndFollowingButtonStatus() {
                userInfo()
            }
        }


        pro_dit.setOnClickListener {
            val getButtonText = pro_dit.text.toString()
            when {
                getButtonText == "Edit Profile" -> startActivity(Intent(requireContext(), AccountActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser.uid?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1)
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser.uid?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1)
                            .setValue(true)
                    }
                    addNotification()
                }
                getButtonText == "Following" -> {
                    firebaseUser.uid?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1)
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser.uid?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1)
                            .removeValue()
                    }
                }
            }
        }

        getProfileId()
        getFollowers()
        getFollowing()
        userInfo()
        myPhotor()
        getTotalNumberOfPosts()
        mySaves()

        return view
    }


    private fun myPhotor(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(pO: DataSnapshot) {
                if(pO.exists()){
                    (postList as ArrayList<Post>).clear()
                    for (snapshot in pO.children)
                    {
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.getPublisher().equals(profileId))
                        {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImageAdapter!!.notifyDataSetChanged()
                    }
                }
            }


            override fun onCancelled(pO: DatabaseError) {
            }
        })
    }


    private fun checkFollowAndFollowingButtonStatus(function: () -> Unit) {
        if (!isProfileIdInitialized) {
            Log.e("YourTag", "profileId has not been initialized")
            return
        }


        val followingRef = firebaseUser.uid?.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1)
                .child("Following")
        }
        if (followingRef == null) {


            followingRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(pO: DataSnapshot) {
                    if (pO.child(profileId).exists()) {
                        pro_dit.text = "Following"
                    } else {
                        pro_dit.text = "Follow"
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled event if needed
                }


            })
        }
    }


    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")


        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    total_follow.text = pO.childrenCount.toString()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }


    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")


        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    total_following.text = pO.childrenCount.toString()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        val imageUrl = user.getImage()
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.user).into(image_dog)
                        } else {
                            // จัดการกรณีที่ URL รูปภาพว่างหรือเป็น null
                            image_dog.setImageResource(R.drawable.dog)
                            // หรือ
                            // image_dog.visibility = View.GONE
                        }

                        profile_fragment_username.text = user.getUsername()
                        pro_name.text = user.getUsername()
                        pro_email.text = user.getEmail()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("YourTag", "userInfo onCancelled: $databaseError")
                // Handle onCancelled event if needed
            }
        })
    }

    private fun getProfileId(): String {
        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        var profileId = pref.getString("profileId", "") ?: ""
        if (profileId.isEmpty()) {
            if (firebaseUser.uid != null) {
                profileId = firebaseUser.uid
                // Save profileId to SharedPreferences
                pref.edit().putString("profileId", profileId).apply()
            } else {
                Log.e("YourTag", "FirebaseUser uid is null")
            }
        }
        return profileId
    }


    private fun getTotalNumberOfPosts()
    {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")


        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    var postCount = 0


                    for (snapShot in dataSnapshot.children)
                    {
                        val post = snapShot.getValue(Post::class.java)!!


                        if (post.getPublisher() == profileId)
                        {
                            postCount++
                        }
                    }
                    total_posts.text = " " + postCount
                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun mySaves()
    {
        mySaveImg = ArrayList()


        val savedRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser.uid)


        savedRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children)
                    {
                        (mySaveImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImageData()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readSavedImageData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (postListSaved as ArrayList<String>).clear()
                }


                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for (key in mySaveImg!!) {
                        if (post!!.getPostid() == key) {
                            (postListSaved as ArrayList<Post>).add(post!!)
                        }
                    }
                }
                myImageAdapterSaveImg!!.notifyDataSetChanged()


            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "start following you"
        notiMap["postid"]= ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

    override fun onStop() {
        super.onStop()
        if (firebaseUser != null && firebaseUser.uid != null) {
            val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", firebaseUser.uid)
            pref.apply()
        }
    }


    override fun onPause() {
        super.onPause()
        if (firebaseUser != null && firebaseUser.uid != null) {
            val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", firebaseUser.uid)
            pref.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (firebaseUser != null && firebaseUser.uid != null) {
            val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", firebaseUser.uid)
            pref.apply()
        }
    }

}
