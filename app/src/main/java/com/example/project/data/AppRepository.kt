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