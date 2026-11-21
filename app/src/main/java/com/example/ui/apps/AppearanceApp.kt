package com.example.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccentPreset
import com.example.data.DesktopPreferences
import com.example.data.WallpaperOption
import com.example.ui.theme.LocalDesktopPalette
import kotlin.math.roundToInt

@Composable
fun AppearanceApp(
    preferences: DesktopPreferences,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetAccent: (AccentPreset) -> Unit,
    onSetWallpaper: (WallpaperOption) -> Unit,
    onSetTaskbarTransparency: (Float) -> Unit,
    onSetWindowTransparency: (Float) -> Unit,
    onSetCompactTaskbar: (Boolean) -> Unit,
    onSetShowDesktopLabels: (Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetIconScale: (Float) -> Unit
) {
    val palette = LocalDesktopPalette.current

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Theme Mode
        SectionCard(title = "Theme Mode") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ThemeOptionTile(
                    title = "Dark Workspace",
                    subtitle = "Default obsidian desktop",
                    icon = Icons.Rounded.DarkMode,
                    selected = preferences.isDarkTheme,
                    onClick = { onSetDarkTheme(true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_dark_option")
                )
                ThemeOptionTile(
                    title = "Light Workspace",
                    subtitle = "Crisp daylight desktop",
                    icon = Icons.Rounded.LightMode,
                    selected = !preferences.isDarkTheme,
                    onClick = { onSetDarkTheme(false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_light_option")
                )
            }
        }

        // Accent Color
        SectionCard(title = "Accent Color") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AccentPreset.entries.forEach { preset ->
                    val selected = preferences.accentPreset == preset
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSetAccent(preset) }
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(preset.primaryColor)
                                .border(
                                    width = if (selected) 2.5.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.displayName.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Desktop Wallpaper
        SectionCard(title = "Desktop Wallpaper") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WallpaperOption.entries.forEach { wp ->
                    val selected = preferences.wallpaper == wp
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) palette.accent else palette.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSetWallpaper(wp) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(7.dp))
                        ) {
                            when (wp) {
                                WallpaperOption.AURORA_MESH -> Image(
                                    painter = painterResource(id = R.drawable.img_wallpaper_aurora),
                                    contentDescription = wp.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                WallpaperOption.OBSIDIAN_BEAM -> Image(
                                    painter = painterResource(id = R.drawable.img_wallpaper_obsidian),
                                    contentDescription = wp.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                WallpaperOption.DAYLIGHT_GLASS -> Image(
                                    painter = painterResource(id = R.drawable.img_wallpaper_daylight),
                                    contentDescription = wp.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                WallpaperOption.NEON_BLUEPRINT -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0A1220), palette.accent.copy(alpha = 0.5f))
                                            )
                                        )
                                )
                                WallpaperOption.SOLAR_HORIZON -> Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(palette.accent.copy(alpha = 0.55f), Color(0xFF1F2A44))
                                            )
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wp.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Transparency, Taskbar & Typography Sliders
        SectionCard(title = "Taskbar, Windows & Scaling") {
            SliderSettingRow(
                label = "Taskbar Opacity",
                valueText = "${(preferences.taskbarTransparency * 100).roundToInt()}%",
                value = preferences.taskbarTransparency,
                range = 0.50f..1.0f,
                onValueChange = onSetTaskbarTransparency
            )
            SliderSettingRow(
                label = "Window Opacity",
                valueText = "${(preferences.windowTransparency * 100).roundToInt()}%",
                value = preferences.windowTransparency,
                range = 0.70f..1.0f,
                onValueChange = onSetWindowTransparency
            )
            SliderSettingRow(
                label = "Interface Font Scale",
                valueText = "${(preferences.fontScaleMultiplier * 100).roundToInt()}%",
                value = preferences.fontScaleMultiplier,
                range = 0.85f..1.20f,
                onValueChange = onSetFontScale
            )
            SliderSettingRow(
                label = "Desktop Icon Scale",
                valueText = "${(preferences.iconScaleMultiplier * 100).roundToInt()}%",
                value = preferences.iconScaleMultiplier,
                range = 0.80f..1.25f,
                onValueChange = onSetIconScale
            )
            SwitchSettingRow(
                title = "Compact Taskbar",
                subtitle = "Hide labels on unlaunched taskbar pins",
                checked = preferences.compactTaskbar,
                onCheckedChange = onSetCompactTaskbar
            )
            SwitchSettingRow(
                title = "Show Desktop Icon Labels",
                subtitle = "Display shortcut titles under desktop icons",
                checked = preferences.showDesktopIconLabels,
                onCheckedChange = onSetShowDesktopLabels
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun ThemeOptionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) palette.accent.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            )
            .border(
                width = 1.dp,
                color = if (selected) palette.accent else palette.borderSubtle,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant)
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SliderSettingRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
