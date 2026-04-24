package fr.antoinehory.bonnefete.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Saint for a specific date.
 * @property id Unique identifier.
 * @property month Month of the year (1-12).
 * @property day Day of the month (1-31).
 * @property name Name of the Saint.
 * @property title Title (e.g., Saint, Sainte, or empty).
 */
@Entity(tableName = "saints")
data class SaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val month: Int,
    val day: Int,
    val name: String,
    val title: String
)
