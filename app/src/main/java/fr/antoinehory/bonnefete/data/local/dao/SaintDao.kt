package fr.antoinehory.bonnefete.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.antoinehory.bonnefete.data.local.entity.SaintEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [SaintEntity].
 */
@Dao
interface SaintDao {
    @Query("SELECT * FROM saints WHERE month = :month AND day = :day LIMIT 1")
    fun getSaintForDate(month: Int, day: Int): Flow<SaintEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaints(saints: List<SaintEntity>)

    @Query("SELECT COUNT(*) FROM saints")
    suspend fun getCount(): Int
}
