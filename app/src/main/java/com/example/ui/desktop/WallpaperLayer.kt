package com.example.ui.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.AccentPreset
import com.example.data.WallpaperOption

@Composable
fun WallpaperLayer(
    wallpaper: WallpaperOption,
    accentPreset: AccentPreset,
    isDarkTheme: Boolean,
    onDesktopTap: () -> Unit,
    onDesktopLongPress: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onDesktopTap() },
                    onLongPress = { offset -> onDesktopLongPress(offset) }
                )
            }
    ) {
        when (wallpaper) {
            WallpaperOption.AURORA_MESH -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_aurora),
                    contentDescription = "Aurora Mesh desktop wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperOption.OBSIDIAN_BEAM -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_obsidian),
                    contentDescription = "Obsidian Beam desktop wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperOption.DAYLIGHT_GLASS -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_daylight),
                    contentDescription = "Daylight Glass desktop wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperOption.NEON_BLUEPRINT -> {
                val baseColor = if (isDarkTheme) Color(0xFF070B12) else Color(0xFFEAF1F8)
                val gridColor = accentPreset.primaryColor.copy(alpha = if (isDarkTheme) 0.14f else 0.16f)
                val glowColor = accentPreset.primaryColor.copy(alpha = if (isDarkTheme) 0.28f else 0.22f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = baseColor)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent),
                            center = Offset(size.width * 0.72f, size.height * 0.28f),
                            radius = size.maxDimension * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentPreset.tertiaryColor.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.8f),
                            radius = size.maxDimension * 0.55f
                        )
                    )
                    val step = 48f
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }
                }
            }
            WallpaperOption.SOLAR_HORIZON -> {
                val top = if (isDarkTheme) Color(0xFF0B1120) else Color(0xFFF3F6FC)
                val mid = accentPreset.primaryColor.copy(alpha = if (isDarkTheme) 0.34f else 0.24f)
                val warm = accentPreset.tertiaryColor.copy(alpha = if (isDarkTheme) 0.30f else 0.22f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(top, if (isDarkTheme) Color(0xFF131D33) else Color(0xFFE2EBF6)),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mid, Color.Transparent),
                            center = Offset(size.width * 0.3f, size.height * 0.35f),
                            radius = size.maxDimension * 0.6f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(warm, Color.Transparent),
                            center = Offset(size.width * 0.82f, size.height * 0.72f),
                            radius = size.maxDimension * 0.55f
                        )
                    )
                }
            }
        }
    }
}
