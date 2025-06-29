package com.example.mftool.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [IsinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IsinDb : RoomDatabase() {
    abstract fun isinDao(): IsinDao
}