package com.example.project.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.adapter.MonthAdapter
import com.example.project.data.CheckViewModel
import com.example.project.data.LeaveViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.ActivityUserDetailsBinding
import com.example.project.fragment.list.MonthItem
import com.example.project.fragment.list.YearHeader
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Userdetails : AppCompatActivity() {

    lateinit var binding: ActivityUserDetailsBinding
    lateinit var userViewModel: UserViewModel
    lateinit var leaveViewModel: LeaveViewModel

    private lateinit var checkViewModel: CheckViewModel
    val currentMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    var presentCount = 0
    var absentCount = 0
    var lateCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        checkViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

        val userId = intent.getLongExtra("userId", -1)

        userViewModel.getUserById(userId).observe(this){user ->
            val fullName = "${user.name} ${user.lastName}"
            binding.tvUserFullName.text = fullName
            binding.tvUserGmail.text = user.email
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        checkViewModel.getChecksUserByMonth(currentMonth, userId).observe(this) { checks ->
            presentCount = 0
            absentCount = 0
            lateCount = 0

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = dateFormat.parse("$currentMonth-01")!!

            val groupedChecks = checks.groupBy { it.date }

            while (!calendar.time.after(today)) {
                val dateStr = dateFormat.format(calendar.time)
                val checksForDay = groupedChecks[dateStr]
                if (!checksForDay.isNullOrEmpty()) {

                    for (check in checksForDay) {
                        val (hourIn, _) = timeToIntPair(check.checkInTime)

                        if ((hourIn in 9 until 13) || (hourIn in 15 until 19)) {
                            lateCount++
                        } else {
                            presentCount++
                        }
                    }
                } else {
                    absentCount++
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            binding.tvPresentCount.text = presentCount.toString()
            binding.tvAbsentCount.text = absentCount.toString()
            binding.tvLateCount.text = lateCount.toString()
            binding.tvSummaryTitle.text = "Summary of $currentMonthName"
        }




        leaveViewModel.getLeavesByMonth(currentMonth, userId).observe(this) { leaves ->

            binding.tvSummaryleave.text = "Summary of $currentMonthName"
                if (leaves != null) {
                    val totalLeaves = leaves.size
                    val pendingApprovals = leaves.filter { it.status == "Pending" }.size
                    val arrpovedLeaves = leaves.filter { it.status == "Approved" }.size
                    val rejectedLeaves = leaves.filter { it.status == "Rejected" }.size

                    binding.tvPendingLeaves.text = pendingApprovals.toString()
                    binding.tvApprovedLeaves.text = arrpovedLeaves.toString()
                    binding.tvRejectedLeaves.text = rejectedLeaves.toString()
                    binding.tvLeaveTitle.text = "$totalLeaves Leaves"
            }
        }

        binding.ivFilterCheck.setOnClickListener {
            showFilterDialog(userId)
        }

    }

    // Filter button to select a different month
    fun showFilterDialog(userId:Long) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_date, null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)

        // Set up RecyclerView for month selection
        val recyclerView: RecyclerView = view.findViewById(R.id.month_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Prepare data for month selection
        val items = mutableListOf<YearHeader>()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        for (year in currentYear - 2..currentYear) {
            val monthsList = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ).mapIndexed { index, name ->
                MonthItem(
                    year = year,
                    monthNumber = index + 1,
                    monthName = name
                )
            }
            items.add(YearHeader(year = year, months = monthsList))
        }

        // Set up MonthAdapter for selecting months
        val monthAdapter = MonthAdapter(items) { selectedMonth ->
            val monthYearStr = "${selectedMonth.year}-${selectedMonth.monthNumber.toString().padStart(2, '0')}"

            binding.tvSummaryTitle.text = "Summary of ${selectedMonth.monthName}"
            binding.tvSummaryleave.text = "Summary of ${selectedMonth.monthName}"

            leaveViewModel.getLeavesByMonth(monthYearStr, userId).observe(this) { leaves ->
                if (leaves != null) {
                    val totalLeaves = leaves.size
                    val pendingApprovals = leaves.filter { it.status == "Pending" }.size
                    val arrpovedLeaves = leaves.filter { it.status == "Approved" }.size
                    val rejectedLeaves = leaves.filter { it.status == "Rejected" }.size

                    binding.tvPendingLeaves.text = pendingApprovals.toString()
                    binding.tvApprovedLeaves.text = arrpovedLeaves.toString()
                    binding.tvRejectedLeaves.text = rejectedLeaves.toString()
                    binding.tvLeaveTitle.text = "$totalLeaves Leaves"
                }
            }

            checkViewModel.getChecksUserByMonth(monthYearStr, userId).observe(this) { checks ->
                var presentCount = 0
                var absentCount = 0
                var lateCount = 0

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val selectedMonthStart = dateFormat.parse("$monthYearStr-01")!!
                calendar.time = selectedMonthStart

                val selectedMonth = calendar.get(Calendar.MONTH)
                val selectedYear = calendar.get(Calendar.YEAR)

                val today = Calendar.getInstance()

                val groupedChecks = checks.groupBy { it.date }

                while (
                    calendar.get(Calendar.MONTH) == selectedMonth &&
                    calendar.get(Calendar.YEAR) == selectedYear &&
                    !calendar.after(today)
                ) {
                    val dateStr = dateFormat.format(calendar.time)
                    val checksForDay = groupedChecks[dateStr]

                    if (checksForDay != null && checksForDay.isNotEmpty()) {
                        for (check in checksForDay) {
                            val (hourIn, _) = timeToIntPair(check.checkInTime)
                            if ((hourIn in 9 until 13) || (hourIn in 15 until 19)) {
                                lateCount++
                            } else {
                                presentCount++
                            }
                        }
                    } else {
                        absentCount++
                    }

                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                binding.tvPresentCount.text = presentCount.toString()
                binding.tvAbsentCount.text = absentCount.toString()
                binding.tvLateCount.text = lateCount.toString()
            }

            dialog.dismiss()
        }
        recyclerView.adapter = monthAdapter
        dialog.show()
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