package com.example.project.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.project.R
import com.example.project.UserPrefs
import com.example.project.data.Leave
import com.example.project.data.LeaveViewModel
import com.example.project.databinding.ActivityApplyLeaveBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ApplyLeave : AppCompatActivity() {

    private lateinit var binding: ActivityApplyLeaveBinding
    private lateinit var leaveViewModel: LeaveViewModel
    private lateinit var type: String
    private val now = System.currentTimeMillis()
    private val calendar = Calendar.getInstance()

    // File attachment variables
    private var selectedFileUri: Uri? = null
    private var savedFilePath: String? = null

    // File picker launcher
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileNameFromUri(it) ?: "attachment_${System.currentTimeMillis()}"
            binding.tvAttachmentName.text = fileName
            savedFilePath = saveFileToStorage(it, fileName)
        } ?: run {
            binding.tvAttachmentName.text = "No file selected"
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyLeaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = UserPrefs.loadUserId(this)
        leaveViewModel = ViewModelProvider(this)[LeaveViewModel::class.java]

        // Initialize leave type
        type = when (binding.leaveTypeGroup.checkedButtonId) {
            R.id.btn_casual -> "Casual"
            R.id.btn_sick -> "Sick"
            else -> ""
        }

        // Date picker listeners
        binding.startDate.setOnClickListener { showDatePicker(true) }
        binding.endDate.setOnClickListener { showDatePicker(false) }

        // Back arrow click
        binding.tvbackArrow.setOnClickListener { finish() }

        // Leave type selection
        binding.leaveTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                type = when (checkedId) {
                    R.id.btn_casual -> "Casual"
                    R.id.btn_sick -> "Sick"
                    else -> ""
                }
            }
        }

        // Attach file button
        binding.btnAttachFile.setOnClickListener { checkStoragePermission() }

        // Apply button click
        binding.applyBtn.setOnClickListener { submitLeaveRequest(userId) }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()
        datePicker.show(this.supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
            if (isStartDate) {
                binding.startDate.setText(formattedDate)
            } else {
                binding.endDate.setText(formattedDate)
            }
        }
    }

    private fun checkStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+: Check granular permissions
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFilePicker()
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }

            else -> {
                // Android 12 and below: Check READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFilePicker()
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openFilePicker() {
        pickFileLauncher.launch("*/*") // Adjust MIME type as needed
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return null
    }

    private fun saveFileToStorage(uri: Uri, fileName: String): String? {
        try {
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val destFile = File(storageDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun submitLeaveRequest(userId: Long) {
        val startDate = binding.startDate.text.toString()
        val endDate = binding.endDate.text.toString()
        val date = "$startDate - $endDate"
        val note = binding.tvNote.text.toString()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show()
            return
        }
        if (type.isEmpty()) {
            Toast.makeText(this, "Please select a leave type", Toast.LENGTH_SHORT).show()
            return
        }
        if (note.isEmpty()) {
            Toast.makeText(this, "Please provide a note", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the result data
        val resultIntent = Intent()
        resultIntent.putExtra("DATE", date)
        resultIntent.putExtra("TYPE", type)
        resultIntent.putExtra("NOTE", note)
        resultIntent.putExtra("ATTACHMENT_PATH", savedFilePath) // Include attachment path

        // Set the result
        setResult(RESULT_OK, resultIntent)

        // Save to database
        val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        val leave = Leave(0,currentDateStr,startDate,endDate,type,note,"Pending",savedFilePath,userId)
        leaveViewModel.insertLeave(leave)

        finish()
    }
}