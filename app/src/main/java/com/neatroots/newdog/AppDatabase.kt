package com.neatroots.newdog

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neatroots.newdog.Dao
import com.neatroots.newdog.Petdata


@Database(entities = [Petdata::class], exportSchema = false, version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun getDao(): Dao
}