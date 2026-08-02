package com.pandeyganesha.kaamsutra.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NetWorth::class, Task::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun netWorthDao(): NetWorthDao
    abstract fun taskDao(): TaskDao
}