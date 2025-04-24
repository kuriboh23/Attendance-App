package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.Check
import com.example.project.data.CheckViewModel
import com.example.project.data.TimeManagerViewModel
import com.example.project.databinding.FragmentAttendanceBinding
import com.example.project.fragment.list.CheckAdapter
import com.example.project.function.Function.showCustomToast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Attendance : Fragment() {

    private lateinit var binding: FragmentAttendanceBinding
    private lateinit var checkAdapter: CheckAdapter
    private lateinit var attendanceViewModel: CheckViewModel
    private lateinit var timeManagerViewModel: TimeManagerViewModel

    private val now = System.currentTimeMillis()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthNameFormat = SimpleDateFormat("MMMM", Locale.getDefault())

    private lateinit var startOfWeek: String
    private lateinit var endOfWeek: String
    private lateinit var monthYear: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        val userId = UserPrefs.loadUserId(requireContext())

        checkAdapter = CheckAdapter()
        binding.RecView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = checkAdapter
        }

        attendanceViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        timeManagerViewModel = ViewModelProvider(this)[TimeManagerViewModel::class.java]

        binding.loadingOverlay.visibility = View.VISIBLE

        val currentDateStr = dateFormatter.format(Date(now))
        val monthNameYear = monthNameYearFormat.format(now)
        val monthName = monthNameFormat.format(now)
        val weekOfMonth = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

        attendanceViewModel.getChecksUserByDate(currentDateStr, userId).observe(viewLifecycleOwner) { todayChecks ->
            checkAdapter.setData(todayChecks)

            binding.tvMonthYear.text = monthNameYear
            binding.summaryText.text = "Summary of $monthName"
            binding.weeksText.text = "Week $weekOfMonth"

            hideLoadingOverlayWithFade()
        }

        // Load TimeManager
        timeManagerViewModel.getTimeManagersByMonth(monthYearFormat.format(now), userId).observe(viewLifecycleOwner) { timeManager ->
            hideLoadingOverlayWithFade()
            binding.extraTimeTxt.text = timeManager.sumOf { it.extraTime }.toString()
            binding.absentTxt.text = timeManager.count { it.absent }.toString()
        }

        binding.filterMouth.setOnClickListener {
            filterByMonth(userId, dateFormatter)
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun filterByMonth(userId: Long, dateFormat: SimpleDateFormat) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date to view its week")
            .build()

        datePicker.show(childFragmentManager, "WEEK_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }

            val startWeek = calendar.time
            calendar.add(Calendar.DAY_OF_WEEK, 4)
            val endWeek = calendar.time

            startOfWeek = dateFormat.format(startWeek)
            endOfWeek = dateFormat.format(endWeek)
            monthYear = monthYearFormat.format(startWeek)

            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
            val monthNameYear = monthNameYearFormat.format(startWeek)
            val monthName = monthNameFormat.format(startWeek)

            binding.loadingOverlay.visibility = View.VISIBLE

            attendanceViewModel.getChecksByWeek(userId, startOfWeek, endOfWeek).observe(viewLifecycleOwner) { checks ->
                hideLoadingOverlayWithFade()
                if (checks.isNotEmpty()) {
                    checkAdapter.setData(checks)
                    binding.tvMonthYear.text = monthNameYear
                    binding.summaryText.text = "Summary of $monthName"
                    binding.weeksText.text = "Week $weekOfMonth"
                } else {
                    requireContext().showCustomToast("No Checks found", R.layout.error_toast)
                }
            }

            timeManagerViewModel.getTimeManagersByMonth(monthYear, userId).observe(viewLifecycleOwner) { timeManager ->
                hideLoadingOverlayWithFade()
                binding.extraTimeTxt.text = timeManager.sumOf { it.extraTime }.toString()
                binding.absentTxt.text = timeManager.count { it.absent }.toString()
            }
        }
    }

    private fun hideLoadingOverlayWithFade() {
        binding.loadingOverlay.animate()
            .setStartDelay(50)
            .setDuration(150)
            .withEndAction { binding.loadingOverlay.visibility = View.GONE }
            .start()
    }
}
