package fr.antoinehory.bonnefete.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.bonnefete.R
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.ui.theme.MedievalGlanceColorScheme
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.max

class SaintWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SaintWidgetEntryPoint {
        fun saintRepository(): SaintRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SaintWidgetEntryPoint::class.java
        )
        val repository = entryPoint.saintRepository()

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val todayDateKey = "$month-$day"

        val saint = try {
            repository.populateDatabaseIfNeeded()
            repository.getSaintForDate(month, day).first()
        } catch (e: Exception) {
            null
        }

        val fallbackSaintName = saint?.name ?: context.getString(R.string.unknown)

        // Decode shared background bitmap once outside provideContent (SDK 37 RemoteViews memory cap best practice)
        val sharedBackgroundBitmap = decodeDownsampledResourceBitmap(
            context = context,
            resId = R.drawable.widget_background,
            distinctImageCount = 3,
            maxWidthDp = 320,
            maxHeightDp = 200,
            config = Bitmap.Config.RGB_565
        )

        provideContent {
            val prefs = currentState<Preferences>()
            // Reading KEY_REVISION and KEY_DATE_KEY inside provideContent ensures Glance
            // recomposes the widget whenever pushWidgetUpdate mutates state at midnight.
            @Suppress("UNUSED_VARIABLE")
            val revision = prefs[KEY_REVISION] ?: 0
            val stateDateKey = prefs[KEY_DATE_KEY]
            val stateSaintName = prefs[KEY_SAINT_NAME]

            val activeSaintName = if (stateDateKey == todayDateKey && !stateSaintName.isNullOrBlank()) {
                stateSaintName
            } else {
                fallbackSaintName
            }

            GlanceTheme(colors = MedievalGlanceColorScheme) {
                WidgetContent(
                    name = activeSaintName,
                    backgroundBitmap = sharedBackgroundBitmap
                )
            }
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        val fallback = RemoteViews(context.packageName, R.layout.glance_default_layout)
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
    }

    @Composable
    private fun WidgetContent(name: String, backgroundBitmap: Bitmap?) {
        val context = LocalContext.current
        val size = LocalSize.current

        // Use the minimum ratio of both dimensions to ensure text fits
        val widthFactor = size.width.value / 110f
        val heightFactor = size.height.value / 70f
        val responsiveFactor = minOf(widthFactor, heightFactor).coerceIn(1f, 4f)

        // Generate Bitmaps for the text to use the custom medieval font
        val titleBitmap = createTextBitmap(
            context = context,
            text = context.getString(R.string.bonne_fete),
            fontSizeDp = (14 * responsiveFactor).toInt()
        )
        val nameBitmap = createTextBitmap(
            context = context,
            text = name.trim(),
            fontSizeDp = (18 * responsiveFactor).toInt()
        )

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (backgroundBitmap != null) {
                Image(
                    provider = ImageProvider(backgroundBitmap),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    provider = ImageProvider(R.drawable.widget_background),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (titleBitmap != null) {
                    Image(
                        provider = ImageProvider(titleBitmap),
                        contentDescription = context.getString(R.string.bonne_fete),
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                if (nameBitmap != null) {
                    Image(
                        provider = ImageProvider(nameBitmap),
                        contentDescription = name,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }

    private fun createTextBitmap(context: Context, text: String, fontSizeDp: Int): Bitmap? {
        val typeface = ResourcesCompat.getFont(context, R.font.medieval_font) ?: Typeface.DEFAULT
        val density = context.resources.displayMetrics.density
        val fontSizePx = fontSizeDp * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.getColor(R.color.antique_gold)
            this.typeface = typeface
            textSize = fontSizePx
            textAlign = Paint.Align.CENTER
        }

        val baseline = -paint.ascent()
        val width = (paint.measureText(text) + 0.5f).toInt()
        val height = (baseline + paint.descent() + 0.5f).toInt()

        if (width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, width / 2f, baseline, paint)
        return bitmap
    }

    companion object {
        val KEY_SAINT_NAME = stringPreferencesKey("widget_saint_name")
        val KEY_DATE_KEY = stringPreferencesKey("widget_date_key")
        val KEY_REVISION = intPreferencesKey("widget_revision")

        /**
         * Pushes today's saint into Glance's PreferencesGlanceStateDefinition for every active
         * widget instance and immediately triggers a recomposition.
         * This guarantees the widget updates at midnight without needing a manual resize.
         */
        suspend fun pushWidgetUpdate(context: Context, saintRepository: SaintRepository) {
            val appContext = context.applicationContext
            saintRepository.populateDatabaseIfNeeded()

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val todayDateKey = "$month-$day"

            val saint = saintRepository.getSaintForDate(month, day).first()
            val saintName = saint?.name ?: appContext.getString(R.string.unknown)

            val widget = SaintWidget()
            val glanceIds = GlanceAppWidgetManager(appContext).getGlanceIds(SaintWidget::class.java)
            for (glanceId in glanceIds) {
                updateAppWidgetState(appContext, glanceId) { prefs ->
                    prefs[KEY_DATE_KEY] = todayDateKey
                    prefs[KEY_SAINT_NAME] = saintName
                    prefs[KEY_REVISION] = (prefs[KEY_REVISION] ?: 0) + 1
                }
                widget.update(appContext, glanceId)
            }
        }

        private fun calculateWidgetMemoryCapBytes(context: Context): Long {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = context.getSystemService(WindowManager::class.java)?.currentWindowMetrics?.bounds
                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                    return 6L * bounds.width().toLong() * bounds.height().toLong()
                }
            }
            val dm = context.resources.displayMetrics
            return 6L * dm.widthPixels.toLong() * dm.heightPixels.toLong()
        }

        private fun decodeDownsampledResourceBitmap(
            context: Context,
            resId: Int,
            distinctImageCount: Int = 1,
            maxWidthDp: Int = 320,
            maxHeightDp: Int = 200,
            config: Bitmap.Config = Bitmap.Config.RGB_565
        ): Bitmap? {
            val density = context.resources.displayMetrics.density
            val targetWidthPx = max(1, (maxWidthDp * density).toInt())
            val targetHeightPx = max(1, (maxHeightDp * density).toInt())
            val requestedBytesPerPixel = when (config) {
                Bitmap.Config.ALPHA_8 -> 1L
                Bitmap.Config.RGB_565 -> 2L
                else -> 4L
            }
            val perImageCapBytes = calculateWidgetMemoryCapBytes(context) / max(1, distinctImageCount)
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                inPreferredConfig = config
            }
            BitmapFactory.decodeResource(context.resources, resId, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val bytesPerPixel = if (bounds.outConfig != null && bounds.outConfig != config) {
                4L
            } else {
                requestedBytesPerPixel
            }
            var sampleSize = 1
            while (
                (bounds.outWidth / (sampleSize * 2) >= targetWidthPx &&
                    bounds.outHeight / (sampleSize * 2) >= targetHeightPx) ||
                (bounds.outWidth / sampleSize > targetWidthPx * 2) ||
                (bounds.outHeight / sampleSize > targetHeightPx * 2)
            ) {
                sampleSize *= 2
            }
            while ((max(1, bounds.outWidth / sampleSize).toLong() * max(1, bounds.outHeight / sampleSize).toLong() * bytesPerPixel) > perImageCapBytes) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = config
            }
            return BitmapFactory.decodeResource(context.resources, resId, opts)
        }
    }
}
