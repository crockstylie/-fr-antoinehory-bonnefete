package fr.antoinehory.bonnefete.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.domain.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SaintWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = SaintWidget()

    @Inject
    lateinit var saintRepository: SaintRepository

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        refreshWidgetAndScheduleMidnight(context, "onUpdate")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            refreshWidgetAndScheduleMidnight(context, "onReceive($action)")
        }
    }

    private fun refreshWidgetAndScheduleMidnight(context: Context, source: String) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SaintWidgetReceiver", "Refreshing widget from $source")
                SaintWidget.pushWidgetUpdate(appContext, saintRepository)
                workScheduler.scheduleMidnightRefresh()
            } catch (e: Exception) {
                Log.e("SaintWidgetReceiver", "Failed to refresh widget from $source", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
