package com.example.project.fragment

import TeamUserAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.*
import com.example.project.data.Leave
import com.example.project.databinding.FragmentAdminLeaveBinding
import com.example.project.fragment.adapters.LeaveAdapter
import com.example.project.function.Function.showCustomToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AdminLeave : Fragment() {

    private lateinit var binding: FragmentAdminLeaveBinding
    private lateinit var leaveViewModel: LeaveViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var teamUserAdapter: TeamUserAdapter
    private lateinit var leaveAdapter: LeaveAdapter

    private var allLeaves: List<Leave> = emptyList()
    private var userMap: Map<Long, User> = emptyMap()

    private var currentFilterType: String? = null
    private var currentStatusFilter: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAdminLeaveBinding.inflate(inflater, container, false)
        initViewModels()
        setupObservers()
        setupUI()
        syncLeavesFromFirebaseToRoom()
        return binding.root
    }

    private fun initViewModels() {
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        leaveViewModel.allLeaves.observe(viewLifecycleOwner) { leaves ->
            allLeaves = leaves
            updateStatistics(leaves)
            userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
                userMap = users.associateBy { it.id }
                updateAdapterWithFilters()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        with(binding) {
            btnAll.setOnClickListener { applyTypeFilter(null, btnAll) }
            btnCasual.setOnClickListener { applyTypeFilter("Casual", btnCasual) }
            btnSick.setOnClickListener { applyTypeFilter("Sick", btnSick) }
            leaveFilter.setOnClickListener { showLeaveFilterBottomSheet() }
            searchUser.setOnClickListener { showUserSearchBottomSheet() }
            setButtonState(btnAll)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatistics(leaves: List<Leave>) = with(binding) {
        tvTotalLeaves.text = leaves.size.toString()
        tvPendingApprovals.text = leaves.count { it.status == "Pending" }.toString()
        tvCasualLeaves.text = leaves.count { it.type == "Casual" }.toString()
        tvSickLeaves.text = leaves.count { it.type == "Sick" }.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapterWithFilters() {
        var filtered = allLeaves

        currentFilterType?.let { filterType ->
            filtered = filtered.filter { leave -> leave.type == filterType }
        }

        currentStatusFilter?.let { statusFilter ->
            filtered = filtered.filter { leave -> leave.status == statusFilter }
        }

        leaveAdapter = LeaveAdapter { leave -> showLeaveDetailsBottomSheet(leave) }
        binding.rvLeaves.adapter = leaveAdapter
        leaveAdapter.setData(filtered, userMap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyTypeFilter(type: String?, button: MaterialButton) {
        currentFilterType = type
        setButtonState(button)
        updateAdapterWithFilters()
    }

    private fun setButtonState(selected: MaterialButton) = with(binding) {
        listOf(btnAll, btnCasual, btnSick).forEach {
            it.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray_light)
        }
        selected.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        selected.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.mainColor)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLeaveFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_admin_leave_filter, null)
        dialog.setContentView(view)
        dialog.show()

        val group = view.findViewById<com.google.android.material.button.MaterialButtonToggleGroup>(R.id.leaveStatusGroup)
        val reset = view.findViewById<MaterialButton>(R.id.reset)
        val apply = view.findViewById<MaterialButton>(R.id.apply)

        reset.setOnClickListener {
            group.clearChecked()
            currentStatusFilter = null
            updateAdapterWithFilters()
        }

        apply.setOnClickListener {
            currentStatusFilter = when (group.checkedButtonId) {
                R.id.leavePending -> "Pending"
                R.id.leaveApproved -> "Approved"
                R.id.leaveRejected -> "Rejected"
                else -> null
            }
            updateAdapterWithFilters()
            dialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showLeaveDetailsBottomSheet(leave: Leave) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_admin_leave_details, null)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)

        val user = userMap[leave.userId]
        view.findViewById<TextView>(R.id.tvFullName).text = "${user?.lastName} ${user?.name}"
        view.findViewById<TextView>(R.id.tvLeaveDate).text = formatDate(leave.date)
        view.findViewById<TextView>(R.id.tvStartDate).text = formatDate(leave.startDate)
        view.findViewById<TextView>(R.id.tvEndDate).text = formatDate(leave.endDate)
        view.findViewById<TextView>(R.id.tvType).text = leave.type
        view.findViewById<TextView>(R.id.tvNote).text = leave.note

        val btnApprove = view.findViewById<MaterialButton>(R.id.btnApprove)
        val btnReject = view.findViewById<MaterialButton>(R.id.btnReject)

        if (leave.status == "Pending") {
            btnApprove.setOnClickListener {
                updateLeaveStatus("Approved", leave, user?.uid ?: "", dialog)
            }
            btnReject.setOnClickListener {
                updateLeaveStatus("Rejected", leave, user?.uid ?: "", dialog)
            }
        } else {
            btnApprove.visibility = View.GONE
            btnReject.visibility = View.GONE
        }

        dialog.show()
    }

    private fun updateLeaveStatus(newStatus: String, leave: Leave, uid: String, dialog: BottomSheetDialog) {
        showConfirmationDialog("Confirm to $newStatus this leave?", dialog) {
            leave.status = newStatus
            leaveViewModel.updateLeaveStatus(leave.id, newStatus)

            updateLeaveInFirebase(leave, uid)
            dialog.dismiss()
        }
    }

    private fun updateLeaveInFirebase(leave: Leave, userUid: String) {
        val leaveRef = FirebaseDatabase.getInstance()
            .getReference("User/$userUid/leave")

        leaveRef.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                if (child.child("uid").value == leave.uid) {
                    child.ref.child("status").setValue(leave.status)
                    break
                }
            }
        }
    }

    private fun syncLeavesFromFirebaseToRoom() {
        val db = FirebaseDatabase.getInstance().getReference("User")

        db.get().addOnSuccessListener { usersSnapshot ->
            for (userSnapshot in usersSnapshot.children) {
                val userUid = userSnapshot.key ?: continue
                val leavesSnapshot = userSnapshot.child("leave")

                for (leaveSnap in leavesSnapshot.children) {
                    val leave = leaveSnap.getValue(Leave::class.java)
                    leave?.let {
                        if (it.status == "Pending") {
                            leaveViewModel.insertIfNotExists(it)
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmationDialog(msg: String, dialog: BottomSheetDialog, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmation")
            .setMessage(msg)
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
    private fun formatDate(dateStr: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        return LocalDate.parse(dateStr, formatter).format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private fun showUserSearchBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_user_search, null)
        dialog.setContentView(view)
        dialog.show()

        val searchView = view.findViewById<SearchView>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTeamAttendance)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        teamUserAdapter = TeamUserAdapter(emptyList()) { user ->
            dialog.dismiss()
            updateUserLeaves(user)
        }
        recyclerView.adapter = teamUserAdapter

        userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
            binding.loadingOverlay.visibility = View.VISIBLE
            teamUserAdapter.updateUsers(users.filter { it.role == "user" })
            binding.loadingOverlay.visibility = View.GONE
        }

        setupSearchAppearance(searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?) = teamUserAdapter.filterUsers(newText.orEmpty()).let { true }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserLeaves(user: User) {
        userViewModel.getUserById(user.id).observe(viewLifecycleOwner) {
            binding.mainTitle.text = "${it.lastName} Leaves"
        }
        leaveViewModel.getAllUserLeaves(user.id).observe(viewLifecycleOwner) { leaves ->
            binding.loadingOverlay.visibility = View.VISIBLE
            allLeaves = leaves
            updateAdapterWithFilters()
            binding.loadingOverlay.visibility = View.GONE
        }
    }

    private fun setupSearchAppearance(searchView: SearchView) {
        val textView = searchView.findViewById<AutoCompleteTextView>(
            resources.getIdentifier("search_src_text", "id", requireContext().packageName)
        )
        textView.setTextColor(Color.BLACK)
        textView.setHintTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        searchView.isIconified = false
        searchView.requestFocus()
    }
}
