package fr.antoinehory.bonnefete.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.data.repository.UserPreferencesRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import fr.antoinehory.bonnefete.widget.SaintWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver triggered when the device finishes booting or when the app package is updated.
 * Used to reschedule all alarms (notifications and widget refresh) and refresh the widget.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var workScheduler: WorkScheduler

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var saintRepository: SaintRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Action $action received, rescheduling alarms and updating widget...")
            val pendingResult = goAsync()
            val appContext = context.applicationContext

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    workScheduler.rescheduleAll(preferencesRepository)
                    SaintWidget.pushWidgetUpdate(appContext, saintRepository)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule or update widget on $action", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
