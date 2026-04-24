package fr.antoinehory.bonnefete.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.bonnefete.domain.WorkScheduler
import javax.inject.Inject
import android.content.BroadcastReceiver

/**
 * Receiver triggered by AlarmManager to start the notification worker.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Alarm received! Starting DailyUpdateWorker...")
        
        val workRequest = OneTimeWorkRequestBuilder<DailyUpdateWorker>()
            .addTag("daily_update_tag")
            .build()
            
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
