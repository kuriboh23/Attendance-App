package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityApplyLeaveBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplyLeave: AppCompatActivity() {
    lateinit var binding: ActivityApplyLeaveBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityApplyLeaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()

        binding.dateEditText.setOnClickListener {
            datePicker.show(this.supportFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate))
            binding.dateEditText.setText(formattedDate)
        }

        binding.tvbackArrow.setOnClickListener {
            finish()

        }

    }
}