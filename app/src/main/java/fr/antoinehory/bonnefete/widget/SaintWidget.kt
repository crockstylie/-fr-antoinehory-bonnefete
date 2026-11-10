package fr.antoinehory.bonnefete.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
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
import kotlin.math.roundToInt

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
        val size: DpSize = LocalSize.current

        val widthDp = size.width.value.coerceAtLeast(110f)
        val heightDp = size.height.value.coerceAtLeast(56f)

        // Larger, highly legible medieval typography fitted to the widget bounds
        val titleBitmap = createFittedTextBitmap(
            context = context,
            text = context.getString(R.string.bonne_fete),
            maxWidthDp = widthDp - 12f,
            maxHeightDp = heightDp * 0.36f,
            preferredFontSizeDp = heightDp * 0.27f,
            textColor = context.getColor(R.color.antique_gold)
        )
        val nameBitmap = createFittedTextBitmap(
            context = context,
            text = name.trim(),
            maxWidthDp = widthDp - 12f,
            maxHeightDp = heightDp * 0.54f,
            preferredFontSizeDp = heightDp * 0.44f,
            textColor = Color.parseColor("#FFF4C2") // Brighter warm gold/parchment for maximum contrast on the saint name
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
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (titleBitmap != null) {
                    Image(
                        provider = ImageProvider(titleBitmap),
                        contentDescription = context.getString(R.string.bonne_fete),
                        contentScale = ContentScale.Fit,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                    )
                }
                Spacer(modifier = GlanceModifier.height(2.dp))
                if (nameBitmap != null) {
                    Image(
                        provider = ImageProvider(nameBitmap),
                        contentDescription = name,
                        contentScale = ContentScale.Fit,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                    )
                }
            }
        }
    }

    /**
     * Renders text with the custom medieval font at a large, legible size, automatically shrinking
     * the font size if the Saint name is very long so it always fits within [maxWidthDp] x [maxHeightDp],
     * with a dark drop shadow and subtle stroke for high contrast against the illuminated background.
     */
    private fun createFittedTextBitmap(
        context: Context,
        text: String,
        maxWidthDp: Float,
        maxHeightDp: Float,
        preferredFontSizeDp: Float,
        textColor: Int
    ): Bitmap? {
        if (text.isBlank()) return null
        val typeface = ResourcesCompat.getFont(context, R.font.medieval_font) ?: Typeface.DEFAULT
        val density = context.resources.displayMetrics.density

        val maxWidthPx = (maxWidthDp * density).coerceAtLeast(40f)
        val maxHeightPx = (maxHeightDp * density).coerceAtLeast(24f)
        var currentFontSizePx = (preferredFontSizeDp * density).coerceAtLeast(18f * density)
        val minFontSizePx = 12f * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // Auto-fit text size so long Saint names fit the width without making short names tiny
        while (currentFontSizePx > minFontSizePx) {
            paint.textSize = currentFontSizePx
            val measuredWidth = paint.measureText(text) + 12f * density
            val measuredHeight = (-paint.ascent() + paint.descent()) + 8f * density
            if (measuredWidth <= maxWidthPx && measuredHeight <= maxHeightPx) {
                break
            }
            currentFontSizePx -= 1.5f * density
        }
        paint.textSize = currentFontSizePx

        val shadowPadding = (4f * density).roundToInt().coerceAtLeast(4)
        val baseline = -paint.ascent() + shadowPadding
        val width = (paint.measureText(text) + shadowPadding * 2 + 0.5f).toInt().coerceAtLeast(1)
        val height = (baseline + paint.descent() + shadowPadding + 0.5f).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = width / 2f

        // 1. Dark outline + drop shadow for strong legibility over the textured blue background
        val outlinePaint = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = (2.2f * density).coerceAtLeast(2f)
            strokeJoin = Paint.Join.ROUND
            color = Color.argb(220, 8, 16, 30)
            setShadowLayer(4f * density, 0f, 2f * density, Color.argb(230, 0, 0, 0))
        }
        canvas.drawText(text, centerX, baseline, outlinePaint)

        // 2. Main golden fill
        paint.style = Paint.Style.FILL
        paint.color = textColor
        canvas.drawText(text, centerX, baseline, paint)

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
