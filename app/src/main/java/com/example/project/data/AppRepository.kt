package com.example.project.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CheckRepository(private val attendanceDao: CheckDao) {
    val allAttendances: LiveData<List<Check>> = attendanceDao.getAllChecks()

    // Get all checks for a specific user
    fun getAllUserChecks(userId: Long): LiveData<List<Check>> {
        return attendanceDao.getAllUserChecks(userId)
    }

    suspend fun addCheck(check: Check){
        attendanceDao.insertCheck(check)
    }

    fun getChecksUserByDate(date: String, userId: Long): LiveData<List<Check>> {
        return attendanceDao.getChecksUserByDate(date, userId)
    }

    fun getChecksByWeek(userId: Long, startOfWeek: String, endOfWeek: String): LiveData<List<Check>> {
        return attendanceDao.getChecksByWeek(userId, startOfWeek, endOfWeek)
    }

    fun deleteAllChecks(){
        attendanceDao.deleteAllChecks()
    }

}

class UserRepository(private val userDao: UserDao) {
    val allUsers: LiveData<List<User>> = userDao.getAllUsers()

    fun getUserById(userId: Long): LiveData<User> {
        return userDao.getUserById(userId)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    fun deleteUserById(userId: Long) {
        userDao.deleteUserById(userId)
    }
}

class TimeManagerRepository(private val timeManagerDao: TimeManagerDao) {
    val allTimeManagers: LiveData<List<TimeManager>> = timeManagerDao.getAllTimeManagers()

    fun getAllUserTimeManagers(userId: Long): LiveData<List<TimeManager>> {
        return timeManagerDao.getAllUserTimeManagers(userId)
    }
    suspend fun insertTimeManager(timeManager: TimeManager) {
        timeManagerDao.insertTimeManager(timeManager)
    }
    fun deleteAllTimeManagers() {
        timeManagerDao.deleteAllTimeManagers()
    }
    fun getTimeManagersByMonth(monthYear: String, userId: Long): LiveData<List<TimeManager>> {
        return timeManagerDao.getTimeManagersByMonth(monthYear, userId)
    }


}

class LeaveRepository(private val leaveDao: LeaveDao) {
    val allLeaves: LiveData<List<Leave>> = leaveDao.getAllLeaves()

    fun getAllUserLeaves(userId: Long): LiveData<List<Leave>> {
        return leaveDao.getAllUserLeaves(userId)
    }

    suspend fun insertLeave(leave: Leave) {
        leaveDao.insertLeave(leave)
    }

    suspend fun deleteLeave(leave: Leave) {
        leaveDao.deleteLeave(leave)
    }

    fun getLeavesByMonth(monthYear: String, userId: Long): LiveData<List<Leave>> {
        return leaveDao.getLeavesByMonth(monthYear, userId)
    }

    fun getLeavesByStatusAndType(userId: Long, status: String, type: String): LiveData<List<Leave>> {
        return leaveDao.getLeavesByStatusAndType(userId, status, type)
    }
    fun getLeavesByStatus(status: String): LiveData<List<Leave>> {
        return leaveDao.getLeavesByStatus(status)
    }


    fun getLeaveSummary(userId: Long): Flow<LeaveSummary> {
        return combine(
            leaveDao.getCasualUsed(userId),
            leaveDao.getTotalCasual(userId),
            leaveDao.getSickUsed(userId),
            leaveDao.getTotalSick(userId)
        ) { casualUsed, casualTotal, sickUsed, sickTotal ->
            LeaveSummary(
                casualUsed = casualUsed,
                casualTotal = casualTotal,
                sickUsed = sickUsed,
                sickTotal = sickTotal
            )
        }
    }
}