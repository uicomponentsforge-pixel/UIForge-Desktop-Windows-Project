package com.example.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BuiltInAppId
import com.example.data.DesktopPreferences
import com.example.data.DesktopWindowState
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Taskbar(
    preferences: DesktopPreferences,
    windows: List<DesktopWindowState>,
    activeWindowId: String?,
    isStartMenuOpen: Boolean,
    isQuickSettingsOpen: Boolean,
    isNotificationsOpen: Boolean,
    notificationCount: Int,
    onStartClick: () -> Unit,
    onTaskbarAppClick: (BuiltInAppId) -> Unit,
    onQuickSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onShowDesktopClick: () -> Unit,
    onClockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    val taskbarHeight = if (preferences.compactTaskbar) 46.dp else 54.dp

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFormat = remember(preferences.showClockSeconds) {
        SimpleDateFormat(
            if (preferences.showClockSeconds) "HH:mm:ss" else "HH:mm",
            Locale.getDefault()
        )
    }
    val dateFormat = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    }
    val currentDate = remember(nowMillis) { Date(nowMillis) }

    // Combine pinned apps and currently open apps in stable order
    val taskbarItems = remember(preferences.taskbarPinnedApps, windows) {
        val openAppIds = windows.map { it.appId }
        (preferences.taskbarPinnedApps + openAppIds).distinct()
    }

    Surface(
        color = palette.taskbarSurface.copy(alpha = preferences.taskbarTransparency),
        tonalElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.7f), thickness = 1.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(taskbarHeight)
                    .padding(horizontal = 8.dp)
            ) {
                // Start Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isStartMenuOpen) {
                                Brush.horizontalGradient(
                                    listOf(palette.accent, palette.accent.copy(alpha = 0.75f))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        palette.accent.copy(alpha = 0.22f),
                                        palette.accent.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = palette.accent.copy(alpha = if (isStartMenuOpen) 0.9f else 0.45f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onStartClick() }
                        .testTag("start_button")
                        .padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = "Start Menu",
                        tint = if (isStartMenuOpen) Color.White else palette.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isStartMenuOpen) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(palette.borderSubtle.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Pinned & Running Applications Strip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    taskbarItems.forEach { appId ->
                        val window = windows.firstOrNull { it.appId == appId }
                        val isOpen = window != null
                        val isActive = window != null && !window.isMinimized && window.instanceId == activeWindowId
                        val isMinimized = window != null && window.isMinimized

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    when {
                                        isActive -> palette.accent.copy(alpha = 0.22f)
                                        isOpen -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isActive) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = palette.accent.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(9.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onTaskbarAppClick(appId) }
                                .testTag("taskbar_app_${appId.name}")
                                .padding(horizontal = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = appId.icon,
                                    contentDescription = appId.title,
                                    tint = if (isOpen || isActive) appId.badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (!preferences.compactTaskbar || isOpen) {
                                    Text(
                                        text = appId.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isActive) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (isOpen) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .width(if (isActive) 20.dp else 8.dp)
                                        .height(2.5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isMinimized) {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            } else {
                                                palette.accent
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // System Tray & Quick Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Settings pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isQuickSettingsOpen) palette.accent.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                            .clickable { onQuickSettingsClick() }
                            .testTag("quick_settings_button")
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WifiOff,
                            contentDescription = "Offline Ready Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Icon(
                            imageVector = if (preferences.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = "Theme",
                            tint = palette.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Quick Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Notification Center button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isNotificationsOpen) palette.accent.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                            .clickable { onNotificationsClick() }
                            .testTag("notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(17.dp)
                        )
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent)
                            )
                        }
                    }

                    // Clock & Date
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onClockClick() }
                            .testTag("taskbar_clock")
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = timeFormat.format(currentDate),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateFormat.format(currentDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    // Show Desktop edge button
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.borderSubtle.copy(alpha = 0.5f))
                            .clickable { onShowDesktopClick() }
                            .testTag("show_desktop_button")
                    )
                }
            }
        }
    }
}
