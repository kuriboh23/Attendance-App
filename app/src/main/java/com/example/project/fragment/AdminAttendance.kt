package com.example.project.fragment

import TeamUserAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.CheckViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentAdminAttendanceBinding
import com.example.project.fragment.adapters.CheckAdapter
import com.example.project.function.function.showCustomToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminAttendance : Fragment() {

    lateinit var binding: FragmentAdminAttendanceBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var checkViewModel: CheckViewModel

    private lateinit var checkAdapter: CheckAdapter
    private lateinit var teamUserAdapter: TeamUserAdapter

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val now = System.currentTimeMillis()
    val currentDateStr = dateFormatter.format(Date(now))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminAttendanceBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]

        // Get userId from arguments
        val userId = arguments?.getLong("homeUserId")

        if (userId != null) {
            // Show loading overlay while loading initial data
            binding.loadingOverlay.visibility = View.VISIBLE

            // Initialize CheckAdapter
            checkAdapter = CheckAdapter(emptyList())
            binding.rvUserCheck.layoutManager = LinearLayoutManager(requireContext())
            binding.rvUserCheck.adapter = checkAdapter


            // Observe user details
            userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
                binding.mainTitle.text = "${user.lastName} Attendance"
            }

            val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthNameYear = monthNameYearFormat.format(now)
            val monthNameFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val monthName = monthNameFormat.format(now)
            val weekOfMonth = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

            // Fetch initial checks
            checkViewModel.getChecksUserByDate(currentDateStr, userId)
                .observe(viewLifecycleOwner) { checks ->

                    // Hide loading overlay after initial data is loaded
                    binding.loadingOverlay.visibility = View.GONE

                    checkAdapter.updateChecks(checks)
                    binding.tvMonthYear.text = monthNameYear
                    binding.summaryText.text = "Summary of $monthName"
                    binding.weeksText.text = "Week $weekOfMonth"
                }
            binding.filterMonth.setOnClickListener {
                filterByMonth(userId, dateFormatter)
            }
        }

        binding.searchUser.setOnClickListener {
            showUseSearchBottomSheet()
        }

        val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthNameYear = monthNameYearFormat.format(now)
        val monthNameFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val monthName = monthNameFormat.format(now)
        val weekOfMonth = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

        binding.tvMonthYear.text = monthNameYear
        binding.summaryText.text = "Summary of $monthName"
        binding.weeksText.text = "Week $weekOfMonth"

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun filterByMonth(userId: Long, dateFormat: SimpleDateFormat) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date we will use its week")
            .build()

        datePicker.show(childFragmentManager, "WEEK_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection

            // Align to start of the week (Monday)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startWeek = calendar.time

            // End of week (Friday)
            calendar.add(Calendar.DAY_OF_WEEK, 4)
            val endWeek = calendar.time

            val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            val startOfWeek = dateFormat.format(startWeek)
            val endOfWeek = dateFormat.format(endWeek)

            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
            val monthNameYear = monthNameYearFormat.format(startWeek)
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(startWeek)

            // Fetch checks for the selected week
            checkViewModel.getChecksByWeek(userId, startOfWeek, endOfWeek)
                .observe(viewLifecycleOwner) { checks ->

                    // Show loading overlay while loading initial data
                    binding.loadingOverlay.visibility = View.VISIBLE

                    if (checks.isNotEmpty()) {
                        checkAdapter.updateChecks(checks)
                        binding.tvMonthYear.text = monthNameYear
                        binding.summaryText.text = "Summary of $monthName"
                        binding.weeksText.text = "Week $weekOfMonth"
                    } else {
                        requireContext().showCustomToast("No Checks found", R.layout.error_toast)
                    }
                    binding.loadingOverlay.visibility = View.GONE // Hide loading overlay
                }
            binding.filterMonth.setOnClickListener {
                filterByMonth(userId, dateFormatter)
            }
        }
    }

    @SuppressLint("MissingInflatedId", "RestrictedApi")
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

            checkAdapter = CheckAdapter(emptyList())
            binding.rvUserCheck.layoutManager = LinearLayoutManager(requireContext())
            binding.rvUserCheck.adapter = checkAdapter

            userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
                binding.mainTitle.text = "${user.lastName} Attendance"
            }
            // Fetch initial checks
            checkViewModel.getChecksUserByDate(currentDateStr, userId)
                .observe(viewLifecycleOwner) { checks ->
                    // Show loading overlay while loading initial data
                    binding.loadingOverlay.visibility = View.VISIBLE

                    checkAdapter.updateChecks(checks)

                    // Hide loading overlay after initial data is loaded
                    binding.loadingOverlay.visibility = View.GONE
                }

            binding.filterMonth.setOnClickListener {
                filterByMonth(userId, dateFormatter)
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