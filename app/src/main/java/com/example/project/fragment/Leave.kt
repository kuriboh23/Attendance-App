package com.example.project.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.activities.ApplyLeave
import com.example.project.activities.HomeActivity
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.Leave
import com.example.project.data.LeaveViewModel
import com.example.project.databinding.FragmentLeaveBinding
import com.example.project.fragment.list.LeaveAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class Leave : Fragment() {
    private lateinit var binding: FragmentLeaveBinding
    private lateinit var leaveAdapter: LeaveAdapter
    private lateinit var leaveViewModel: LeaveViewModel

    // Activity result launcher to handle the result from ApplyLeave
    private val applyLeaveLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Extract data from the result
            val data = result.data
            val date = data?.getStringExtra("DATE")
            val type = data?.getStringExtra("TYPE")
            val note = data?.getStringExtra("NOTE")
            val attachmentPath = data?.getStringExtra("ATTACHMENT_PATH")

            // Show the BottomSheetDialog for pending leave
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_pending, null)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)
            dialog.show()

            val okBtn = view.findViewById<MaterialButton>(R.id.home_btn)
            okBtn.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = UserPrefs.loadUserId(requireContext())
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val leavesRef = FirebaseDatabase.getInstance().getReference("User").child(uid).child("leave")

        leavesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedLeaves = mutableListOf<Leave>()
                for (child in snapshot.children) {
                    val leave = Leave(
                        id = 0,
                        date = child.child("date").getValue(String::class.java) ?: "",
                        type = child.child("type").getValue(String::class.java) ?: "",
                        status = child.child("status").getValue(String::class.java) ?: "",
                        note = child.child("note").getValue(String::class.java) ?: "",
                        attachmentPath = child.child("attachmentPath").getValue(String::class.java),
                        startDate = child.child("startDate").getValue(String::class.java) ?: "",
                        endDate = child.child("endDate").getValue(String::class.java) ?: "",
                        userId = userId,
                        uid = child.key ?: ""
                    )
                    updatedLeaves.add(leave)
                }

                // First clear existing Room entries for this user
                leaveViewModel.deleteLeavesForUser(userId) {
                    updatedLeaves.forEach { leaveViewModel.insertLeave(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error syncing leaves", Toast.LENGTH_SHORT).show()
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaveBinding.inflate(inflater, container, false)

        // Load user ID from SharedPreferences
        val userId = UserPrefs.loadUserId(requireContext())

        // Initialize RecyclerView
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        leaveAdapter = LeaveAdapter { leave ->
            // Show bottom sheet with leave details
            showLeaveDetailsBottomSheet(leave)
        }
        binding.recyclerViewRequests.adapter = leaveAdapter

        // Initialize ViewModel
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

        // Fetch leave summary for Casual and Sick leaves
        viewLifecycleOwner.lifecycleScope.launch {
            leaveViewModel.getLeaveSummary(userId).collect { summary ->
                // Update Casual leave
                binding.progressCasual.progress = if (summary.casualTotal > 0) {
                    (summary.casualUsed * 100) / summary.casualTotal
                } else {
                    0
                }
                binding.tvCasualCount.text = "${summary.casualUsed}/${summary.casualTotal}"

                // Update Sick leave
                binding.progressSick.progress = if (summary.sickTotal > 0) {
                    (summary.sickUsed * 100) / summary.sickTotal
                } else {
                    0
                }
                binding.tvSickCount.text = "${summary.sickUsed}/${summary.sickTotal}"
            }
        }

        binding.tvLeaveFilter.setOnClickListener {
            showLeaveFilterBottomSheet(userId)
        }

        // Show loading overlay
        binding.loadingOverlay.visibility = View.VISIBLE

        // Fetch leaves for the current user
        leaveViewModel.getAllUserLeaves(userId).observe(viewLifecycleOwner) { leaves ->
            binding.loadingOverlay.visibility = View.GONE // Hide loading overlay
            leaveAdapter.setData(leaves)
        }

        // Set up click listener for adding a new leave request
        binding.addRequest.setOnClickListener {
            val intent = Intent(requireContext(), ApplyLeave::class.java)
            applyLeaveLauncher.launch(intent)
        }

        return binding.root
    }

    private fun showLeaveFilterBottomSheet(userId : Long) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_leave_filter, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        dialog.show()

        val tvLeaveStatusGroup = view.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.tvLeaveStatusGroup)
        val tvLeaveTypeGroup = view.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.tvLeaveTypeGroup)
        val tvLeaveReset = view.findViewById<MaterialButton>(R.id.tvLeaveReset)
        val tvLeaveApply = view.findViewById<MaterialButton>(R.id.tvLeaveApply)

        tvLeaveReset.setOnClickListener {
            tvLeaveStatusGroup.clearChecked()
            tvLeaveTypeGroup.clearChecked()
        }

        tvLeaveApply.setOnClickListener {
            if (tvLeaveStatusGroup.checkedButtonId != -1 && tvLeaveTypeGroup.checkedButtonId != -1) {
                val status = when (tvLeaveStatusGroup.checkedButtonId) {
                    R.id.tvLeavePending -> "Pending"
                    R.id.tvLeaveApproved -> "Approved"
                    R.id.tvLeaveRejected -> "Rejected"
                    else -> return@setOnClickListener
                }

                val type = when (tvLeaveTypeGroup.checkedButtonId) {
                    R.id.tvLeaveCasual -> "Casual"
                    R.id.tvLeaveSick -> "Sick"
                    else -> return@setOnClickListener
                }

                // Observe the LiveData based on selected status and type
                leaveViewModel.getLeavesByStatusAndType(userId, status, type).observe(viewLifecycleOwner) { leaves ->
                    leaveAdapter.setData(leaves)
                }

                dialog.dismiss()
            }
        }
    }

    // Function to show leave details in a bottom sheet
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLeaveDetailsBottomSheet(leave: Leave) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_leave_details, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)

        // Find views in the bottom sheet layout
        val tvDate = view.findViewById<TextView>(R.id.tvLeaveDetailDate)
        val tvType = view.findViewById<TextView>(R.id.tvLeaveDetailType)
        val tvStatus = view.findViewById<TextView>(R.id.tvLeaveDetailStatus)
        val tvNote = view.findViewById<TextView>(R.id.tvLeaveDetailNote)
        val tvAttachment = view.findViewById<MaterialButton>(R.id.tvLeaveDetailAttachment)

        // Set leave details
        tvDate.text = getDayAbbreviation(leave.date)
        tvType.text = leave.type
        tvStatus.text = leave.status
        tvNote.text = leave.note

        // Set status text color
        when (leave.status.lowercase()) {
            "approved" -> tvStatus.setBackgroundResource(R.drawable.status_approved)
            "rejected" -> tvStatus.setBackgroundResource(R.drawable.status_rejected)
            "pending" -> tvStatus.setBackgroundResource(R.drawable.status_pending)
            else -> null
        }

        // Handle attachment
        if (!leave.attachmentPath.isNullOrEmpty()) {
            val file = File(leave.attachmentPath)
            tvAttachment.text = "Show Attachment"

            // Make attachment clickable to open the file
            tvAttachment.setOnClickListener {
                openFile(leave.attachmentPath)
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayAbbreviation(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val date = LocalDate.parse(dateString, inputFormatter)
        val dayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) // "EEE" gives "Fri"
        return date.format(dayFormatter)
    }

    // Function to open the attached file
    private fun openFile(filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, requireContext().contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Unable to open file", Toast.LENGTH_SHORT).show()
        }
    }

}