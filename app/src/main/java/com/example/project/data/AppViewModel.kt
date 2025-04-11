package com.example.project.data


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
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