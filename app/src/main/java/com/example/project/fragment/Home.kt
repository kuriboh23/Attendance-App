package com.example.project.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.project.CheckInPrefs
import com.example.project.R
import com.example.project.ScanActivity
import com.example.project.UserPrefs
import com.example.project.data.CheckViewModel
import com.example.project.data.Check
import com.example.project.data.TimeManager
import com.example.project.data.TimeManagerViewModel
import com.example.project.data.UserViewModel
import com.example.project.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() {

    private val requiredQrText = "Yakuza"

    private var checkInTime: Long? = null
    private var isCheckedIn = false

    private val timeFormatterHM = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private val hoursFormat = SimpleDateFormat("HH", Locale.getDefault())
    private val minutesFormat = SimpleDateFormat("mm", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val now = System.currentTimeMillis()
    private val dayName = dayNameFormat.format(Date())
    private val day = dayFormat.format(Date(now))
    private val month = monthFormat.format(Date(now))
    private val year = yearFormat.format(Date(now))

    private val handler = Handler()
    private lateinit var timeRunnable: Runnable
    private lateinit var timeHourRunnable: Runnable

    private lateinit var binding: FragmentHomeBinding

    private lateinit var attendanceViewModel: CheckViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var timeManagerViewModel: TimeManagerViewModel


    private lateinit var userId: String

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startQrScan()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to scan",
                    Toast.LENGTH_SHORT
                ).show()
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        attendanceViewModel = ViewModelProvider(this)[CheckViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        timeManagerViewModel = ViewModelProvider(this)[TimeManagerViewModel::class.java]

        userId = UserPrefs.loadUserId(requireContext()).toString()

        binding.tvDate.text = "$month $day, $year - $dayName"

        startTimeCheck(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startQrScan()
        }

        val savedState = CheckInPrefs.loadCheckInState(requireContext(), userId)

        if (savedState.checkInStr != null) {
            binding.tvCheckInTime.text = savedState.checkInStr
            checkInTime = if (savedState.isCheckedIn) savedState.checkInMillis else null
            isCheckedIn = savedState.isCheckedIn
        }
        if (savedState.checkOutStr != null) {
            binding.tvCheckOutTime.text = savedState.checkOutStr
        }
        if (savedState.duration != null) {
            binding.tvTotalHours.text = savedState.duration
        }

        if (savedState.isCheckedIn) {
            binding.checkBtnName.text = "Check Out"
            binding.checkInBtn.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.checkOut
                )
            )
            binding.cardCheckInButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.checkOutLight
                )
            )
        } else {
            binding.checkBtnName.text = "Check In"
            binding.checkInBtn.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.mainColor
                )
            )
            binding.cardCheckInButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondColor
                )
            )
        }

        userViewModel.getUserById(userId.toLong()).observe(viewLifecycleOwner) { user ->
            val lastName = user.lastName
            binding.tvGreeting.text = "Hey, $lastName"
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        startUpdatingTime()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeRunnable)
        if (::timeHourRunnable.isInitialized) {
            handler.removeCallbacks(timeHourRunnable)
        }
    }

    private fun startQrScan() {
        binding.checkInBtn.setOnClickListener {
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
                binding.tvCheckInTime.text = checkInString
                binding.checkBtnName.text = "Check Out"

                isCheckedIn = true
                CheckInPrefs.saveCheckIn(requireContext(), userId, true, now, checkInString)
                CheckInPrefs.saveCheckOut(requireContext(), userId, true, "00:00", "00:00")

                binding.tvCheckOutTime.text = "00:00"
                binding.tvTotalHours.text = "00:00"

                binding.checkInBtn.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.checkOut
                    )
                )
                binding.cardCheckInButton.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.checkOutLight
                    )
                )

            } else {
                // Second scan: Check-out
                checkOutFunction()
            }
        } else {
            Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkOutFunction() {
        val now = System.currentTimeMillis()
        val checkOutString = timeFormatterHM.format(Date(now))
        binding.tvCheckOutTime.text = checkOutString
        binding.checkBtnName.text = "Check In"

        val durationMillis = now - (checkInTime ?: 0L)
        val durationInSeconds = durationMillis / 1000
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val durationStr = "${hours}h ${minutes}m"
        binding.tvTotalHours.text = durationStr

        CheckInPrefs.saveCheckOut(requireContext(), userId, false, checkOutString, durationStr)

        checkInTime = null
        isCheckedIn = false

        binding.checkInBtn.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.mainColor
            )
        )
        binding.cardCheckInButton.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.secondColor
            )
        )

        val savedState = CheckInPrefs.loadCheckInState(requireContext(), userId)
        var checkInTimeMillis = savedState.checkInStr
        checkInTimeMillis = checkInTimeMillis.toString()

        val currentDateStr = dateFormatter.format(Date(now))
        insertCheckToDatabase(currentDateStr, checkInTimeMillis, checkOutString, durationInSeconds)
    }

    private fun insertCheckToDatabase(
        date: String,
        checkInTime: String,
        checkOutTime: String,
        durationInSecond: Long
    ) {
        val check = Check(0, date, checkInTime, checkOutTime, durationInSecond, userId.toLong())
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
        binding.tvTime.text = "$currentTimeStr"
    }

    /*    private fun updateUI() {
            val savedState = CheckInPrefs.loadCheckInState(requireContext(), userId)

            binding.tvCheckInTime.text = savedState.checkInStr ?: "00:00"
            binding.tvCheckOutTime.text = savedState.checkOutStr ?: "00:00"
            binding.tvTotalHours.text = savedState.duration ?: "0h 0m"
            binding.checkBtnName.text = if (savedState.isCheckedIn) "Check Out" else "Check In"
            isCheckedIn = savedState.isCheckedIn

            CheckInPrefs.resetCheckInData(requireContext(), userId)
            Toast.makeText(requireContext(), "Check-in data reset", Toast.LENGTH_SHORT).show()
        }*/

    private fun startTimeCheck(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lastRunDate = prefs.getString("summary_last_run_date", null)
        val today = dateFormatter.format(Date(now))

        // If already run today, skip it
        if (lastRunDate == today) {
            println("Summary already run today. Skipping.")
            return
        }

        val now = System.currentTimeMillis()
        val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(now)).toInt()

        // Already past 12 PM? Run immediately and mark it as done
        if (currentHour >= 24) {
            timeManager()
            // Save today's date to avoid running again
            prefs.edit().putString("summary_last_run_date", today).apply()
            return
        }

        // Otherwise start checking every minute
        timeHourRunnable = object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()
                val currentHour =
                    SimpleDateFormat("HH", Locale.getDefault()).format(Date(now)).toInt()
                val currentMinute = minutesFormat.format(Date(now)).toInt()

                if (currentHour >= 24) {
                    timeManager()

                    // Save today's date to mark as done
                    prefs.edit().putString("summary_last_run_date", today).apply()
                    handler.removeCallbacks(this)
                } else {
                    handler.postDelayed(this, 60000)
                }
            }
        }
        handler.post(timeHourRunnable)
    }

    private fun timeManager() {
        val now = System.currentTimeMillis()
        val currentDateStr = dateFormatter.format(Date(now))

        userId = UserPrefs.loadUserId(requireContext()).toString()

        val userCheckByDate =
            attendanceViewModel.getChecksUserByDate(currentDateStr, userId.toLong())

        userCheckByDate.observe(viewLifecycleOwner) { checks ->
            if (checks.isNotEmpty()) {
                var workTime = 0
                var extraTime = 0
                var absent = false
                for (check in checks) {
                    workTime += check.durationInSecond.toInt()
                }

                workTime /= 3600
                if (workTime > 8) {
                    extraTime = workTime - 8
                    workTime = 8
                } else if (workTime < 8) {
                    absent = true
                }

                val timeManager =
                    TimeManager(0, currentDateStr, workTime, extraTime, absent, userId.toLong())
                timeManagerViewModel.insertTimeManager(timeManager)
                Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
