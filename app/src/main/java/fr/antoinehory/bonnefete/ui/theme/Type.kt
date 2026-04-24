package fr.antoinehory.bonnefete.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fr.antoinehory.bonnefete.R

// Déclaration de votre police médiévale personnalisée
val MedievalFont = FontFamily(
    Font(R.font.medieval_font, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MedievalFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
