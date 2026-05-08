package fr.antoinehory.bonnefete.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.antoinehory.bonnefete.data.local.entity.SaintEntity
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.data.repository.UserPreferencesRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import fr.antoinehory.bonnefete.worker.DailyUpdateWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * UI State for the Main screen.
 * @property todaySaint The saint entity for the current day.
 * @property notificationHour The hour at which the user wants to be notified.
 * @property notificationMinute The minute at which the user wants to be notified.
 * @property onlyContacts Whether to notify only if the saint name matches a contact.
 */
data class MainUiState(
    val todaySaint: SaintEntity? = null,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val onlyContacts: Boolean = true
)

/**
 * ViewModel for the main application logic and settings.
 * Manages user preferences, saint data, and work scheduling.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val saintRepository: SaintRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val workScheduler: WorkScheduler,
    private val workManager: WorkManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            saintRepository.populateDatabaseIfNeeded()
        }
        
        // Schedule work based on current preferences
        viewModelScope.launch {
            preferencesRepository.notificationTime.collect { time ->
                workScheduler.scheduleDailyUpdate(time.first, time.second)
            }
        }
    }

    private val today = Calendar.getInstance().let {
        Pair(it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
    }

    val uiState: StateFlow<MainUiState> = combine(
        preferencesRepository.notificationTime,
        preferencesRepository.onlyContacts,
        saintRepository.getSaintForDate(today.first, today.second)
    ) { time, onlyContacts, saint ->
        MainUiState(
            todaySaint = saint,
            notificationHour = time.first,
            notificationMinute = time.second,
            onlyContacts = onlyContacts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    val allSaints = saintRepository.getAllSaints()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Updates the notification time in preferences and reschedules the alarm.
     * @param hour The new hour.
     * @param minute The new minute.
     */
    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationTime(hour, minute)
        }
    }

    /**
     * Updates the preference for notifying only if matches are found in contacts.
     * @param onlyContacts The new preference value.
     */
    fun updateOnlyContacts(onlyContacts: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateOnlyContacts(onlyContacts)
        }
    }

    /**
     * Triggers a manual test of the notification system.
     * Enqueues a OneTimeWorkRequest for [DailyUpdateWorker] with test flag.
     */
    fun testNotification() {
        val inputData = Data.Builder()
            .putBoolean("is_test", true)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<DailyUpdateWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueue(workRequest)
    }
}
