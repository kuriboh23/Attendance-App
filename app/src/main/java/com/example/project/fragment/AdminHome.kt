package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.R
import com.example.project.data.CheckViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminHomeBinding
import com.example.project.fragment.adapters.UserAdapter
import com.example.project.fragment.adapters.UserWithStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHome : Fragment() {

    lateinit var binding: FragmentAdminHomeBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel
    private lateinit var userAdapter: UserAdapter
    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        // Initialize UserAdapter with click callback
        userAdapter = UserAdapter(emptyList()) { user ->
            val bundle = Bundle().apply {
                putLong("userId", user.id)
            }
            findNavController().navigate(R.id.toAdminAttendance, bundle)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = userAdapter

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
            val userWithStatusList = mutableListOf<UserWithStatus>()
            var presentCount = 0
            var absentCount = 0
            var leaveCount = 0

            val filteredUsers = users.filter { it.role == "user" }

            filteredUsers.forEach { user ->
                checkViewModel.getChecksUserByDate(currentDate, user.id)
                    .observe(viewLifecycleOwner) { checks ->
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
                                leaveCount++
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
                            binding.tvLeaveCount.text = leaveCount.toString()
                            userAdapter.notifyDataSetChanged()
                        }
                    }
            }
        }

        return binding.root
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
}