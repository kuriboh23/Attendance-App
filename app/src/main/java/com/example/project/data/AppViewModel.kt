package com.example.project.data


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CheckViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CheckRepository
    val allAttendances: LiveData<List<Check>>

    init {
        val attendanceDao = AppDatabase.getDatabase(application).checkDao()
        repository = CheckRepository(attendanceDao)
        allAttendances = repository.allAttendances
    }

    // Initialize the check list for a specific user
    fun getAllUserChecks(userId: Long) = repository.getAllUserChecks(userId)

    fun addCheck(check: Check){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCheck(check)
        }
    }

    fun getChecksUserByDate(date: String, userId: Long): LiveData<List<Check>> {
        return repository.getChecksUserByDate(date, userId)
    }

    fun getChecksByWeek(userId: Long, startOfWeek: String, endOfWeek: String): LiveData<List<Check>> {
        return repository.getChecksByWeek(userId, startOfWeek, endOfWeek)
    }

    fun deleteAllChecks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllChecks()
        }
    }

}

class UserViewModel(application: Application): AndroidViewModel(application){
    private val repository: UserRepository
    val allUsers: LiveData<List<User>>

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        allUsers = repository.allUsers
    }
    fun getUserById(userId: Long): LiveData<User> {
        return repository.getUserById(userId)
    }

    fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUser(user)
        }
    }
    fun deleteUserById(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserById(userId)
        }
    }

}

class TimeManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeManagerRepository
    val allTimeManagers: LiveData<List<TimeManager>>

    init {
        val timeManagerDao = AppDatabase.getDatabase(application).timeManagerDao()
        repository = TimeManagerRepository(timeManagerDao)
        allTimeManagers = repository.allTimeManagers
    }
    fun getAllUserTimeManagers(userId: Long): LiveData<List<TimeManager>> {
        return repository.getAllUserTimeManagers(userId)
    }
    fun insertTimeManager(timeManager: TimeManager) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTimeManager(timeManager)
        }
    }
    fun deleteAllTimeManagers() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTimeManagers()
        }
    }
    fun getTimeManagersByMonth(monthYear: String, userId: Long): LiveData<List<TimeManager>> {
        return repository.getTimeManagersByMonth(monthYear, userId)
    }
}

class LeaveViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LeaveRepository
    val allLeaves: LiveData<List<Leave>>

    init {
        val leaveDao = AppDatabase.getDatabase(application).leaveDao()
        repository = LeaveRepository(leaveDao)
        allLeaves = repository.allLeaves
    }

    fun getAllUserLeaves(userId: Long): LiveData<List<Leave>> {
        return repository.getAllUserLeaves(userId)
    }
    fun insertLeave(leave: Leave) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLeave(leave)
        }
    }

    fun deleteLeave(leave: Leave) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLeave(leave)
        }
    }

    fun getLeavesByStatusAndType(userId: Long, status: String, type: String): LiveData<List<Leave>> {
        return repository.getLeavesByStatusAndType(userId, status, type)
    }

    fun getLeaveSummary(userId: Long): Flow<LeaveSummary> {
        return repository.getLeaveSummary(userId)
    }

    fun getLeavesByMonth(monthYear: String, userId: Long): LiveData<List<Leave>> {
        return repository.getLeavesByMonth(monthYear, userId)
    }
}