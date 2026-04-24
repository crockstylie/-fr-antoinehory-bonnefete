package fr.antoinehory.bonnefete.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user preferences.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val ONLY_CONTACTS = booleanPreferencesKey("only_contacts")
    }

    val notificationTime: Flow<Pair<Int, Int>> = context.dataStore.data.map { preferences ->
        val hour = preferences[PreferencesKeys.NOTIFICATION_HOUR] ?: 9
        val minute = preferences[PreferencesKeys.NOTIFICATION_MINUTE] ?: 0
        Pair(hour, minute)
    }

    val onlyContacts: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONLY_CONTACTS] ?: true
    }

    suspend fun updateNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_HOUR] = hour
            preferences[PreferencesKeys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun updateOnlyContacts(onlyContacts: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONLY_CONTACTS] = onlyContacts
        }
    }
}
