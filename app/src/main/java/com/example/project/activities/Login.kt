package com.example.project.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivityLoginBinding
import com.example.project.function.function.showCustomToast

class Login:AppCompatActivity() {

//    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityLoginBinding

    lateinit var userViewModel: UserViewModel

//    lateinit var back:ImageView
//    lateinit var login: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        back = findViewById(R.id.arrowLeft)
//        login = findViewById(R.id.signIn_btn)

        binding.arrowLeft.setOnClickListener {
            finish()
        }

    binding.signUpLink.setOnClickListener {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }

    binding.signInBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.txPassword.text.toString()
        if (isValidEmail(email)) {
            getUserByEmail(email,password)
        }else {
            this.showCustomToast("Invalid Email", R.layout.error_toast)
            binding.emailInput.text?.clear()
            binding.emailInput.requestFocus()
        }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getUserByEmail(email: String, password: String) {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.allUsers.observe(this) { users ->
            val user = users.find { it.email == email }
            if (user != null) {
                // User found, proceed with login
                if (user.password == password) {
                    val savedUserId = user.id
                    UserPrefs.saveUserId(this, savedUserId)
                    if(binding.privacyChekbox.isChecked){
                        UserPrefs.savedIsLoggedIn(this, true)
                        UserPrefs.saveUserRole(this, user.role)
                    }
                    // Login successful, navigate to the next activity
                    if (user.role == "user"){
                        UserPrefs.saveUserRole(this, user.role)
                    val intent = Intent(this, HomeActivity::class.java)
                    this.showCustomToast("Login Successful", R.layout.success_toast)
                    startActivity(intent)
                    finish()
                    }else{
                        val intent = Intent(this, AdminHomeActivity::class.java)
                        this.showCustomToast("Login Successful", R.layout.success_toast)
                        startActivity(intent)
                        finish()
                    }
                }
                else{
                    this.showCustomToast("Incorrect password", R.layout.error_toast)
                    binding.txPassword.text?.clear()
                    binding.txPassword.requestFocus()
                }
            }
            else{
                this.showCustomToast("User not found", R.layout.error_toast)
                finish()
            }
        }
    }
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}