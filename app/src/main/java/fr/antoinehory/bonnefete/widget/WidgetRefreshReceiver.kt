package fr.antoinehory.bonnefete.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver triggered daily at midnight to refresh the widget content.
 */
class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetRefreshReceiver", "Midnight alarm received! Refreshing widget...")
        
        // Refresh the widget content
        CoroutineScope(Dispatchers.Main).launch {
            SaintWidget().updateAll(context)
        }
    }
}
