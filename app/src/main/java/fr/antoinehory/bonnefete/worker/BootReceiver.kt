package fr.antoinehory.bonnefete.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.bonnefete.data.repository.UserPreferencesRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver triggered when the device finishes booting.
 * Used to reschedule all alarms (notifications and widget refresh).
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling all alarms...")
            
            // Use GlobalScope or a custom scope since the receiver process might be short-lived
            // For rescheduling, Dispatchers.IO is appropriate
            CoroutineScope(Dispatchers.IO).launch {
                workScheduler.rescheduleAll(preferencesRepository)
            }
        }
    }
}
