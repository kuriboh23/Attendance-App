package com.example.project

import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var checkInTime: Long? = null
    var checkInTimeString: String? = null
    var checkOutTimeString: String? = null
    var durationString: String? = null
    var isCheckedIn: Boolean = false
}
