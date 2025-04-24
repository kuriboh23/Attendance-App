package com.example.project.fragment

import TeamUserAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.project.R
import com.example.project.data.Leave
import com.example.project.data.LeaveViewModel
import com.example.project.data.User
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminLeaveBinding
import com.example.project.fragment.adapters.LeaveAdapter
import com.example.project.function.Function.showCustomToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class AdminLeave : Fragment() {
    private lateinit var binding: FragmentAdminLeaveBinding
    private lateinit var leaveAdapter: LeaveAdapter
    private lateinit var leaveViewModel: LeaveViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var teamUserAdapter: TeamUserAdapter

    private var allLeaves: List<Leave> = emptyList() // Store unfiltered leaves
    private var userMap: Map<Long, User> = emptyMap() // Store user map
    private var currentFilterType: String? = null // Store the current type filter ("All", "Casual", "Sick")

    @RequiresApi(Build.VERSION_CODES.O)
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

        binding.searchUser.setOnClickListener {
            showUseSearchBottomSheet()
        }

        return binding.root
    }

    private var currentStatusFilter: String? = null // Store the current status filter

    @RequiresApi(Build.VERSION_CODES.O)
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

        reset.setOnClickListener {
            leaveStatusGroup.clearChecked()
            currentStatusFilter = null
            updateAdapterWithFilters()
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        leaveAdapter = LeaveAdapter { leave ->
            // Show bottom sheet with leave details
            showLeaveDetailsBottomSheet(leave)
        }
        binding.rvLeaves.adapter = leaveAdapter

        leaveAdapter.setData(filteredLeaves, userMap)
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    private fun showLeaveDetailsBottomSheet(leave: Leave) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_admin_leave_details, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)

        val tvLeaveDate = view.findViewById<TextView>(R.id.tvLeaveDate)
        val ivUser = view.findViewById<ImageView>(R.id.ivUser)
        val tvFullName = view.findViewById<TextView>(R.id.tvFullName)
        val tvStartDate = view.findViewById<TextView>(R.id.tvStartDate)
        val tvEndDate = view.findViewById<TextView>(R.id.tvEndDate)
        val tvType = view.findViewById<TextView>(R.id.tvType)
        val tvNote = view.findViewById<TextView>(R.id.tvNote)
        val btnAttachment = view.findViewById<MaterialButton>(R.id.btnAttachment)
        val btnReject = view.findViewById<MaterialButton>(R.id.btnReject)
        val btnApprove = view.findViewById<MaterialButton>(R.id.btnApprove)

        val user = userMap[leave.userId]
        if (user != null) {
            tvFullName.text = "${user.lastName} ${user.name}"
        }

        tvLeaveDate.text = getDayAbbreviation(leave.date)
        tvStartDate.text = getDayAbbreviation(leave.startDate)
        tvEndDate.text = getDayAbbreviation(leave.endDate)
        tvType.text = leave.type
        tvNote.text = leave.note

        btnAttachment.setOnClickListener {
            // Handle attachment button click
        }

        // Only allow action if status is "Pending"
        if (leave.status != "Pending") {
            btnReject.visibility = View.GONE
            btnApprove.visibility = View.GONE
        } else {
            btnReject.setOnClickListener {
                showConfirmationDialog("Reject this leave request?",dialog) {
                    leave.status = "Rejected"
                    leaveViewModel.updateLeaveStatus(leave.id, leave.status)
                    dialog.dismiss()
                }
            }

            btnApprove.setOnClickListener {
                showConfirmationDialog("Approve this leave request?",dialog) {
                    leave.status = "Approved"
                    leaveViewModel.updateLeaveStatus(leave.id, leave.status)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    // Helper function to show confirmation dialog
    private fun showConfirmationDialog(message: String,dialog: BottomSheetDialog, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmation")
            .setMessage(message)
            .setIcon(R.drawable.error_red)
            .setPositiveButton("Confirm") { _, _ ->
                onConfirm()
                requireContext().showCustomToast("Leave updated", R.layout.success_toast)
            }
            .setNegativeButton("Cancel") { _, _ ->
                requireContext().showCustomToast("Leave not updated", R.layout.error_toast)
                dialog.dismiss()
            }
            .show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayAbbreviation(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val date = LocalDate.parse(dateString, inputFormatter)
        val dayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) // "EEE" gives "Fri"
        return date.format(dayFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private fun showUseSearchBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_user_search, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        dialog.show()

        val searchView = view.findViewById<SearchView>(R.id.search_bar)
        val rvTeamAttendance = view.findViewById<RecyclerView>(R.id.rvTeamAttendance)

        // Customize search view appearance
        val searchAutoComplete = searchView.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(
            androidx.appcompat.R.id.search_src_text
        )
        searchAutoComplete.setTextColor(Color.BLACK)
        searchAutoComplete.setHintTextColor(Color.BLACK)
        searchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

        // Make search bar focused and open keyboard
        searchView.isIconified = false
        searchView.requestFocus()

        // RecyclerView setup
        teamUserAdapter = TeamUserAdapter(emptyList()){ user ->
            dialog.dismiss()
            val userId = user.id

            userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
                binding.mainTitle.text = "${user.lastName} Leaves"
            }
            // Fetch initial checks
            leaveViewModel.getAllUserLeaves(userId).observe(viewLifecycleOwner) { leaves ->
                // Show loading overlay while loading initial data
                    binding.loadingOverlay.visibility = View.VISIBLE

                    allLeaves = leaves
                /*println("Leaves: $leaves")
                println("Leaves: $allLeaves")*/
                    updateAdapterWithFilters()
                    // Hide loading overlay after initial data is loaded
                    binding.loadingOverlay.visibility = View.GONE
                }

        }
        rvTeamAttendance.layoutManager = LinearLayoutManager(requireContext())
        rvTeamAttendance.adapter = teamUserAdapter

        userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
            // Show loading overlay while loading initial data
            binding.loadingOverlay.visibility = View.VISIBLE

            val filteredUsers = users.filter { it.role == "user" }
            teamUserAdapter.updateUsers(filteredUsers)

            // Hide loading overlay after initial data is loaded
            binding.loadingOverlay.visibility = View.GONE
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                teamUserAdapter.filterUsers(newText.orEmpty())
                return true
            }
        })

    }


}