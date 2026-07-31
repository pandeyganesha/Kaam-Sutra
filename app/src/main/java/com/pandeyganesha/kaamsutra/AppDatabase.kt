package com.pandeyganesha.kaamsutra

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NetWorth::class, Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun netWorthDao(): NetWorthDao
    abstract fun taskDao(): TaskDao
}