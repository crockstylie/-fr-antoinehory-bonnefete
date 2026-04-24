package fr.antoinehory.bonnefete.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.Image
import androidx.glance.ImageProvider
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import fr.antoinehory.bonnefete.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.bonnefete.data.repository.SaintRepository
import fr.antoinehory.bonnefete.ui.theme.MedievalGlanceColorScheme
import kotlinx.coroutines.flow.first
import java.util.Calendar

class SaintWidget : GlanceAppWidget() {

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
        
        val saint = try {
            repository.populateDatabaseIfNeeded()
            repository.getSaintForDate(month, day).first()
        } catch (e: Exception) {
            null
        }

        val saintName = saint?.name ?: "Inconnu"
        val saintTitle = saint?.title ?: ""

        provideContent {
            GlanceTheme(colors = MedievalGlanceColorScheme) {
                WidgetContent(saintTitle, saintName)
            }
        }
    }

    @Composable
    private fun WidgetContent(title: String, name: String) {
        val context = LocalContext.current
        val size = LocalSize.current
        
        // Use the minimum ratio of both dimensions to ensure text fits
        val widthFactor = size.width.value / 110f
        val heightFactor = size.height.value / 70f
        val responsiveFactor = minOf(widthFactor, heightFactor).coerceIn(1f, 4f)
        
        // Generate Bitmaps for the text to use the custom font
        val titleBitmap = createTextBitmap(context, "Bonne Fête", (14 * responsiveFactor).toInt())
        val nameBitmap = createTextBitmap(context, "$title $name".trim(), (18 * responsiveFactor).toInt())
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_background),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
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
                        contentDescription = "Bonne Fête",
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                if (nameBitmap != null) {
                    Image(
                        provider = ImageProvider(nameBitmap),
                        contentDescription = "$title $name",
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
}
