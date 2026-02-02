package com.neatroots.newdog

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var login_createBtn: Button
    private lateinit var btn_login: Button
    private lateinit var forgetPass: TextView
    private lateinit var rememberMeCheckBox: CheckBox
    private var userEmail: TextInputEditText? = null
    private var userPass:  TextInputEditText? = null
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()

        mAuth.setLanguageCode("th")

        login_createBtn = findViewById(R.id.login_createBtn)
        btn_login = findViewById(R.id.login_btn)
        forgetPass = findViewById(R.id.forgetpass)
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)

        // โหลดสถานะ "จดจำฉัน" จาก SharedPreferences
        val prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val isRemembered = prefs.getBoolean("rememberMe", false)
        if (isRemembered) {
            rememberMeCheckBox.isChecked = true
            val savedEmail = prefs.getString("email", "")
            val savedPassword = prefs.getString("password", "")
            userEmail?.setText(savedEmail)
            userPass?.setText(savedPassword)
        }

        login_createBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_login.setOnClickListener {
            loginUser()
        }

        forgetPass.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = userEmail?.text.toString()
        val password = userPass?.text.toString()

        when {
            TextUtils.isEmpty(email) -> Toast.makeText(this, "กรุณากรอกอีเมล", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "กรุณากรอกรหัสผ่าน", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this@LoginActivity).apply {
                    setTitle("เข้าสู่ระบบ")
                    setMessage("กรุณารอสักครู่")
                    setCanceledOnTouchOutside(false)
                    show()
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        val prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                        if (rememberMeCheckBox.isChecked) {
                            prefs.edit()
                                .putBoolean("rememberMe", true)
                                .putString("email", email)
                                .putString("password", password)
                                .apply()
                        } else {
                            prefs.edit()
                                .putBoolean("rememberMe", false)
                                .remove("email")
                                .remove("password")
                                .apply()
                        }

                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "การเข้าสู่ระบบล้มเหลว: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun init() {
        userEmail = findViewById(R.id.loginEmailInput)
        userPass = findViewById(R.id.loginPassInput)
    }
}