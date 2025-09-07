package com.f1rq.lifemap.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.f1rq.lifemap.data.dao.EventDao
import com.f1rq.lifemap.data.entity.Event

@Database(
    entities = [Event::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}