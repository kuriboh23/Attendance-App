package com.example.project.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivitySignupBinding
import com.example.project.function.function.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp:AppCompatActivity() {

    lateinit var back:ImageView
    lateinit var binding:ActivitySignupBinding

    lateinit var firebaseRef : DatabaseReference
    lateinit var auth: FirebaseAuth

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseRef = FirebaseDatabase.getInstance().getReference("User")
        auth = FirebaseAuth.getInstance()

        back = findViewById(R.id.arrowLeft)

        back.setOnClickListener {
            finish()
        }
        binding.loginLink.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        binding.signupBtn.setOnClickListener {
            val firstName = binding.nameInput.text.toString().trim()
            val lastName = binding.lastNameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordTxt.text.toString()
            val confirmPassword = binding.confirmPasswordTxt.text.toString()
            val isCheckboxChecked = binding.privacyChekbox.isChecked
            val role = "user"

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (isValidEmail(email)) {
                    if (password == confirmPassword) {
                        if (!isCheckboxChecked) {
                            this.showCustomToast("Please accept the privacy policy", R.layout.error_toast)
                            return@setOnClickListener
                        }

                        // Register with Firebase Auth
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser: FirebaseUser? = auth.currentUser
                                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener

                                    val user = User(0, firstName, lastName, email, password, role, uid)

                                    // Save to Realtime Database
                                    firebaseRef.child(uid).setValue(user)
                                        .addOnSuccessListener {
                                            this.showCustomToast("User registered successfully", R.layout.success_toast)
                                            val intent = Intent(this, Login::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            this.showCustomToast("Failed to save user to database", R.layout.error_toast)
                                        }

                                    // Save to Room DB if needed
                                    insertUser(user)
                                } else {
                                    this.showCustomToast("Registration failed: ${task.exception?.message}", R.layout.error_toast)
                                }
                            }
                    } else {
                        this.showCustomToast("Passwords do not match", R.layout.error_toast)
                    }
                } else {
                    this.showCustomToast("Invalid Email", R.layout.error_toast)
                }
            }
        }

    }

    private fun insertUser(user: User) {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.insertUser(user)
        this.showCustomToast("User registered successfully", R.layout.success_toast)
    }
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}