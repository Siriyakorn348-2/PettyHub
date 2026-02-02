package com.neatroots.newdog

import android.app.ProgressDialog
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
    private lateinit var confirm_password_signup: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        init()

        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser != null) {
            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            finish()
            return
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage("กำลังสมัครสมาชิก...")
            setCancelable(false)
        }

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun init() {
        username_signup = findViewById(R.id.adddog_name)
        email_signup = findViewById(R.id.email_signup_layout)
        password_signup = findViewById(R.id.pass_signup_layout)
        confirm_password_signup = findViewById(R.id.confirm_pass_signup_layout)
        signin_link_btn = findViewById(R.id.signin_link_btn)
        signup_btn = findViewById(R.id.save_btn_acc)
    }

    private fun createAccount() {
        val userName = username_signup.text.toString().trim()
        val email = email_signup.text.toString().trim()
        val password = password_signup.text.toString().trim()
        val confirmPassword = confirm_password_signup.text.toString().trim()

        when {
            TextUtils.isEmpty(userName) -> showToast("Username is required")
            TextUtils.isEmpty(email) -> showToast("Email is required")
            TextUtils.isEmpty(password) -> showToast("Password is required")
            TextUtils.isEmpty(confirmPassword) -> showToast("Confirm Password is required")
            password != confirmPassword -> showToast("Passwords do not match")
            else -> {
                progressDialog.show()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val authResult = mAuth?.createUserWithEmailAndPassword(email, password)?.await()
                        if (authResult != null) {
                            saveUserInfo(userName, email)
                        } else {
                            showToast("Failed to create account")
                        }
                    } catch (e: Exception) {
                        val message = e.message ?: "Unknown error"
                        showToast("Error: $message")
                    } finally {
                        runOnUiThread { progressDialog.dismiss() }
                    }
                }
            }
        }
    }

    private fun saveUserInfo(enteredUsername: String, enteredEmail: String) {
        val currentUserID = mAuth?.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID)
        val userMap = HashMap<String, Any>().apply {
            put("uid", currentUserID)
            put("username", enteredUsername.toLowerCase())
            put("email", enteredEmail)
            put("image", "https://firebasestorage.googleapis.com/v0/b/mydog-568cd.appspot.com/o/Default%20Images%2Fuser%20(4).png?alt=media&token=23350a1f-4bce-4ad5-a3e2-76e7c256df52")
        }

        userRef.setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)
                        .addOnCompleteListener {
                            showToast("Account has been created successfully")
                            navigateToDogExperienceActivity()
                        }
                } else {
                    showToast("Failed to save user info: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to save user info: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    private fun navigateToDogExperienceActivity() {
        val intent = Intent(this@SignUpActivity, DogExperienceActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }
}