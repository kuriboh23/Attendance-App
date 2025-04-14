package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.TimeManagerViewModel
import com.example.project.databinding.FragmentSalaryBinding
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

        binding.filterMouth.setOnClickListener {
            filterByMonth(userId)
        }

        return binding.root
    }

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