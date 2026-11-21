package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

fun buildDesktopTypography(scale: Float = 1.0f): Typography {
    val s = scale.coerceIn(0.85f, 1.25f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * s).sp,
            lineHeight = (42 * s).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * s).sp,
            lineHeight = (34 * s).sp,
            letterSpacing = (-0.25).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * s).sp,
            lineHeight = (26 * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (22 * s).sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        titleSmall = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * s).sp,
            lineHeight = (18 * s).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13 * s).sp,
            lineHeight = (18 * s).sp
        ),
        bodySmall = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (11.5f * s).sp,
            lineHeight = (16 * s).sp
        ),
        labelLarge = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13 * s).sp,
            lineHeight = (18 * s).sp
        ),
        labelMedium = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * s).sp,
            lineHeight = (15 * s).sp
        ),
        labelSmall = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (10.5f * s).sp,
            lineHeight = (14 * s).sp
        )
    )
}
