package com.example.project.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.example.project.R
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity00 : AppCompatActivity() {

    val requiredQrText = "Yakuza"
    private var checkInTime: Long? = null
    private val timeFormatterHM = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())


    val hoursFormat = SimpleDateFormat("HH", Locale.getDefault())
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())     // 07
    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault()) // April
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())  // 2025
    val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    val now = System.currentTimeMillis()

    val dayName = dayNameFormat.format(Date())
    val day = dayFormat.format(Date(now))
    val month = monthFormat.format(Date(now))
    val year = yearFormat.format(Date(now))

    private val handler = android.os.Handler()
    private lateinit var timeRunnable: Runnable

    lateinit var checkInTimeText: TextView
    lateinit var checkOutTimeText: TextView
    lateinit var durationText: TextView
    lateinit var scanButton: CardView
    lateinit var currentTimeText: TextView

    lateinit var currentDate: TextView
    lateinit var checkBtnName: TextView
    lateinit var cardCheckInButton: CardView

    // Permissions launcher
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startQrScan()
            } else {
                Toast.makeText(this, "Camera permission is required to scan", Toast.LENGTH_SHORT).show()
            }
        }

    // Activity result launcher for ScanActivity
    private val scanActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scannedText = result.data?.getStringExtra("SCAN_RESULT")
                scannedText?.let {
                    handleQrResult(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home0)

        checkInTimeText = findViewById(R.id.tvCheckInTime)
        checkOutTimeText = findViewById(R.id.tvCheckOutTime)
        durationText = findViewById(R.id.tvTotalHours)
        scanButton = findViewById(R.id.checkIn_btn)
        currentTimeText = findViewById(R.id.tvTime)

        currentDate = findViewById(R.id.tvDate)
        checkBtnName = findViewById(R.id.checkBtnName)
        cardCheckInButton = findViewById(R.id.cardCheckInButton)

        currentDate.text = "$month $day, $year - $dayName"

        // Request Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Permission already granted, initialize camera
            startQrScan()
        }
    }

    // Starts the QR scan process
    fun startQrScan() {
        scanButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            scanActivityLauncher.launch(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        startUpdatingTime()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeRunnable)
    }

    // Handling the result of QR scan
    private fun handleQrResult(scannedText: String) {
        if (scannedText == requiredQrText) {
                val now = System.currentTimeMillis()
            if (checkInTime == null) {
                // First scan -> Check In
                checkInTime = now
                val checkInString = timeFormatterHM.format(Date(now))
                checkInTimeText.text = "$checkInString"
                checkBtnName.text = "Check Out"

                scanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.checkOut))
                cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(this,
                    R.color.checkOutLight
                ))

//                checkOutTimeText.text = "00:00"
            } else {
                // Second scan -> Check Out
                val checkOutTimeMillis = now
                val checkOutString = timeFormatterHM.format(Date(checkOutTimeMillis))
                checkOutTimeText.text = "$checkOutString"
                checkBtnName.text = "Check In"

                scanButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.mainColor))
                cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(this,
                    R.color.secondColor
                ))

                // Calculate the duration
                val durationMillis = checkOutTimeMillis - (checkInTime ?: 0L)
                val durationInSeconds = durationMillis / 1000
                val hours = durationInSeconds / 3600
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60

                durationText.text = "${hours}h ${minutes}m"

//                checkInTimeText.text = "00:00"
                checkInTime = null
            }
        } else {
            // Invalid QR code content
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update the current time every second
    private fun startUpdatingTime() {
        timeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTimeText()
                handler.postDelayed(this, 1000) // Repeat every second
            }
        }
        handler.post(timeRunnable) // Start the loop
    }

    // Update the current time
    private fun updateCurrentTimeText() {
        val now = System.currentTimeMillis()
        val currentTimeStr = timeFormatter.format(Date(now))
        currentTimeText.text = "$currentTimeStr"
    }
}
