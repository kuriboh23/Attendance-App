package com.example.project.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project.*
import com.example.project.R
import com.example.project.data.*
import com.example.project.databinding.ActivityLoginBinding
import com.example.project.function.Function.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel
    private lateinit var leaveViewModel: LeaveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

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
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    binding.loadingOverlay.visibility = View.VISIBLE

                    val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)
                    userRef.get().addOnSuccessListener { snapshot ->
                        val role = snapshot.child("role").getValue(String::class.java) ?: "user"
                        if (role == "user") {
                            fetchAndStoreSingleUser(uid, email)
                        } else {
                            fetchAndStoreAllUsers()
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

    private fun fetchAndStoreSingleUser(uid: String, email: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.toUser(uid, email) ?: return@addOnSuccessListener
            userViewModel.insertUser(user) {
                fetchAndStoreChecks(uid, user.id)
                fetchAndStoreLeaves(uid, user.id)
                handleLoginSuccess(user)
            }
        }.addOnFailureListener {
            showCustomToast("Failed to fetch user data", R.layout.error_toast)
            hideLoadingOverlayWithFade()
        }
    }

    private fun fetchAndStoreAllUsers() {
        val dbRef = FirebaseDatabase.getInstance().getReference("User")
        dbRef.get().addOnSuccessListener { snapshot ->
            val allUsers = mutableListOf<User>()
            snapshot.children.forEach { userSnap ->
                val uid = userSnap.key ?: return@forEach
                val user = userSnap.toUser(uid, userSnap.child("email").getValue(String::class.java) ?: return@forEach)
                user?.let {
                    allUsers.add(it)
                    userViewModel.insertUser(it) {}
                    fetchAndStoreChecks(uid, it.id)
                    fetchAndStoreLeaves(uid, it.id)
                }
            }

            if (allUsers.isNotEmpty()) {
                handleLoginSuccess(allUsers.first { it.uid == auth.currentUser?.uid })
            } else {
                showCustomToast("No users found", R.layout.error_toast)
                hideLoadingOverlayWithFade()
            }
        }.addOnFailureListener {
            showCustomToast("Failed to fetch users", R.layout.error_toast)
            hideLoadingOverlayWithFade()
        }
    }

    private fun fetchAndStoreChecks(uid: String, userId: Long) {
        val checksRef = FirebaseDatabase.getInstance().getReference("User").child(uid).child("check")
        checksRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.mapNotNull {
                it.toCheck(userId)
            }.forEach {
                checkViewModel.addCheck(it)
            }
        }
    }

    private fun fetchAndStoreLeaves(uid: String, userId: Long) {
        val leavesRef = FirebaseDatabase.getInstance().getReference("User").child(uid).child("leave")
        leavesRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.mapNotNull {
                it.toLeave(userId)
            }.forEach {
                leaveViewModel.insertLeave(it)
            }
        }
    }

    private fun handleLoginSuccess(user: User) {
        UserPrefs.saveUserId(this, user.id)
        UserPrefs.saveUserUid(this, user.uid)

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

    // Extensions to convert FirebaseSnapshot to data models
    private fun DataSnapshot.toUser(uid: String, email: String): User? {
        return User(
            id = child("id").getValue(Long::class.java) ?: return null,
            uid = uid,
            name = child("name").getValue(String::class.java) ?: "",
            lastName = child("lastName").getValue(String::class.java) ?: "",
            email = email,
            password = child("password").getValue(String::class.java) ?: "",
            role = child("role").getValue(String::class.java) ?: "user"
        )
    }

    private fun DataSnapshot.toCheck(userId: Long): Check? {
        return Check(
            id = 0,
            date = child("date").getValue(String::class.java) ?: return null,
            checkInTime = child("checkInTime").getValue(String::class.java) ?: "",
            checkOutTime = child("checkOutTime").getValue(String::class.java) ?: "",
            durationInSecond = child("durationInSecond").getValue(Long::class.java) ?: 0L,
            userId = userId
        )
    }

    private fun DataSnapshot.toLeave(userId: Long): Leave? {
        return Leave(
            id = 0,
            date = child("date").getValue(String::class.java) ?: return null,
            type = child("type").getValue(String::class.java) ?: "",
            status = child("status").getValue(String::class.java) ?: "",
            note = child("note").getValue(String::class.java) ?: "",
            attachmentPath = child("attachmentPath").getValue(String::class.java),
            startDate = child("startDate").getValue(String::class.java) ?: "",
            endDate = child("endDate").getValue(String::class.java) ?: "",
            userId = userId,
            uid = child("uid").getValue(String::class.java) ?: ""
        )
    }
}
