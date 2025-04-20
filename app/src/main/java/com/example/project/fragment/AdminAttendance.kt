package com.example.project.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.data.CheckViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminAttendanceBinding
import com.example.project.fragment.adapters.CheckAdapter

class AdminAttendance : Fragment() {

    lateinit var binding: FragmentAdminAttendanceBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel
    private lateinit var checkAdapter: CheckAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminAttendanceBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        // Initialize CheckAdapter
        checkAdapter = CheckAdapter(emptyList())
        binding.rvUserCheck.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserCheck.adapter = checkAdapter

        // Get userId from arguments
        val userId = arguments?.getLong("userId") ?: return binding.root

        // Observe user details
        userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
            binding.mainTitle.text = "${user.lastName} Attendance"
        }

        // Observe checks for the user
        checkViewModel.getAllUserChecks(userId).observe(viewLifecycleOwner) { checks ->
            checkAdapter.updateChecks(checks)
        }

        return binding.root
    }
}