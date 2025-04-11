package com.example.project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =[Check::class, TimeManagerDao::class, User::class], version = 1,exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun checkDao(): CheckDao
    abstract fun userDao(): UserDao
    abstract fun timeManagerDao(): TimeManagerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}