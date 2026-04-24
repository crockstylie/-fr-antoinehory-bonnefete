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

data class MainUiState(
    val todaySaint: SaintEntity? = null,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val onlyContacts: Boolean = true
)

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

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationTime(hour, minute)
        }
    }

    fun updateOnlyContacts(onlyContacts: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateOnlyContacts(onlyContacts)
        }
    }

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
