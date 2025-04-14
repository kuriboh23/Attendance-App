package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivityLoginBinding
import com.example.project.databinding.ActivitySignupBinding

class SignUp:AppCompatActivity() {

    lateinit var back:ImageView
    lateinit var binding:ActivitySignupBinding

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        back = findViewById(R.id.arrowLeft)

        back.setOnClickListener {
            finish()
        }

        binding.signupBtn.setOnClickListener {
            val firstName = binding.nameInput.text.toString()
            val lastName = binding.lastNameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordTxt.text.toString()
            val confirmPassword = binding.confirmPasswordTxt.text.toString()
            val role = "user"
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (password == confirmPassword){
                    val user = User(0, firstName, lastName, email, password, role)
                    insertUser(user)
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun insertUser(user: User) {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.insertUser(user)
        Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()
    }
}