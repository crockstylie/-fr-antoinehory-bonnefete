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
import kotlinx.coroutines.SupervisorJob
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
        // Note: super.onUpdate() in GlanceAppWidgetReceiver already calls goAsync().
        // Calling goAsync() a second time on the same broadcast returns null!
        refreshWidgetAndScheduleMidnight(context, "onUpdate", useGoAsync = false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val isCustomDateOrTimeAction =
            action == Intent.ACTION_DATE_CHANGED ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED

        super.onReceive(context, intent)

        if (isCustomDateOrTimeAction) {
            // GlanceAppWidgetReceiver.onReceive does not handle these actions, so goAsync() hasn't been called yet.
            refreshWidgetAndScheduleMidnight(context, "onReceive($action)", useGoAsync = true)
        }
    }

    private fun refreshWidgetAndScheduleMidnight(
        context: Context,
        source: String,
        useGoAsync: Boolean
    ) {
        val pendingResult = if (useGoAsync) runCatching { goAsync() }.getOrNull() else null
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                Log.d("SaintWidgetReceiver", "Refreshing widget from $source")
                SaintWidget.pushWidgetUpdate(appContext, saintRepository)
                workScheduler.scheduleMidnightRefresh()
            } catch (e: Exception) {
                Log.e("SaintWidgetReceiver", "Failed to refresh widget from $source", e)
            } finally {
                pendingResult?.finish()
            }
        }
    }
}
