package com.example.project.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.Check
import com.example.project.data.CheckViewModel
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivityLoginBinding
import com.example.project.function.Function.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        binding.arrowLeft.setOnClickListener { finish() }

        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        binding.signInBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.txPassword.text.toString()

            if (!isValidEmail(email)) {
                showCustomToast("Invalid Email", R.layout.error_toast)
                binding.emailInput.text?.clear()
                binding.emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showCustomToast("Invalid Password", R.layout.error_toast)
                binding.txPassword.text?.clear()
                binding.txPassword.requestFocus()
                return@setOnClickListener
            }

            loginWithFirebase(email, password)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    userViewModel.allUsers.observe(this) { users ->
                        val localUser = users.find { it.uid == uid }
                        if (localUser != null) {
                            handleLoginSuccess(localUser)
                        } else {
                            assignAutoIncrementedUserId(uid, email)
                        }
                    }
                } else {
                    showCustomToast("Authentication failed: ${task.exception?.message}", R.layout.error_toast)
                }
            }
            .addOnFailureListener {
                showCustomToast("Login failed: ${it.message}", R.layout.error_toast)
            }
    }

    private fun assignAutoIncrementedUserId(uid: String, email: String) {
        val database = FirebaseDatabase.getInstance()
        val lastIdRef = database.getReference("lastUserId")

        lastIdRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var currentId = currentData.getValue(Long::class.java) ?: 0L
                currentData.value = currentId + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    val newId = snapshot?.getValue(Long::class.java) ?: 0L
                    fetchUserAndChecks(uid, email, newId)
                } else {
                    showCustomToast("Failed to assign ID", R.layout.error_toast)
                    hideLoadingOverlayWithFade()
                }
            }
        })
    }

    private fun fetchUserAndChecks(uid: String, email: String, generatedId: Long) {
        val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            val firstName = snapshot.child("name").getValue(String::class.java) ?: ""
            val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
            val role = snapshot.child("role").getValue(String::class.java) ?: "user"
            val passwordFromDb = snapshot.child("password").getValue(String::class.java) ?: ""

            val newUser = User(
                id = generatedId,
                uid = uid,
                name = firstName,
                lastName = lastName,
                email = email,
                password = passwordFromDb,
                role = role
            )

            userRef.child("id").setValue(generatedId)

            userViewModel.insertUser(newUser) {
                val checksRef = userRef.child("check")
                checksRef.get().addOnSuccessListener { checksSnapshot ->
                    val checkList = mutableListOf<Check>()
                    checksSnapshot.children.forEach { child ->
                        val check = Check(
                            id = 0,
                            date = child.child("date").getValue(String::class.java) ?: "",
                            checkInTime = child.child("checkInTime").getValue(String::class.java) ?: "",
                            checkOutTime = child.child("checkOutTime").getValue(String::class.java) ?: "",
                            durationInSecond = child.child("durationInSecond").getValue(Long::class.java) ?: 0L,
                            userId = generatedId
                        )
                        checkList.add(check)
                    }
                    checkList.forEach { checkViewModel.addCheck(it) }
                    handleLoginSuccess(newUser)
                }.addOnFailureListener {
                    showCustomToast("Failed to sync checks", R.layout.error_toast)
                    hideLoadingOverlayWithFade()
                }
            }
        }.addOnFailureListener {
            showCustomToast("Failed to fetch user data", R.layout.error_toast)
            hideLoadingOverlayWithFade()
        }
    }

    private fun handleLoginSuccess(user: User) {
        UserPrefs.saveUserId(this, user.id)

        if (binding.privacyChekbox.isChecked) {
            UserPrefs.savedIsLoggedIn(this, true)
            UserPrefs.saveUserRole(this, user.role)
        }

        val intent = if (user.role == "user") {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, AdminHomeActivity::class.java)
        }

        showCustomToast("Login Successful", R.layout.success_toast)
        hideLoadingOverlayWithFade()
        startActivity(intent)
        finish()
    }

    private fun hideLoadingOverlayWithFade() {
        binding.loadingOverlay.animate()
            .setStartDelay(50)
            .setDuration(150)
            .withEndAction { binding.loadingOverlay.visibility = View.GONE }
            .start()
    }
}
