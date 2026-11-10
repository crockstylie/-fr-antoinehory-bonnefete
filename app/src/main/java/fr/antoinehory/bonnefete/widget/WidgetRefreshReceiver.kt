package fr.antoinehory.bonnefete.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver triggered daily at midnight to refresh the widget content
 * and schedule the next midnight alarm.
 */
@AndroidEntryPoint
class WidgetRefreshReceiver : BroadcastReceiver() {

    @Inject
    lateinit var saintRepository: SaintRepository

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetRefreshReceiver", "Midnight alarm received! Refreshing widget and rescheduling...")
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SaintWidget.pushWidgetUpdate(appContext, saintRepository)
                workScheduler.scheduleMidnightRefresh()
            } catch (e: Exception) {
                Log.e("WidgetRefreshReceiver", "Error refreshing widget at midnight", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
