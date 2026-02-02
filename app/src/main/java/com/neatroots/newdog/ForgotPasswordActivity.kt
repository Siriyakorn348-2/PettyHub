package com.neatroots.newdog

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth = FirebaseAuth.getInstance()
        mAuth.setLanguageCode("th")

        emailEditText = findViewById(R.id.forgot_email)
        resetButton = findViewById(R.id.reset_password_button)
        toolbar = findViewById(R.id.toolbar)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        resetButton.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email = emailEditText.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "กรุณากรอกอีเมลของคุณ", Toast.LENGTH_LONG).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setTitle("รีเซ็ตรหัสผ่าน")
            setMessage("กำลังส่งอีเมลรีเซ็ต...")
            setCanceledOnTouchOutside(false)
            show()
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                Log.d("ResetPassword", "Task successful: ${task.isSuccessful}, Exception: ${task.exception?.message}")

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "ส่งอีเมลรีเซ็ตรหัสผ่านเรียบร้อย กรุณาตรวจสอบกล่องจดหมาย!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "ส่งอีเมลรีเซ็ตล้มเหลว: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}