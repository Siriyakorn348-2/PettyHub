package com.neatroots.newdog

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var auth: FirebaseAuth

    private lateinit var backChangePassword: ImageButton
    private lateinit var currentPasswordInput: TextInputEditText
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var updatePasswordBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        init()

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser ?: run {
            Toast.makeText(this, "ไม่พบผู้ใช้ กรุณาเข้าสู่ระบบใหม่", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        backChangePassword.setOnClickListener {
            onBackPressed()
        }

        updatePasswordBtn.setOnClickListener {
            changePassword()
        }
    }

    private fun init() {
        backChangePassword = findViewById(R.id.back_change_password)
        currentPasswordInput = findViewById(R.id.current_password_input)
        newPasswordInput = findViewById(R.id.new_password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        updatePasswordBtn = findViewById(R.id.update_password_btn)
    }

    private fun changePassword() {
        val currentPassword = currentPasswordInput.text.toString().trim()
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // ตรวจสอบว่าฟิลด์ว่างหรือไม่
        when {
            TextUtils.isEmpty(currentPassword) -> {
                Toast.makeText(this, "กรุณากรอกรหัสผ่านปัจจุบัน", Toast.LENGTH_LONG).show()
                return
            }
            TextUtils.isEmpty(newPassword) -> {
                Toast.makeText(this, "กรุณากรอกรหัสผ่านใหม่", Toast.LENGTH_LONG).show()
                return
            }
            TextUtils.isEmpty(confirmPassword) -> {
                Toast.makeText(this, "กรุณายืนยันรหัสผ่านใหม่", Toast.LENGTH_LONG).show()
                return
            }
            newPassword != confirmPassword -> {
                Toast.makeText(this, "รหัสผ่านใหม่และการยืนยันไม่ตรงกัน", Toast.LENGTH_LONG).show()
                return
            }
            newPassword.length < 6 -> {
                Toast.makeText(this, "รหัสผ่านใหม่ต้องมีอย่างน้อย 6 ตัวอักษร", Toast.LENGTH_LONG).show()
                return
            }
        }

        // แสดง ProgressDialog
        val progressDialog = ProgressDialog(this).apply {
            setTitle("เปลี่ยนรหัสผ่าน")
            setMessage("กรุณารอสักครู่ กำลังอัปเดตรหัสผ่าน...")
            setCanceledOnTouchOutside(false)
            show()
        }

        // ตรวจสอบรหัสผ่านปัจจุบันและอัปเดตรหัสผ่านใหม่
        val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, currentPassword)
        firebaseUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                firebaseUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    progressDialog.dismiss()
                    if (updateTask.isSuccessful) {
                        Toast.makeText(this, "อัปเดตรหัสผ่านสำเร็จ", Toast.LENGTH_LONG).show()
                        // กลับไปที่ MainActivity และไปที่ ProfileFragment
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("selectedNavItemId", R.id.nav_profile)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "อัปเดตรหัสผ่านไม่สำเร็จ: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "รหัสผ่านปัจจุบันไม่ถูกต้อง", Toast.LENGTH_LONG).show()
            }
        }
    }
}