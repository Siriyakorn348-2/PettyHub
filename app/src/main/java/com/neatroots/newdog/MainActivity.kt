package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.neatroots.newdog.Fragments.ProfileFragment
import com.neatroots.newdog.Fragments.HomeFragment
import com.neatroots.newdog.Fragments.IdeaFragment
import com.neatroots.pettyhub.Fragment.DogFragment

class MainActivity : AppCompatActivity() {



    private var homeFragment: HomeFragment<Any?>? = null
    private var ideaFragment: IdeaFragment? = null
    private var profileFragment: ProfileFragment? = null
    private var dogFragment: DogFragment? = null


    private var selectedFragment: Fragment? = null


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                moveToFragmant(HomeFragment<Any>())
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_idea-> {
                moveToFragmant(IdeaFragment())
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_addDog -> {
                moveToFragmant(DogFragment())
                selectedFragment = DogFragment()
            }
            R.id.nav_profile-> {
                moveToFragmant(ProfileFragment())
                return@OnNavigationItemSelectedListener true

            }
        }



        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        moveToFragmant(HomeFragment<Any>()
        )

    }

    private fun moveToFragmant(fragment: Fragment){
        val fragmentTrans =supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container,fragment)
        fragmentTrans.commit()
    }

}
