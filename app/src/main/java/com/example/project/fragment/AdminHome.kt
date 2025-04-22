package com.example.project.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.activities.MainActivity
import com.example.project.activities.Userdetails
import com.example.project.data.CheckViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminHomeBinding
import com.example.project.fragment.adapters.UserAdapter
import com.example.project.fragment.adapters.UserWithStatus
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHome : Fragment() {

    lateinit var binding: FragmentAdminHomeBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel
    private lateinit var userAdapter: UserAdapter
    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val adminDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    val userWithStatusList = mutableListOf<UserWithStatus>()

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        binding.tvAdminDate.text = adminDate

        binding.ivAdminProfile.setOnClickListener {
            signOut()
        }

        // Show loading overlay while loading initial data
        binding.loadingOverlay.visibility = View.VISIBLE

        binding.teamFilter.setOnClickListener {
            showUserFilterBottomSheet()
        }

       /* // Initialize UserAdapter with click callback
        userAdapter = UserAdapter(emptyList()) { user ->
            val bundle = Bundle().apply {
                putLong("homeUserId", user.id)
            }
            findNavController().navigate(R.id.toAdminAttendance, bundle)
        }*/

        userAdapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(),Userdetails::class.java).apply {
                putExtra("userId", user.id)
            }
            startActivity(intent)
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = userAdapter

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        val userId = UserPrefs.loadUserId(requireContext())
        userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
            binding.tvAdminGreeting.text = "Hello, ${user.lastName}"

            binding.loadingOverlay.visibility = View.GONE
        }

        userViewModel.allUsers.observe(viewLifecycleOwner) { users ->

            binding.loadingOverlay.visibility = View.VISIBLE

            var presentCount = 0
            var absentCount = 0
            var lateCount = 0

            val filteredUsers = users.filter { it.role == "user" }

            filteredUsers.forEach { user ->
                checkViewModel.getChecksUserByDate(currentDate, user.id).observe(viewLifecycleOwner) { checks ->
                        val status: String = if (checks.isNotEmpty()) {
                            var isLate = false
                            checks.forEach { check ->
                                val (hourIn, _) = timeToIntPair(check.checkInTime)
                                isLate = if ((hourIn in 9 until 13) || (hourIn in 15 until 19)) {
                                    true
                                } else {
                                    false
                                }
                            }
                            if (isLate) {
                                lateCount++
                                "Late"
                            } else {
                                presentCount++
                                "Present"
                            }
                        } else {
                            absentCount++
                            "Absent"
                        }

                        userWithStatusList.add(UserWithStatus(user, status))

                        if (userWithStatusList.size == filteredUsers.size) {
                            userAdapter.updateUsers(userWithStatusList)
                            binding.tvPresentCount.text = presentCount.toString()
                            binding.tvAbsentCount.text = absentCount.toString()
                            binding.tvLateCount.text = lateCount.toString()

                            userAdapter.notifyDataSetChanged()

                            // Hide loading overlay after initial data is loaded
                            binding.loadingOverlay.visibility = View.GONE
                        }
                    }
            }
        }

        return binding.root
    }

    private fun signOut() {
        UserPrefs.clearUserId(requireContext())
        UserPrefs.savedIsLoggedIn(requireContext(), false)

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    fun timeToIntPair(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(" ")
        val timeParts = parts[0].split(":")
        var hours = timeParts[0].toInt()
        val minutes = timeParts[1].toInt()
        val isPM = parts[1].equals("PM", ignoreCase = true)

        if (isPM && hours != 12) {
            hours += 12
        } else if (!isPM && hours == 12) {
            hours = 0
        }

        return Pair(hours, minutes)
    }

    @SuppressLint("MissingInflatedId")
    private fun showUserFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_user_filter, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        dialog.show()

        val usersStatusGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.userStatusGroup)
        val reset = view.findViewById<MaterialButton>(R.id.btReset)
        val apply = view.findViewById<MaterialButton>(R.id.btApply)

        reset.setOnClickListener {
            usersStatusGroup.clearChecked()
        }

        apply.setOnClickListener {
            if (usersStatusGroup.checkedButtonId != -1) {
                val status = when (usersStatusGroup.checkedButtonId) {
                    R.id.usersPresent -> "Present"
                    R.id.usersAbsent -> "Absent"
                    R.id.usersLate -> "Late"
                    else -> return@setOnClickListener
                }

                val newUsersList = userWithStatusList.filter { it.status == status }
                userAdapter.updateUsers(newUsersList)

                dialog.dismiss()
            }
        }
    }
}