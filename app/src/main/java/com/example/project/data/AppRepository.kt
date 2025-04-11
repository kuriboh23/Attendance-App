package com.example.project.data

import androidx.lifecycle.LiveData

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

    fun getChecksByDate(date: String, userId: Long): LiveData<List<TimeManager>> {
        return timeManagerDao.getChecksByDate(date, userId)
    }

}