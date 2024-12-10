package com.neatroots.newdog

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var signin_link_btn: Button
    private lateinit var signup_btn: Button
    private lateinit var username_signup: EditText
    private lateinit var email_signup: EditText
    private lateinit var password_signup: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        init()

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser != null) {
            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            finish()
        }

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when {
            TextUtils.isEmpty(userName) -> showToast("Username is required")
            TextUtils.isEmpty(email) -> showToast("Email is required")
            TextUtils.isEmpty(password) -> showToast("Password is required")
            else -> {
                // ใช้ Kotlin Coroutines เพื่อดำเนินการแบบ background
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val authResult = mAuth?.createUserWithEmailAndPassword(email, password)?.await()
                        saveUserInfo(userName, email)
                        showToast("Account has been created successfully.")
                        navigateToMainActivity()
                    } catch (e: Exception) {
                        val message = e.message ?: "Unknown error"
                        showToast("Error: $message")
                    }
                }
            }
        }
    }

    private fun init() {
        username_signup = findViewById(R.id.adddog_name)
        email_signup = findViewById(R.id.email_signup_layout)
        signin_link_btn = findViewById(R.id.signin_link_btn)
        signup_btn = findViewById(R.id.save_btn_acc)
        password_signup = findViewById(R.id.pass_signup_layout)
    }

    private fun saveUserInfo(enteredUsername: String, enteredEmail: String) {
        val currentUserID = mAuth?.currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID!!)
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID!!
        userMap["username"] = enteredUsername.toLowerCase()
        userMap["email"] = enteredEmail
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/mydog-568cd.appspot.com/o/Default%20Images%2Fuser%20(4).png?alt=media&token=23350a1f-4bce-4ad5-a3e2-76e7c256df52"


        userRef.setValue(userMap)

            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                }

            }
            .addOnFailureListener { e ->

            }


    }


    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@SignUpActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }
}
