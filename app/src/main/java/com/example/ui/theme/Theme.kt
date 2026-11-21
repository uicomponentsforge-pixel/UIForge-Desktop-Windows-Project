package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.AccentPreset

data class DesktopPalette(
    val isDark: Boolean,
    val accent: Color,
    val accentSoft: Color,
    val windowSurface: Color,
    val windowTitleBarActive: Color,
    val windowTitleBarInactive: Color,
    val taskbarSurface: Color,
    val startMenuSurface: Color,
    val borderSubtle: Color,
    val iconSelectionBg: Color
)

val LocalDesktopPalette = staticCompositionLocalOf {
    DesktopPalette(
        isDark = true,
        accent = Color(0xFF06B6D4),
        accentSoft = Color(0x3306B6D4),
        windowSurface = DarkSurfaceBase,
        windowTitleBarActive = DarkSurfaceElevated,
        windowTitleBarInactive = DarkSurfaceBase,
        taskbarSurface = Color(0xE00C111B),
        startMenuSurface = Color(0xF0111622),
        borderSubtle = DarkOutlineSubtle,
        iconSelectionBg = Color(0x3806B6D4)
    )
}

private val DesktopShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp)
)

@Composable
fun UIForgeDesktopTheme(
    darkTheme: Boolean = true,
    accentPreset: AccentPreset = AccentPreset.ELECTRIC_CYAN,
    fontScaleMultiplier: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val accentColor = accentPreset.primaryColor
    val secondaryColor = accentPreset.secondaryColor

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color(0xFF04131C),
            primaryContainer = accentColor.copy(alpha = 0.22f),
            onPrimaryContainer = DarkTextPrimary,
            secondary = secondaryColor,
            onSecondary = Color(0xFF04131C),
            secondaryContainer = DarkSurfaceBright,
            onSecondaryContainer = DarkTextPrimary,
            tertiary = accentPreset.tertiaryColor,
            background = DarkDesktopBg,
            onBackground = DarkTextPrimary,
            surface = DarkSurfaceBase,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceElevated,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkOutlineSubtle,
            outlineVariant = DarkOutlineSubtle.copy(alpha = 0.55f),
            error = WindowCloseRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.16f),
            onPrimaryContainer = LightTextPrimary,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = LightSurfaceBright,
            onSecondaryContainer = LightTextPrimary,
            tertiary = accentPreset.tertiaryColor,
            background = LightDesktopBg,
            onBackground = LightTextPrimary,
            surface = LightSurfaceBase,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = LightTextSecondary,
            outline = LightOutlineSubtle,
            outlineVariant = LightOutlineSubtle.copy(alpha = 0.6f),
            error = WindowCloseRed,
            onError = Color.White
        )
    }

    val desktopPalette = if (darkTheme) {
        DesktopPalette(
            isDark = true,
            accent = accentColor,
            accentSoft = accentColor.copy(alpha = 0.20f),
            windowSurface = DarkSurfaceBase,
            windowTitleBarActive = DarkSurfaceElevated,
            windowTitleBarInactive = Color(0xFF0E131C),
            taskbarSurface = Color(0xFF0B1019),
            startMenuSurface = Color(0xFF121824),
            borderSubtle = DarkOutlineSubtle,
            iconSelectionBg = accentColor.copy(alpha = 0.24f)
        )
    } else {
        DesktopPalette(
            isDark = false,
            accent = accentColor,
            accentSoft = accentColor.copy(alpha = 0.15f),
            windowSurface = LightSurfaceBase,
            windowTitleBarActive = LightSurfaceElevated,
            windowTitleBarInactive = Color(0xFFE9EFF6),
            taskbarSurface = Color(0xFFF1F5F9),
            startMenuSurface = Color(0xFFF8FAFC),
            borderSubtle = LightOutlineSubtle,
            iconSelectionBg = accentColor.copy(alpha = 0.18f)
        )
    }

    CompositionLocalProvider(LocalDesktopPalette provides desktopPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildDesktopTypography(fontScaleMultiplier),
            shapes = DesktopShapes,
            content = content
        )
    }
}
