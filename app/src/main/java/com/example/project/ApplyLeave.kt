package com.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.example.project.data.Leave
import com.example.project.data.LeaveViewModel
import com.example.project.databinding.ActivityApplyLeaveBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplyLeave : AppCompatActivity() {

    lateinit var binding: ActivityApplyLeaveBinding
    lateinit var type: String

    lateinit var leaveViewModel: LeaveViewModel
    private val now = System.currentTimeMillis()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityApplyLeaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = UserPrefs.loadUserId(this)

        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

        binding.startDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date")
                .build()
            datePicker.show(this.supportFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener { selectedDate ->
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
                binding.startDate.setText(formattedDate)
            }
        }
        binding.endDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date")
                .build()
            datePicker.show(this.supportFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener { selectedDate ->
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
                binding.endDate.setText(formattedDate)
            }
        }

        binding.tvbackArrow.setOnClickListener {
            finish()
        }

        val checkedId = binding.leaveTypeGroup.checkedButtonId
        val selectedButton = binding.root.findViewById<MaterialButton>(checkedId)
        type = selectedButton.text.toString()

        binding.leaveTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_casual -> {
                        type = "Casual"
                    }
                    R.id.btn_sick -> {
                        type = "Sick"
                    }
                }
            }
        }

        binding.applyBtn.setOnClickListener {
            val startDate = binding.startDate.text.toString()
            val endDate = binding.endDate.text.toString()
            val date = "$startDate - $endDate"
            val note = binding.tvNote.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()  && type.isNotEmpty() && note.isNotEmpty()) {
                // Prepare the result data
                val resultIntent = Intent()
                resultIntent.putExtra("DATE", date)
                resultIntent.putExtra("TYPE", type)
                resultIntent.putExtra("NOTE", note)

                // Set the result and finish the activity
                setResult(RESULT_OK, resultIntent)
                val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))

                val leave = Leave(0,currentDateStr,startDate,endDate,type,note,"Pending",userId)
                leaveViewModel.insertLeave(leave)

                finish()
            }
        }
    }
}