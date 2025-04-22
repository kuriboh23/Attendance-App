package com.example.project.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "leave_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )

    ]
)
data class Leave(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val startDate: String,
    val endDate: String,
    val type: String,
    val note: String,
    var status: String,
    val attachmentPath: String?,
    @ColumnInfo(name = "userId") val userId: Long
)
