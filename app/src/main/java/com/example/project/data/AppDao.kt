package com.example.project.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {

    @Insert
    suspend fun insertCheck(check: Check):Long

    @Query("SELECT * FROM check_table WHERE :user_id == userId ORDER BY id ASC")
    fun getAllUserChecks(user_id:Long):LiveData<List<Check>>

    @Query("SELECT * FROM check_table WHERE date == :date AND :user_id == userId ORDER BY id ASC")
    fun getChecksUserByDate(date: String, user_id:Long): LiveData<List<Check>>

    @Query("SELECT * FROM check_table ORDER BY id ASC")
    fun getAllChecks():LiveData<List<Check>>

    @Query("DELETE FROM check_table")
    fun deleteAllChecks()

    @Query("SELECT * FROM check_table WHERE userId = :user_id AND date BETWEEN :startOfWeek AND :endOfWeek")
    fun getChecksByWeek(user_id: Long, startOfWeek: String, endOfWeek: String): LiveData<List<Check>>

}

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User):Long

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): LiveData<User>

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("DELETE FROM users WHERE id = :userId")
    fun deleteUserById(userId: Long)

}

@Dao
interface TimeManagerDao {

    @Insert
    suspend fun insertTimeManager(timeManager: TimeManager):Long

    @Query("SELECT * FROM time_manager")
    fun getAllTimeManagers(): LiveData<List<TimeManager>>

    @Query("DELETE FROM time_manager")
    fun deleteAllTimeManagers()

    // Fixing query for fetching all time managers by userId
    @Query("SELECT * FROM time_manager WHERE userId = :user_id ORDER BY id ASC")
    fun getAllUserTimeManagers(user_id: Long): LiveData<List<TimeManager>>

    @Query("SELECT * FROM time_manager WHERE date LIKE :monthYear || '%' AND userId = :user_id ORDER BY id ASC")
    fun getTimeManagersByMonth(monthYear: String, user_id: Long): LiveData<List<TimeManager>>

}

@Dao
interface LeaveDao{
    @Insert
    suspend fun insertLeave(leave: Leave):Long

    @Query("SELECT * FROM leave_table ORDER BY id ASC")
    fun getAllLeaves():LiveData<List<Leave>>

    @Query("SELECT * FROM leave_table WHERE :user_id == userId ORDER BY id ASC")
    fun getAllUserLeaves(user_id:Long):LiveData<List<Leave>>

    @Delete
    suspend fun deleteLeave(leave: Leave)

/*
    @Query("SELECT * FROM leave_table WHERE date == :date AND :user_id == userId ORDER BY id ASC")
    fun getLeavesUserByDate(date: String, user_id:Long): LiveData<List<Leave>>

    @Query("SELECT * FROM leave_table WHERE userId = :user_id AND date BETWEEN :startOfWeek AND :endOfWeek")
    fun getLeavesByWeek(user_id: Long, startOfWeek: String, endOfWeek: String): LiveData<List<Leave>>
*/

    @Query("SELECT * FROM leave_table WHERE date LIKE :monthYear || '%' AND userId = :user_id ORDER BY id ASC")
    fun getLeavesByMonth(monthYear: String, user_id: Long): LiveData<List<Leave>>

    @Query("SELECT COUNT(*) FROM leave_table WHERE userId = :userId AND type = 'Casual' AND status = 'Approved'")
    fun getCasualUsed(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM leave_table WHERE userId = :userId AND type = 'Casual'")
    fun getTotalCasual(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM leave_table WHERE userId = :userId AND type = 'Sick' AND status = 'Approved'")
    fun getSickUsed(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM leave_table WHERE userId = :userId AND type = 'Sick'")
    fun getTotalSick(userId: Long): Flow<Int>

    @Query("SELECT * FROM leave_table WHERE userId = :userId AND status = :status AND type = :type")
    fun getLeavesByStatusAndType(userId: Long, status: String, type: String): LiveData<List<Leave>>

    @Query("SELECT * FROM leave_table WHERE status = :status")
    fun getLeavesByStatus(status: String): LiveData<List<Leave>>
}