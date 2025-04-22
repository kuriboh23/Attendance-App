package com.example.project.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.activities.Login
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminSignUpBinding
import com.example.project.function.function.showCustomToast

class AdminSignUp : Fragment() {

    lateinit var binding: FragmentAdminSignUpBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminSignUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Sign Up button click listener
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
                            requireContext().showCustomToast("Please accept the privacy policy", R.layout.error_toast)
                            return@setOnClickListener
                        }
                        val user = User(0, firstName, lastName, email, password, role)
                        insertUser(user)

                        binding.nameInput.text?.clear()
                        binding.lastNameInput.text?.clear()
                        binding.emailInput.text?.clear()
                        binding.passwordTxt.text?.clear()
                        binding.confirmPasswordTxt.text?.clear()
                        binding.confirmPasswordInput.isActivated = false
                        binding.privacyChekbox.isChecked = false

                    } else {
                        requireContext().showCustomToast("Passwords do not match", R.layout.error_toast)
                    }
                } else {
                    requireContext().showCustomToast("Invalid Email", R.layout.error_toast)
                }
            } else {
                requireContext().showCustomToast("Please fill in all fields", R.layout.error_toast)
            }
        }
    }

    private fun insertUser(user: User) {
        userViewModel.insertUser(user)
        requireContext().showCustomToast("User registered successfully", R.layout.success_toast)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}