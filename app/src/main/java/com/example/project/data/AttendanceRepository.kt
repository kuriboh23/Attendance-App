package com.example.project.data

import androidx.lifecycle.LiveData

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    val allAttendances: LiveData<List<Check>> = attendanceDao.readAllChecks()

    suspend fun addCheck(check: Check){
        attendanceDao.insertCheck(check)
    }

    fun deleteAllChecks(){
        attendanceDao.deleteAllChecks()
    }

}