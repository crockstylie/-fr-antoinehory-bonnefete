package fr.antoinehory.bonnefete.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.antoinehory.bonnefete.data.local.dao.SaintDao
import fr.antoinehory.bonnefete.data.local.entity.SaintEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Saint data.
 */
@Singleton
class SaintRepository @Inject constructor(
    private val saintDao: SaintDao,
    @ApplicationContext private val context: Context
) {
    /**
     * Returns today's Saint as a Flow.
     */
    fun getSaintForDate(month: Int, day: Int): Flow<SaintEntity?> {
        return saintDao.getSaintForDate(month, day)
    }

    /**
     * Returns all saints ordered by date.
     */
    fun getAllSaints(): Flow<List<SaintEntity>> {
        return saintDao.getAllSaints()
    }

    /**
     * Populates the database with initial data from assets if empty.
     */
    suspend fun populateDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        if (saintDao.getCount() == 0) {
            val jsonString = context.assets.open("saints.json").bufferedReader().use { it.readText() }
            val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
            val saints = mutableListOf<SaintEntity>()

            val months = listOf(
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december"
            )

            months.forEachIndexed { monthIndex, monthKey ->
                jsonObject[monthKey]?.jsonArray?.forEachIndexed { dayIndex, element ->
                    val dayData = element.jsonArray
                    saints.add(
                        SaintEntity(
                            month = monthIndex + 1,
                            day = dayIndex + 1,
                            name = dayData[0].jsonPrimitive.content,
                            title = dayData[1].jsonPrimitive.content
                        )
                    )
                }
            }
            saintDao.insertSaints(saints)
        }
    }
}
