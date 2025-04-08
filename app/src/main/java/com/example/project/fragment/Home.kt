package com.example.project.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.project.CheckInPrefs
import com.example.project.R
import com.example.project.ScanActivity
import com.example.project.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private val requiredQrText = "Yakuza"
    private var checkInTime: Long? = null
    private val timeFormatterHM = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

    private val hoursFormat = SimpleDateFormat("HH", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())

    private val now = System.currentTimeMillis()
    private val dayName = dayNameFormat.format(Date())
    private val day = dayFormat.format(Date(now))
    private val month = monthFormat.format(Date(now))
    private val year = yearFormat.format(Date(now))

    private val handler = Handler()
    private lateinit var timeRunnable: Runnable

    private lateinit var checkInTimeText: TextView
    private lateinit var checkOutTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var scanButton: CardView
    private lateinit var currentTimeText: TextView
    private lateinit var currentDate: TextView
    private lateinit var checkBtnName: TextView
    private lateinit var cardCheckInButton: CardView

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startQrScan()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to scan", Toast.LENGTH_SHORT).show()
            }
        }

    private val scanActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scannedText = result.data?.getStringExtra("SCAN_RESULT")
                scannedText?.let {
                    handleQrResult(it)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        checkInTimeText = view.findViewById(R.id.tvCheckInTime)
        checkOutTimeText = view.findViewById(R.id.tvCheckOutTime)
        durationText = view.findViewById(R.id.tvTotalHours)
        scanButton = view.findViewById(R.id.checkIn_btn)
        currentTimeText = view.findViewById(R.id.tvTime)
        currentDate = view.findViewById(R.id.tvDate)
        checkBtnName = view.findViewById(R.id.checkBtnName)
        cardCheckInButton = view.findViewById(R.id.cardCheckInButton)

        currentDate.text = "$month $day, $year - $dayName"

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startQrScan()
        }

//        New line Code

        val savedState = CheckInPrefs.loadCheckInState(requireContext())

        if (savedState.checkInStr != null) {
            checkInTimeText.text = savedState.checkInStr
            checkInTime = if (savedState.isCheckedIn) savedState.checkInMillis else null
        }
        if (savedState.checkOutStr != null) {
            checkOutTimeText.text = savedState.checkOutStr
        }
        if (savedState.duration != null) {
            durationText.text = savedState.duration
        }

        if (savedState.isCheckedIn) {
            checkBtnName.text = "Check Out"
            scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOut))
            cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOutLight))
        } else {
            checkBtnName.text = "Check In"
            scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainColor))
            cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondColor))
        }



        return view
    }

    override fun onStart() {
        super.onStart()
        startUpdatingTime()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeRunnable)
    }

    private fun startQrScan() {
        scanButton.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            scanActivityLauncher.launch(intent)
        }
    }

    private fun handleQrResult(scannedText: String) {
        if (scannedText == requiredQrText) {
            val now = System.currentTimeMillis()
            if (checkInTime == null) {
                checkInTime = now
                val checkInString = timeFormatterHM.format(Date(now))
                checkInTimeText.text = checkInString
                checkBtnName.text = "Check Out"

                CheckInPrefs.saveCheckIn(requireContext(), true, now, checkInString)

                scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOut))
                cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOutLight))
            } else {
                val checkOutTimeMillis = now
                val checkOutString = timeFormatterHM.format(Date(checkOutTimeMillis))
                checkOutTimeText.text = checkOutString
                checkBtnName.text = "Check In"

                val durationMillis = checkOutTimeMillis - (checkInTime ?: 0L)
                val durationInSeconds = durationMillis / 1000
                val hours = durationInSeconds / 3600
                val minutes = (durationInSeconds % 3600) / 60
                val durationStr = "${hours}h ${minutes}m"
                durationText.text = durationStr

                CheckInPrefs.saveCheckOut(requireContext(), checkOutString, durationStr)

                checkInTime = null
                scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainColor))
                cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondColor))
            }
        } else {
            Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startUpdatingTime() {
        timeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTimeText()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timeRunnable)
    }

    private fun updateCurrentTimeText() {
        val now = System.currentTimeMillis()
        val currentTimeStr = timeFormatter.format(Date(now))
        currentTimeText.text = "$currentTimeStr"
    }
}