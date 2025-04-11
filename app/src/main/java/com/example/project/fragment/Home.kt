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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.example.project.CheckInPrefs
import com.example.project.R
import com.example.project.ScanActivity
import com.example.project.UserPrefs
import com.example.project.data.CheckViewModel
import com.example.project.data.Check
import com.example.project.data.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private val requiredQrText = "Yakuza"

    private var checkInTime: Long? = null

//    val savedData = CheckInPrefs.loadCheckInState(requireContext())

    private var isCheckedIn = false

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
    private lateinit var profileImg: ImageView

    private lateinit var attendanceViewModel: CheckViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var userId:String

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

        attendanceViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        checkInTimeText = view.findViewById(R.id.tvCheckInTime)
        checkOutTimeText = view.findViewById(R.id.tvCheckOutTime)
        durationText = view.findViewById(R.id.tvTotalHours)
        scanButton = view.findViewById(R.id.checkIn_btn)
        currentTimeText = view.findViewById(R.id.tvTime)
        currentDate = view.findViewById(R.id.tvDate)
        checkBtnName = view.findViewById(R.id.checkBtnName)
        cardCheckInButton = view.findViewById(R.id.cardCheckInButton)
        profileImg = view.findViewById(R.id.ivProfile)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        currentDate.text = "$month $day, $year - $dayName"

        profileImg.setOnClickListener {
        attendanceViewModel.deleteAllChecks()
            CheckInPrefs.resetCheckInData(requireContext(),userId)
            Toast.makeText(requireContext(), "Check-in data reset", Toast.LENGTH_SHORT).show()
//            updateUI()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startQrScan()
        }

        userId = UserPrefs.loadUserId(requireContext()).toString()

        val savedState = CheckInPrefs.loadCheckInState(requireContext(),userId)

        if (savedState.checkInStr != null) {
            checkInTimeText.text = savedState.checkInStr
            checkInTime = if (savedState.isCheckedIn) savedState.checkInMillis else null
            isCheckedIn = savedState.isCheckedIn
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

        userViewModel.getUserById(userId.toLong()).observe(viewLifecycleOwner) { user ->
            val fullName = user.fullName
            val lastName = fullName.substringAfterLast(" ")
            tvGreeting.text = "Hey, $lastName"
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
            if (isCheckedIn == false) {
                // First scan: Check-in
                checkInTime = now
                val checkInString = timeFormatterHM.format(Date(now))
                checkInTimeText.text = checkInString
                checkBtnName.text = "Check Out"

                isCheckedIn = true
                CheckInPrefs.saveCheckIn(requireContext(),userId, true, now,checkInString)
                CheckInPrefs.saveCheckOut(requireContext(),userId,true, "00:00", "00:00")

                checkOutTimeText.text = "00:00"
                durationText.text = "00:00"

                scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOut))
                cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.checkOutLight))

            } else {
                // Second scan: Check-out
                checkOutFunction()

//                if (isFirstCheckout) {
//                    CheckInPrefs.saveIsFirstCheckout(requireContext(),false)
//                    isFirstCheckout = false
//
//                    checkOutFunction()
//                } else {
//                    checkOutFunction()
//                    updateUI()
//                }
            }
        } else {
            Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkOutFunction(){
        val now = System.currentTimeMillis()
        val checkOutString = timeFormatterHM.format(Date(now))
        checkOutTimeText.text = checkOutString
        checkBtnName.text = "Check In"

        val durationMillis = now - (checkInTime ?: 0L)
        val durationInSeconds = durationMillis / 1000
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val durationStr = "${hours}h ${minutes}m"
        durationText.text = durationStr

        CheckInPrefs.saveCheckOut(requireContext(),userId, false,checkOutString, durationStr)

        checkInTime = null
        isCheckedIn = false

        scanButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainColor))
        cardCheckInButton.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondColor))

        // Insert check-out data into database
        val NewsavedState = CheckInPrefs.loadCheckInState(requireContext(),userId)
        var checkInTimeMillis = NewsavedState.checkInStr
        checkInTimeMillis = checkInTimeMillis.toString()
        insertCheckToDatabase(now, checkInTimeMillis, checkOutString, durationInSeconds)
    }

    private fun insertCheckToDatabase(date: Long, checkInTime: String, checkOutTime: String, durationInSecond: Long) {
        val check = Check(0, date, checkInTime, checkOutTime, durationInSecond,userId.toLong())
        attendanceViewModel.addCheck(check)
        Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()
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

    // New method to manually update UI after resetting check-in data
    private fun updateUI() {
        val savedState = CheckInPrefs.loadCheckInState(requireContext(),userId)

        // Reset the text values based on the new state
        checkInTimeText.text = savedState.checkInStr ?: "00:00"
        checkOutTimeText.text = savedState.checkOutStr ?: "00:00"
        durationText.text = savedState.duration ?: "0h 0m"
        checkBtnName.text = if (savedState.isCheckedIn) "Check Out" else "Check In"
        isCheckedIn = savedState.isCheckedIn

        // Reset check-in data after second check-out scan
        CheckInPrefs.resetCheckInData(requireContext(),userId)
        Toast.makeText(requireContext(), "Check-in data reset", Toast.LENGTH_SHORT).show()

    }
}