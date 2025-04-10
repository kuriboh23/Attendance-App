package com.example.project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_table")
data class Check(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val checkInTime: String,
    val checkOutTime: String,
    val durationInSecond: Long,
    val isCompleted: Boolean = false
)
