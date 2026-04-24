package fr.antoinehory.bonnefete.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.antoinehory.bonnefete.data.local.dao.SaintDao
import fr.antoinehory.bonnefete.data.local.entity.SaintEntity

/**
 * Main application database.
 */
@Database(entities = [SaintEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saintDao(): SaintDao
}
