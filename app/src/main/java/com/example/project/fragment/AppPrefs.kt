package com.example.project

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object CheckInPrefs {
    private const val PREF_NAME = "check_in_data"
    private const val KEY_CHECKED_IN = "checked_in"
    private const val KEY_CHECK_IN_TIME = "check_in_time"
    private const val KEY_CHECK_IN_STR = "check_in_str"
    private const val KEY_CHECK_OUT_STR = "check_out_str"
    private const val KEY_DURATION = "duration"

    private fun getCheckPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveCheckIn(context: Context, isCheckedIn: Boolean, timeMillis: Long, timeStr: String) {
        getCheckPrefs(context).edit().apply {
            putBoolean(KEY_CHECKED_IN, isCheckedIn)
            putString(KEY_CHECK_IN_STR, timeStr)
            putLong(KEY_CHECK_IN_TIME, timeMillis)
            apply()
        }
    }

    fun saveCheckOut(context: Context,isCheckedIn: Boolean, checkOutStr: String, durationStr: String) {
        getCheckPrefs(context).edit().apply {
            putBoolean(KEY_CHECKED_IN, isCheckedIn)
            putString(KEY_CHECK_OUT_STR, checkOutStr)
            putString(KEY_DURATION, durationStr)
            apply()
        }
    }

//    fun saveIsFirstCheckout(context: Context,isFirstCheckOut: Boolean){
//        getPrefs(context).edit().apply {
//            putBoolean(KEY_IS_FIRST_CHECK_OUT, isFirstCheckOut)
//            apply()
//        }
//    }

    fun loadCheckInState(context: Context): CheckInData {
        val prefs = getCheckPrefs(context)
        return CheckInData(
            isCheckedIn = prefs.getBoolean(KEY_CHECKED_IN, false),
            checkInMillis = prefs.getLong(KEY_CHECK_IN_TIME, 0L),
            checkInStr = prefs.getString(KEY_CHECK_IN_STR, null),
            checkOutStr = prefs.getString(KEY_CHECK_OUT_STR, null),
            duration = prefs.getString(KEY_DURATION, null)
        )
    }

    fun resetCheckInData(context: Context) {
        getCheckPrefs(context).edit() { clear() }
    }

    data class CheckInData(
        val isCheckedIn: Boolean,
        val checkInMillis: Long,
        val checkInStr: String?,
        val checkOutStr: String?,
        val duration: String?
    )
}

object UserPrefs{
    private const val PREF_NAME = "user_data"
    private const val KEY_USER_ID = "user_id"

    private fun getUserPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: Long) {
        getUserPrefs(context).edit().apply {
            putLong(KEY_USER_ID, userId)
            apply()
        }
    }

    fun loadUserId(context: Context): Long {
        return getUserPrefs(context).getLong(KEY_USER_ID, 0L)
    }

    fun clearUserId(context: Context) {
        getUserPrefs(context).edit().apply {
            remove(KEY_USER_ID)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUserPrefs(context).contains(KEY_USER_ID)
    }

}
