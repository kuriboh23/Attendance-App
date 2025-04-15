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
import com.example.project.fragment.list.MonthItem
import com.example.project.fragment.list.YearHeader
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

        binding.filterMouth.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_date, null)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)

            // Set up RecyclerView
            val recyclerView: RecyclerView = view.findViewById(R.id.month_list)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Prepare data
            val items = mutableListOf<YearHeader>()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)

            // Add years and months (e.g., 2023 to 2024)
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
            // Set up adapter
            val adapter = MonthAdapter(items) { selectedMonth ->
                val monthYearStr = "${selectedMonth.year}-${selectedMonth.monthNumber.toString().padStart(2, '0')}"

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
            adapter.notifyDataSetChanged()
            recyclerView.adapter = adapter

            dialog.show()
        }

        return binding.root
    }

}