package com.example.project.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivitySignupBinding
import com.example.project.function.Function.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp: AppCompatActivity() {

    lateinit var back: ImageView
    lateinit var binding: ActivitySignupBinding

    lateinit var firebaseRef: DatabaseReference
    lateinit var auth: FirebaseAuth

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseRef = FirebaseDatabase.getInstance().getReference("User")
        auth = FirebaseAuth.getInstance()

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

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

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser: FirebaseUser? = auth.currentUser
                                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener

                                    val newUser = User(0, firstName, lastName, email, password, role, uid)

                                    val idCounterRef = FirebaseDatabase.getInstance().getReference("userIdCounter")
                                    idCounterRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                                        override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                                            var currentId = currentData.getValue(Int::class.java) ?: 0
                                            currentId += 1
                                            currentData.value = currentId
                                            return com.google.firebase.database.Transaction.success(currentData)
                                        }

                                        override fun onComplete(
                                            error: com.google.firebase.database.DatabaseError?,
                                            committed: Boolean,
                                            currentData: com.google.firebase.database.DataSnapshot?
                                        ) {
                                            if (committed) {
                                                val generatedId = currentData?.getValue(Int::class.java) ?: 0
                                                val userWithId = newUser.copy(id = generatedId.toLong())

                                                firebaseRef.child(uid).setValue(userWithId)
                                                    .addOnSuccessListener {
                                                        userViewModel.insertUser(userWithId) {}
                                                        this@SignUp.showCustomToast("User registered successfully", R.layout.success_toast)
                                                        val intent = Intent(this@SignUp, Login::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                    .addOnFailureListener {
                                                        this@SignUp.showCustomToast("Failed to save user to database", R.layout.error_toast)
                                                    }
                                            } else {
                                                this@SignUp.showCustomToast("Failed to generate unique user ID", R.layout.error_toast)
                                            }
                                        }
                                    })
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

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
