package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.adapter.MonthAdapter
import com.example.project.data.TimeManagerViewModel
import com.example.project.databinding.FragmentSalaryBinding
import com.example.project.fragment.list.DateItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Salary : Fragment() {

    lateinit var binding: FragmentSalaryBinding

    private lateinit var timeManagerViewModel: TimeManagerViewModel

    private val now = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSalaryBinding.inflate(inflater, container, false)

        timeManagerViewModel = ViewModelProvider(this)[TimeManagerViewModel::class.java]
        val userId = UserPrefs.loadUserId(requireContext())

        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        val monthYear = monthYearFormat.format(now)
        val monthNameYear = monthNameYearFormat.format(now)

        timeManagerViewModel.getTimeManagersByMonth(monthYear, userId)
            .observe(viewLifecycleOwner) { timeManagers ->
                binding.tvMonthYear.text = monthNameYear
                val extraTime = timeManagers.sumOf { it.extraTime }
                val workTime = timeManagers.sumOf { it.workTime }
                val salaryNet = (workTime + extraTime) * 200
                binding.tvSalaryNet.text = "MAD $salaryNet"
            }

/*        binding.filterMouth.setOnClickListener {
            filterByMonth(userId)
        }*/

        binding.filterMouth.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_date, null)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)

            // Set up RecyclerView
            val recyclerView: RecyclerView = view.findViewById(R.id.month_list)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Prepare data
            val items = mutableListOf<DateItem>()
            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

            // Add years and months (e.g., 2023 to 2024)
            for (year in currentYear - 2..currentYear) {
                // Expand the current year by default
                val isCurrent = year == currentYear
                items.add(DateItem.YearHeader(year, isExpanded = isCurrent))

                months.forEachIndexed { index, month ->
                    val monthNumber = index + 1
                    val isSelected = isCurrent && monthNumber == currentMonth
                    items.add(DateItem.MonthItem(monthNumber, month, year, isSelected))
                }
            }

            // Set up adapter
            val adapter = MonthAdapter(items) { selectedMonth ->
                val monthYearStr = "${selectedMonth.year}-${selectedMonth.monthNumber.toString().padStart(2, '0')}"
                items.forEach { when(it) {
                    is DateItem.MonthItem -> it.isSelected = false
                    is DateItem.YearHeader -> it.isExpanded = false
                }}
                selectedMonth.isSelected = true

                timeManagerViewModel.getTimeManagersByMonth(monthYearStr, userId)
                    .observe(viewLifecycleOwner) { timeManagers ->
                        binding.tvMonthYear.text = "${selectedMonth.monthName} ${selectedMonth.year}"
                        val extraTime = timeManagers.sumOf { it.extraTime }
                        val workTime = timeManagers.sumOf { it.workTime }
                        val salaryNet = (workTime + extraTime) * 200
                        binding.tvSalaryNet.text = "MAD $salaryNet"
                    }
                dialog.dismiss()
            }
            recyclerView.adapter = adapter

            dialog.show()
        }

        return binding.root
    }/*
    private fun setupYearMonthSpinner(spinner: Spinner) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 50..currentYear + 10).toList() // Adjust range as needed
        val yearMonthList = mutableListOf<String>()

        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        // Create entries like "2023 - January"
        years.forEach { year ->
            months.forEach { month ->
                yearMonthList.add("$year - $month")
            }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            yearMonthList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner.adapter = adapter
    }

    // Helper function to convert month name to number (e.g., "January" -> "01")
    private fun monthToNumber(month: String): String {
        return when (month) {
            "January" -> "01"
            "February" -> "02"
            "March" -> "03"
            "April" -> "04"
            "May" -> "05"
            "June" -> "06"
            "July" -> "07"
            "August" -> "08"
            "September" -> "09"
            "October" -> "10"
            "November" -> "11"
            "December" -> "12"
            else -> "01"
        }
    }*/

    @SuppressLint("SetTextI18n")
    private fun filterByMonth(userId: Long) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date (we'll use its month)")
            .build()

        datePicker.show(childFragmentManager, "MONTH_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
            }

            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            val monthYear = monthYearFormat.format(calendar.time)
            val monthNameYear = monthNameYearFormat.format(calendar.time)

            timeManagerViewModel.getTimeManagersByMonth(monthYear, userId)
                .observe(viewLifecycleOwner) { timeManagers ->
                    binding.tvMonthYear.text = monthNameYear
                    val extraTime = timeManagers.sumOf { it.extraTime }
                    val workTime = timeManagers.sumOf { it.workTime }
                    val salaryNet = (workTime + extraTime) * 200
                    binding.tvSalaryNet.text = "MAD $salaryNet"
                }
        }
    }

}