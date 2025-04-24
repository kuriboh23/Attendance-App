package com.example.project.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )

    ]
)
data class Check(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String,
    val durationInSecond: Long,
    @ColumnInfo(name = "userId") val userId: Long

)