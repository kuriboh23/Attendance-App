package com.example.project.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.project.data.LeaveViewModel
import com.example.project.databinding.FragmentAdminLeaveBinding


class AdminLeave : Fragment() {
    private lateinit var binding: FragmentAdminLeaveBinding
    private lateinit var leaveViewModel: LeaveViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminLeaveBinding.inflate(inflater, container, false)
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

        leaveViewModel.allLeaves.observe(viewLifecycleOwner) { leaves ->
            val totalLeaves = leaves.size
            val pendingApprovals = leaves.filter { it.status == "Pending" }.size
            val casualLeaves = leaves.filter { it.type == "Casual" }.size
            val sickLeaves = leaves.filter { it.type == "Sick" }.size

            binding.tvTotalLeaves.text = totalLeaves.toString()
            binding.tvPendingApprovals.text = pendingApprovals.toString()
            binding.tvCasualLeaves.text = casualLeaves.toString()
            binding.tvSickLeaves.text = sickLeaves.toString()
        }

        return binding.root
    }
}