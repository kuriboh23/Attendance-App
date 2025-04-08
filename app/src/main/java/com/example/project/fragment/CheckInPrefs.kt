package com.example.project

import android.content.Context
import android.content.SharedPreferences

object CheckInPrefs {
    private const val PREF_NAME = "check_in_data"
    private const val KEY_CHECKED_IN = "checked_in"
    private const val KEY_CHECK_IN_TIME = "check_in_time"
    private const val KEY_CHECK_IN_STR = "check_in_str"
    private const val KEY_CHECK_OUT_STR = "check_out_str"
    private const val KEY_DURATION = "duration"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveCheckIn(context: Context, isCheckedIn: Boolean, timeMillis: Long, timeStr: String) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_CHECKED_IN, isCheckedIn)
            putLong(KEY_CHECK_IN_TIME, timeMillis)
            putString(KEY_CHECK_IN_STR, timeStr)
            apply()
        }
    }

    fun saveCheckOut(context: Context, checkOutStr: String, durationStr: String) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_CHECKED_IN, false)
            putString(KEY_CHECK_OUT_STR, checkOutStr)
            putString(KEY_DURATION, durationStr)
            apply()
        }
    }

    fun loadCheckInState(context: Context): CheckInData {
        val prefs = getPrefs(context)
        return CheckInData(
            isCheckedIn = prefs.getBoolean(KEY_CHECKED_IN, false),
            checkInMillis = prefs.getLong(KEY_CHECK_IN_TIME, 0L),
            checkInStr = prefs.getString(KEY_CHECK_IN_STR, null),
            checkOutStr = prefs.getString(KEY_CHECK_OUT_STR, null),
            duration = prefs.getString(KEY_DURATION, null)
        )
    }

    fun resetCheckInData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    data class CheckInData(
        val isCheckedIn: Boolean,
        val checkInMillis: Long,
        val checkInStr: String?,
        val checkOutStr: String?,
        val duration: String?
    )
}
