package com.example.project.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.UserPrefs
import com.example.project.data.CheckViewModel
import com.example.project.data.TimeManagerViewModel
import com.example.project.databinding.FragmentAttendanceBinding
import com.example.project.fragment.list.CheckAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class Attendance : Fragment() {
    private lateinit var binding: FragmentAttendanceBinding

    private lateinit var checkAdapter: CheckAdapter
    private lateinit var attendanceViewModel: CheckViewModel
    private lateinit var timeManagerViewModel: TimeManagerViewModel

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var startOfWeek:String
    private lateinit var endOfWeek:String
    private lateinit var mouthYear:String

    private val now = System.currentTimeMillis()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAttendanceBinding.inflate( inflater, container, false)

/*        recyclerView = view.findViewById(R.id.RecView)
        img = view.findViewById(R.id.filterMouth)
        extraTimeTxt = view.findViewById(R.id.extraTimeTxt)
        absentTxt = view.findViewById(R.id.absentTxt)*/

        val userId = UserPrefs.loadUserId(requireContext())

        //RecyclerView
        binding.RecView.layoutManager = LinearLayoutManager(requireContext())
        checkAdapter = CheckAdapter()
        binding.RecView.adapter = checkAdapter

        attendanceViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        timeManagerViewModel = ViewModelProvider(this)[TimeManagerViewModel::class.java]

        val currentDateStr = dateFormatter.format(Date(now))

        val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthNameYear = monthNameYearFormat.format(now)

        val monthNameFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val monthName = monthNameFormat.format(now)
        val weekOfMonth = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

        attendanceViewModel.getChecksUserByDate(currentDateStr, userId)
            .observe(viewLifecycleOwner) { checks ->
                    checkAdapter.setData(checks)
                    binding.tvMonthYear.text = monthNameYear
                    binding.summaryText.text = "Summary of $monthName"
                    binding.weeksText.text = "$weekOfMonth Week"
            }

        binding.filterMouth.setOnClickListener {
            filterByMonth(userId,dateFormatter)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun filterByMonth(userId: Long,dateFormat: SimpleDateFormat) {
        val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pick a date we will use it's week")
            .build()

        datePicker.show(childFragmentManager, "WEEK_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection

            // Align to start of the week (Monday)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startWeek = calendar.time

            // End of week Friday)
            calendar.add(Calendar.DAY_OF_WEEK, 4)
            val endWeek = calendar.time

            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val monthNameYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

            startOfWeek = dateFormat.format(startWeek)
            endOfWeek = dateFormat.format(endWeek)
            mouthYear = monthYearFormat.format(startWeek)

            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
            val monthNameYear = monthNameYearFormat.format(startWeek)

            val monthNameFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val monthName = monthNameFormat.format(now)

            attendanceViewModel.getChecksByWeek(userId,startOfWeek,endOfWeek)
                .observe(viewLifecycleOwner) { checks ->
                    if (checks.isNotEmpty()) {
                    checkAdapter.setData(checks)
                        binding.tvMonthYear.text = monthNameYear
                        binding.summaryText.text = "Summary of $monthName"
                        binding.weeksText.text = "$weekOfMonth Week"
                    }else{
                        Toast.makeText(requireContext(), "No Checks found", Toast.LENGTH_SHORT).show()
                    }
                }

            timeManagerViewModel.getTimeManagersByMonth(mouthYear, userId)
                .observe(viewLifecycleOwner) { timeManager ->
                    val totalExtraTime = timeManager.sumOf { it.extraTime }
                    binding.extraTimeTxt.text = totalExtraTime.toString()

                    val totalAbsent = timeManager.count { it.absent }
                    binding.absentTxt.text = totalAbsent.toString()
                }
        }
    }

}