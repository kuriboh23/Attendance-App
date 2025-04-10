package com.example.project.data


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository
    val allAttendances: LiveData<List<Check>>

    init {
        val attendanceDao = AppDatabase.getDatabase(application).attendanceDao()
        repository = AttendanceRepository(attendanceDao)
        allAttendances = repository.allAttendances
    }

    fun addCheck(check: Check){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCheck(check)
        }
    }

    fun deleteAllChecks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllChecks()
        }
    }

}