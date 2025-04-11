package com.example.project.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CheckDao {

    @Insert
    suspend fun insertCheck(check: Check):Long

    @Query("SELECT * FROM check_table WHERE :user_id == userId ORDER BY id ASC")
    fun getAllUserChecks(user_id:Long):LiveData<List<Check>>

    @Query("SELECT * FROM check_table ORDER BY id ASC")
    fun getAllChecks():LiveData<List<Check>>

    @Query("DELETE FROM check_table")
    fun deleteAllChecks()
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