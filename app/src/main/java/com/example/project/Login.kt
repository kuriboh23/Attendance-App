package com.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.data.CheckViewModel
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
            val email = binding.emailInput.text.toString()
            val password = binding.txPassword.text.toString()
            getUserByEmail(email,password)
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

                    // Login successful, navigate to the next activity
                    val intent = Intent(this, HomeActivity::class.java)
                    this.showCustomToast("Login Successful",R.layout.custom_toast)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                    binding.txPassword.text?.clear()
                    binding.txPassword.requestFocus()
                }
            }
            else{
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
}