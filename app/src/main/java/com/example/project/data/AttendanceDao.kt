package com.example.project.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insertCheck(check: Check):Long

    @Query("SELECT * FROM check_table ORDER BY id ASC")
    fun readAllChecks():LiveData<List<Check>>

}