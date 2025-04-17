package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityApplyLeaveBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplyLeave: AppCompatActivity() {

    lateinit var binding: ActivityApplyLeaveBinding
    lateinit var type:String

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
            val date = binding.dateEditText.text.toString()
            val note = binding.tvNote.text.toString()

            if (date.isNotEmpty() && type.isNotEmpty() && note.isNotEmpty()) {
                println("Date: $date, Type: $type, Note: $note")
            }
            finish()
        }

    }
}