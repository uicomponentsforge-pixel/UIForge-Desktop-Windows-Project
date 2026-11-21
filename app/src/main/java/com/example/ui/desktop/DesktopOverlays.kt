package com.example.ui.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.AccentPreset
import com.example.data.BuiltInAppId
import com.example.data.DesktopNotification
import com.example.data.DesktopPreferences
import com.example.data.WallpaperOption
import com.example.ui.theme.LocalDesktopPalette
import com.example.viewmodel.ContextMenuState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun QuickSettingsFlyout(
    visible: Boolean,
    preferences: DesktopPreferences,
    onToggleTheme: (Boolean) -> Unit,
    onSelectAccent: (AccentPreset) -> Unit,
    onSelectWallpaper: (WallpaperOption) -> Unit,
    onWindowTransparencyChange: (Float) -> Unit,
    onCompactTaskbarChange: (Boolean) -> Unit,
    onOpenFullSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 5 }),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.startMenuSurface.copy(alpha = 0.97f),
            tonalElevation = 14.dp,
            modifier = Modifier
                .width(330.dp)
                .shadow(20.dp, RoundedCornerShape(16.dp))
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .testTag("quick_settings_panel")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quick Settings", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onOpenFullSettings) {
                        Text("All Settings")
                    }
                }

                // Theme & Compact Taskbar toggles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .clickable { onToggleTheme(!preferences.isDarkTheme) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (preferences.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (preferences.isDarkTheme) "Dark" else "Light",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Switch(
                            checked = preferences.isDarkTheme,
                            onCheckedChange = onToggleTheme
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .clickable { onCompactTaskbarChange(!preferences.compactTaskbar) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("Compact Bar", style = MaterialTheme.typography.labelMedium)
                        Switch(
                            checked = preferences.compactTaskbar,
                            onCheckedChange = onCompactTaskbarChange
                        )
                    }
                }

                // Accent Swatches
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AccentPreset.entries.forEach { preset ->
                            val selected = preferences.accentPreset == preset
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(preset.primaryColor)
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectAccent(preset) }
                            )
                        }
                    }
                }

                // Wallpaper Quick Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Wallpaper",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WallpaperOption.entries.take(3).forEach { wp ->
                            val selected = preferences.wallpaper == wp
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) palette.accent.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) palette.accent else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectWallpaper(wp) }
                                    .padding(vertical = 7.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = wp.displayName.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Window Opacity Slider
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Window Opacity",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(preferences.windowTransparency * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Slider(
                        value = preferences.windowTransparency,
                        onValueChange = onWindowTransparencyChange,
                        valueRange = 0.72f..1.0f
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCenterFlyout(
    visible: Boolean,
    notifications: List<DesktopNotification>,
    onDismissNotification: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenApp: (BuiltInAppId) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 5 }),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.startMenuSurface.copy(alpha = 0.97f),
            tonalElevation = 14.dp,
            modifier = Modifier
                .width(330.dp)
                .height(310.dp)
                .shadow(20.dp, RoundedCornerShape(16.dp))
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .testTag("notifications_panel")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All")
                        }
                    }
                }
                HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All caught up",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(notifications, key = { it.id }) { item ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                    .clickable(enabled = item.appId != null) {
                                        item.appId?.let(onOpenApp)
                                    }
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = fmt.format(Date(item.timestampMillis)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDismissNotification(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Dismiss notification",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopContextMenuOverlay(
    state: ContextMenuState,
    workspaceWidthPx: Float,
    workspaceHeightPx: Float,
    taskbarPinnedApps: List<BuiltInAppId>,
    onDismiss: () -> Unit,
    onRefreshDesktop: () -> Unit,
    onOpenWallpaperSettings: () -> Unit,
    onOpenApplications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenApp: (BuiltInAppId) -> Unit,
    onTogglePinTaskbar: (BuiltInAppId) -> Unit,
    onRemoveShortcut: (String) -> Unit
) {
    if (state is ContextMenuState.None) return
    val palette = LocalDesktopPalette.current
    val density = LocalDensity.current
    val menuWidthDp = 224.dp
    val menuHeightApproxPx = with(density) { 240.dp.toPx() }
    val menuWidthPx = with(density) { menuWidthDp.toPx() }

    val (rawX, rawY) = when (state) {
        is ContextMenuState.DesktopEmpty -> state.xPx to state.yPx
        is ContextMenuState.ShortcutIcon -> state.xPx to state.yPx
        ContextMenuState.None -> 0f to 0f
    }

    val clampedX = rawX.coerceIn(12f, (workspaceWidthPx - menuWidthPx - 12f).coerceAtLeast(12f))
    val clampedY = rawY.coerceIn(12f, (workspaceHeightPx - menuHeightApproxPx - 12f).coerceAtLeast(12f))

    Box(
        modifier = Modifier
            .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = palette.startMenuSurface.copy(alpha = 0.98f),
            tonalElevation = 14.dp,
            modifier = Modifier
                .width(menuWidthDp)
                .shadow(18.dp, RoundedCornerShape(12.dp))
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(12.dp))
                .testTag("desktop_context_menu")
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                when (state) {
                    is ContextMenuState.DesktopEmpty -> {
                        ContextMenuRow(Icons.Rounded.Refresh, "Refresh Desktop") {
                            onRefreshDesktop()
                        }
                        ContextMenuRow(Icons.Rounded.Wallpaper, "Change Wallpaper") {
                            onDismiss()
                            onOpenWallpaperSettings()
                        }
                        ContextMenuRow(Icons.Rounded.Apps, "Manage Shortcuts") {
                            onDismiss()
                            onOpenApplications()
                        }
                        HorizontalDivider(
                            color = palette.borderSubtle.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        ContextMenuRow(Icons.Rounded.Settings, "Open Settings") {
                            onDismiss()
                            onOpenSettings()
                        }
                        ContextMenuRow(Icons.Rounded.Info, "About UIForge Desktop") {
                            onDismiss()
                            onOpenAbout()
                        }
                    }
                    is ContextMenuState.ShortcutIcon -> {
                        val sc = state.shortcut
                        val isTaskbarPinned = sc.appId in taskbarPinnedApps
                        ContextMenuRow(Icons.Rounded.OpenInNew, "Open ${sc.label}") {
                            onDismiss()
                            onOpenApp(sc.appId)
                        }
                        ContextMenuRow(
                            Icons.Rounded.PushPin,
                            if (isTaskbarPinned) "Unpin from Taskbar" else "Pin to Taskbar"
                        ) {
                            onDismiss()
                            onTogglePinTaskbar(sc.appId)
                        }
                        HorizontalDivider(
                            color = palette.borderSubtle.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        ContextMenuRow(
                            icon = Icons.Rounded.DeleteOutline,
                            label = "Remove Shortcut",
                            tint = MaterialTheme.colorScheme.error
                        ) {
                            onDismiss()
                            onRemoveShortcut(sc.id)
                        }
                    }
                    ContextMenuState.None -> Unit
                }
            }
        }
    }
}

@Composable
private fun ContextMenuRow(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PowerMenuDialog(
    onDismiss: () -> Unit,
    onShowDesktop: () -> Unit,
    onRefreshDesktop: () -> Unit,
    onExitApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Desktop Session Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose an action for your UIForge Desktop session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable {
                            onDismiss()
                            onShowDesktop()
                        }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Rounded.DesktopWindows, contentDescription = null)
                    Text("Minimize All Windows (Show Desktop)")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .clickable {
                            onDismiss()
                            onRefreshDesktop()
                        }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Text("Refresh Workspace")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onExitApp,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Exit UIForge")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
