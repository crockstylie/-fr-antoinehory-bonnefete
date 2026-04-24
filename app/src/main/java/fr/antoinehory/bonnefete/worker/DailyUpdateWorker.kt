package fr.antoinehory.bonnefete.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.data.repository.UserPreferencesRepository
import fr.antoinehory.bonnefete.domain.ContactService
import fr.antoinehory.bonnefete.domain.NotificationService
import fr.antoinehory.bonnefete.widget.SaintWidget
import androidx.glance.appwidget.updateAll
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.bonnefete.domain.WorkScheduler
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class DailyUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val saintRepository: SaintRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val contactService: ContactService,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DailyUpdateWorker"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DailyUpdateWorkerEntryPoint {
        fun workScheduler(): WorkScheduler
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting DailyUpdateWorker execution (ID: ${id})")
        try {
            // 1. Initialize data
            saintRepository.populateDatabaseIfNeeded()

            // 2. Get today's date
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            Log.d(TAG, "Fetching saint for date: $day/$month")

            // 3. Get Saint
            val saint = saintRepository.getSaintForDate(month, day).first()
            if (saint == null) {
                Log.w(TAG, "No saint found for today")
                return Result.success()
            }
            Log.d(TAG, "Found saint: ${saint.title} ${saint.name}")

            // 4. Check preferences
            val isTest = inputData.getBoolean("is_test", false)
            val onlyContacts = preferencesRepository.onlyContacts.first()
            val matches = contactService.getMatchingContacts(saint.name)
            Log.d(TAG, "OnlyContacts pref: $onlyContacts, Matches found: ${matches.size}, isTest: $isTest")

            // 5. Notify if needed (Force if it's a test)
            if (isTest || !onlyContacts || matches.isNotEmpty()) {
                Log.d(TAG, "Sending notification...")
                notificationService.sendSaintNotification(saint.title, saint.name, matches)
            } else {
                Log.d(TAG, "Notification skipped (onlyContacts=true and no matches)")
            }

            // 6. Update Widget
            Log.d(TAG, "Updating widget")
            SaintWidget().updateAll(applicationContext)

            // 7. Schedule next day
            val time = preferencesRepository.notificationTime.first()
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DailyUpdateWorkerEntryPoint::class.java
            )
            Log.d(TAG, "Rescheduling for tomorrow at ${time.first}:${time.second}")
            entryPoint.workScheduler().scheduleDailyUpdate(time.first, time.second)
            
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyUpdateWorker", e)
            return Result.retry()
        }
    }
}
