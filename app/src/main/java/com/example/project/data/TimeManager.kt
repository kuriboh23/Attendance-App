package com.example.project.data


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_manager",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimeManager(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val workTime: Int,
    val extraTime: Int,
    val absent: Int,
    @ColumnInfo(name = "userId") val userId: Long
)
