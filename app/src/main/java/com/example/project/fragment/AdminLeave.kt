package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.data.Leave
import com.example.project.data.LeaveViewModel
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminLeaveBinding
import com.example.project.fragment.adapters.LeaveAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton


class AdminLeave : Fragment() {
    private lateinit var binding: FragmentAdminLeaveBinding
    private lateinit var leaveAdapter: LeaveAdapter
    private lateinit var leaveViewModel: LeaveViewModel
    private lateinit var userViewModel: UserViewModel
    private var allLeaves: List<Leave> = emptyList() // Store unfiltered leaves
    private var userMap: Map<Long, User> = emptyMap() // Store user map
    private var currentFilterType: String? = null // Store the current type filter ("All", "Casual", "Sick")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminLeaveBinding.inflate(inflater, container, false)

        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Observe leaves and users once
        leaveViewModel.allLeaves.observe(viewLifecycleOwner) { leaves ->
            allLeaves = leaves // Store the unfiltered leaves
            val totalLeaves = leaves.size
            val pendingApprovals = leaves.filter { it.status == "Pending" }.size
            val casualLeaves = leaves.filter { it.type == "Casual" }.size
            val sickLeaves = leaves.filter { it.type == "Sick" }.size

            binding.tvTotalLeaves.text = totalLeaves.toString()
            binding.tvPendingApprovals.text = pendingApprovals.toString()
            binding.tvCasualLeaves.text = casualLeaves.toString()
            binding.tvSickLeaves.text = sickLeaves.toString()

            userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
                userMap = users.associateBy { it.id } // Store the user map
                updateAdapterWithFilters() // Update the adapter with the current filters
            }
        }

        val buttonAll = binding.btnAll
        val buttonCasual = binding.btnCasual
        val buttonSick = binding.btnSick

        binding.leaveFilter.setOnClickListener {
            showLeaveFilterBottomSheet()
        }

        // Set click listeners for each button
        buttonAll.setOnClickListener {
            setButtonState(buttonAll)
            currentFilterType = null // No type filter (show all)
            updateAdapterWithFilters()
        }
        buttonCasual.setOnClickListener {
            setButtonState(buttonCasual)
            currentFilterType = "Casual"
            updateAdapterWithFilters()
        }
        buttonSick.setOnClickListener {
            setButtonState(buttonSick)
            currentFilterType = "Sick"
            updateAdapterWithFilters()
        }

        // Set "ALL" as the default highlighted button
        setButtonState(buttonAll)

        return binding.root
    }

    private var currentStatusFilter: String? = null // Store the current status filter

    @SuppressLint("MissingInflatedId")
    private fun showLeaveFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_admin_leave_filter, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        dialog.show()

        val leaveStatusGroup = view.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.leaveStatusGroup)
        val reset = view.findViewById<MaterialButton>(R.id.reset)
        val apply = view.findViewById<MaterialButton>(R.id.apply)

/*        // Pre-check the toggle group based on the current filter
        currentStatusFilter?.let { status ->
            val buttonId = when (status) {
                "Pending" -> R.id.leavePending
                "Approved" -> R.id.leaveApproved
                "Rejected" -> R.id.leaveRejected
                else -> -1
            }
            if (buttonId != -1) {
                leaveStatusGroup.check(buttonId)
            }
        }*/

        reset.setOnClickListener {
            leaveStatusGroup.clearChecked()
        }

        apply.setOnClickListener {
            if (leaveStatusGroup.checkedButtonId != -1) {
                currentStatusFilter = when (leaveStatusGroup.checkedButtonId) {
                    R.id.leavePending -> "Pending"
                    R.id.leaveApproved -> "Approved"
                    R.id.leaveRejected -> "Rejected"
                    else -> null
                }
                updateAdapterWithFilters()
                dialog.dismiss()
            }
        }
    }

    private fun updateAdapterWithFilters() {
        var filteredLeaves = allLeaves

        // Apply type filter (All, Casual, Sick)
        currentFilterType?.let { type ->
            filteredLeaves = filteredLeaves.filter { it.type == type }
        }

        // Apply status filter (Pending, Approved, Rejected)
        currentStatusFilter?.let { status ->
            filteredLeaves = filteredLeaves.filter { it.status == status }
        }

        leaveAdapter = LeaveAdapter(filteredLeaves, userMap)
        binding.rvLeaves.adapter = leaveAdapter
    }

    private fun setButtonState(selectedButton: MaterialButton) {
        binding.btnAll.apply {
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_light)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        binding.btnCasual.apply {
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_light)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        binding.btnSick.apply {
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_light)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        selectedButton.apply {
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.mainColor)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }
}