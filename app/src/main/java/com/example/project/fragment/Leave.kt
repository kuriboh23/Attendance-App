package com.example.project.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.ApplyLeave
import com.example.project.HomeActivity
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.LeaveViewModel
import com.example.project.databinding.FragmentLeaveBinding
import com.example.project.fragment.list.LeaveAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

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

            // Show the BottomSheetDialog
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaveBinding.inflate(inflater, container, false)

        // Load user ID from SharedPreferences
        val userId = UserPrefs.loadUserId(requireContext())

        // Initialize RecyclerView
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        leaveAdapter = LeaveAdapter()
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


        // Show loading overlay
        binding.loadingOverlay.visibility = View.VISIBLE

        // Fetch leaves for the current podczas gdy user
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
}