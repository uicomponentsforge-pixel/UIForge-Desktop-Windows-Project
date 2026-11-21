package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AccentPreset
import com.example.data.BuiltInAppId
import com.example.data.DesktopPreferences
import com.example.data.WallpaperOption
import com.example.ui.theme.LocalDesktopPalette

private enum class SettingsTab(val label: String, val icon: ImageVector) {
    PERSONALIZATION("Personalization", Icons.Rounded.Palette),
    SYSTEM("System", Icons.Rounded.Memory),
    APPLICATIONS("Applications", Icons.Rounded.Apps),
    GENERAL("General", Icons.Rounded.Tune),
    RESET("Reset", Icons.Rounded.RestartAlt)
}

@Composable
fun SettingsApp(
    preferences: DesktopPreferences,
    documentCount: Int,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetAccent: (AccentPreset) -> Unit,
    onSetWallpaper: (WallpaperOption) -> Unit,
    onSetTaskbarTransparency: (Float) -> Unit,
    onSetWindowTransparency: (Float) -> Unit,
    onSetCompactTaskbar: (Boolean) -> Unit,
    onSetShowClockSeconds: (Boolean) -> Unit,
    onSetShowDesktopLabels: (Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetIconScale: (Float) -> Unit,
    onSetConfirmWindowClose: (Boolean) -> Unit,
    onSetStartupApp: (BuiltInAppId?) -> Unit,
    onOpenApplications: () -> Unit,
    onOpenAbout: () -> Unit,
    onResetAppearanceOnly: () -> Unit,
    onResetAll: (clearDocuments: Boolean) -> Unit
) {
    val palette = LocalDesktopPalette.current
    var activeTab by remember { mutableStateOf(SettingsTab.PERSONALIZATION) }
    var confirmFullReset by remember { mutableStateOf(false) }
    var confirmWipeDocs by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Sidebar Navigation
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .width(170.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(10.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            SettingsTab.entries.forEach { tab ->
                val selected = activeTab == tab
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (selected) palette.accent.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { activeTab = tab }
                        .testTag("settings_tab_${tab.name}")
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (selected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) palette.accent else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        VerticalDivider(color = palette.borderSubtle.copy(alpha = 0.5f))

        // Right Content Pane
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (activeTab) {
                SettingsTab.PERSONALIZATION -> {
                    AppearanceApp(
                        preferences = preferences,
                        onSetDarkTheme = onSetDarkTheme,
                        onSetAccent = onSetAccent,
                        onSetWallpaper = onSetWallpaper,
                        onSetTaskbarTransparency = onSetTaskbarTransparency,
                        onSetWindowTransparency = onSetWindowTransparency,
                        onSetCompactTaskbar = onSetCompactTaskbar,
                        onSetShowDesktopLabels = onSetShowDesktopLabels,
                        onSetFontScale = onSetFontScale,
                        onSetIconScale = onSetIconScale
                    )
                }
                SettingsTab.SYSTEM -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionCard(title = "Taskbar Clock & Display") {
                            SwitchSettingRow(
                                title = "Show Seconds in Taskbar Clock",
                                subtitle = "Display HH:mm:ss format in system tray",
                                checked = preferences.showClockSeconds,
                                onCheckedChange = onSetShowClockSeconds
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            SystemInfoApp()
                        }
                        OutlinedButton(onClick = onOpenAbout) {
                            Text("View About UIForge Desktop")
                        }
                    }
                }
                SettingsTab.APPLICATIONS -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionCard(title = "Shortcut & Launcher Management") {
                            Text(
                                text = "Desktop shortcuts: ${preferences.desktopShortcuts.size} • Taskbar pins: ${preferences.taskbarPinnedApps.size} • Start pins: ${preferences.startMenuPinnedApps.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = onOpenApplications) {
                                Text("Open Applications & Shortcut Manager")
                            }
                        }

                        SectionCard(title = "Startup Behavior") {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Launch on Startup", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "Automatically open a window when UIForge starts",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box {
                                    OutlinedButton(onClick = { dropdownExpanded = true }) {
                                        Text(preferences.startupApp?.title ?: "None (Desktop)")
                                    }
                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("None (Desktop)") },
                                            onClick = {
                                                onSetStartupApp(null)
                                                dropdownExpanded = false
                                            }
                                        )
                                        BuiltInAppId.entries.forEach { app ->
                                            DropdownMenuItem(
                                                text = { Text(app.title) },
                                                onClick = {
                                                    onSetStartupApp(app)
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                SettingsTab.GENERAL -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionCard(title = "Window Management Behavior") {
                            SwitchSettingRow(
                                title = "Confirm Before Closing Windows",
                                subtitle = "Show a confirmation dialog when closing any app window",
                                checked = preferences.confirmWindowClose,
                                onCheckedChange = onSetConfirmWindowClose
                            )
                        }
                        SectionCard(title = "Offline & Local Storage") {
                            Text(
                                text = "UIForge Desktop stores all preferences in Jetpack DataStore and local documents/calendar notes in an on-device Room SQLite database. Currently storing $documentCount local text document(s).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                SettingsTab.RESET -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionCard(title = "Restore Appearance") {
                            Text(
                                text = "Reset theme, wallpaper, accent color, transparency, and scaling to factory defaults while keeping your shortcuts and saved documents.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(onClick = onResetAppearanceOnly) {
                                Text("Restore Default Appearance")
                            }
                        }

                        SectionCard(title = "Reset Workspace & Data") {
                            Text(
                                text = "Restore all desktop preferences and shortcut layouts, or wipe local documents and calendar notes as well.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        confirmWipeDocs = false
                                        confirmFullReset = true
                                    }
                                ) {
                                    Text("Reset All Settings")
                                }
                                Button(
                                    onClick = {
                                        confirmWipeDocs = true
                                        confirmFullReset = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Factory Reset + Clear Data")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmFullReset) {
        AlertDialog(
            onDismissRequest = { confirmFullReset = false },
            title = { Text(if (confirmWipeDocs) "Factory Reset & Clear Data?" else "Reset All Settings?") },
            text = {
                Text(
                    if (confirmWipeDocs) {
                        "This will restore all UIForge Desktop settings to defaults and delete your saved local documents and calendar notes."
                    } else {
                        "This will restore all desktop settings, theme, and shortcut layouts to defaults. Your saved documents will be preserved."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmFullReset = false
                        onResetAll(confirmWipeDocs)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmFullReset = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
