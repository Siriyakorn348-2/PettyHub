package com.neatroots.newdog

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.widget.AppCompatImageView
import com.google.firebase.auth.FirebaseAuth

class BarProfileActivity : AppCompatActivity() {
    lateinit var mContext: Context

    var mAuth: FirebaseAuth? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null


    var username_bar_pro: TextView? = null
    var email_bar_pro : TextView? = null
    var logout_acc: Button? = null
    var edit_bar_acc: Button? = null
    var back_bar_pro : ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_profile)


        init() // เพิ่มบรรทัดนี้

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth!!.currentUser
//นําค่ามาใส่ลงใน TextView ที5สร้างขึIน
        username_bar_pro?.text =""+ user!!.displayName
        email_bar_pro?.text = "" + user.email
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // ตรวจสอบความถูกต้องของ user และ displayName
            val user = mAuth!!.currentUser
            Log.d("YourTag", "User Display Name: ${user?.displayName}")

            if (user != null && user.displayName != null) {
                username_bar_pro?.text = "" + user.displayName
                email_bar_pro?.text = "" + user.email
            } else {
                // กรณีไม่มี displayName หรือ user ไม่ถูกต้อง
                username_bar_pro?.text = "N/A"
                email_bar_pro?.text = "N/A"
            }

        }

        edit_bar_acc?.setOnClickListener {
                val intent = Intent(this@BarProfileActivity, AccountActivity::class.java)
                startActivity(intent)
        }



        logout_acc?.setOnClickListener {
            mAuth!!.signOut()
            Toast.makeText(this, "Signed out!", Toast.LENGTH_LONG).show()

            // เพิ่ม Log เพื่อตรวจสอบว่าถูกเรียกหรือไม่
            Log.d("Logout", "Logout button clicked")

            // เมื่อคลิก Logout ให้เปิดหน้าล็อกอิน (LoginActivity)
            startActivity(Intent(this@BarProfileActivity, LoginActivity::class.java))
            finish()
        }


        // กรณีกดปุ่ม Back
        back_bar_pro?.setOnClickListener { onBackPressed() }
    }
    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener { mAuthListener }
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener { mAuthListener }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { moveTaskToBack(true) }
        return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("WrongViewCast")
    fun init() {
        username_bar_pro = findViewById(R.id.username_acc)
        email_bar_pro = findViewById(R.id.email_acc)

        edit_bar_acc = findViewById(R.id.edit_proAcc)

        back_bar_pro = findViewById(R.id.back_acc) as ImageButton?

        logout_acc = findViewById(R.id.logout_acc)

    }
}
