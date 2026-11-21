package com.example.ui.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.AlarmClock
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppCategory
import com.example.data.BuiltInAppId
import com.example.data.DesktopPreferences
import com.example.ui.theme.LocalDesktopPalette

@Composable
fun ApplicationsLauncherApp(
    preferences: DesktopPreferences,
    onOpenApp: (BuiltInAppId) -> Unit,
    onTogglePinDesktop: (BuiltInAppId) -> Unit,
    onTogglePinTaskbar: (BuiltInAppId) -> Unit,
    onTogglePinStart: (BuiltInAppId) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalDesktopPalette.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }

    val filtered = remember(query, selectedCategory) {
        BuiltInAppId.entries.filter { app ->
            val catOk = selectedCategory == null || app.category == selectedCategory
            val q = query.trim().lowercase()
            val qOk = q.isEmpty() || app.title.lowercase().contains(q) || app.subtitle.lowercase().contains(q)
            catOk && qOk
        }
    }

    fun launchSystemIntent(intent: Intent, label: String) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            onShowMessage("No Android app available to handle $label")
        } catch (_: Exception) {
            onShowMessage("Unable to launch $label on this device")
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("Search built-in applications…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${BuiltInAppId.entries.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = palette.accent.copy(alpha = 0.22f),
                    selectedLabelColor = palette.accent
                )
            )
            AppCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accent.copy(alpha = 0.22f),
                        selectedLabelColor = palette.accent
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filtered, key = { it.name }) { appId ->
                val isDesktopPinned = preferences.desktopShortcuts.any { it.appId == appId }
                val isTaskbarPinned = appId in preferences.taskbarPinnedApps
                val isStartPinned = appId in preferences.startMenuPinnedApps

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onOpenApp(appId) }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(appId.badgeColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = appId.icon,
                                contentDescription = null,
                                tint = appId.badgeColor,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column {
                            Text(
                                text = appId.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = appId.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { onTogglePinDesktop(appId) }) {
                            Icon(
                                imageVector = Icons.Rounded.DesktopWindows,
                                contentDescription = if (isDesktopPinned) "Remove from Desktop" else "Pin to Desktop",
                                tint = if (isDesktopPinned) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { onTogglePinTaskbar(appId) }) {
                            Icon(
                                imageVector = Icons.Rounded.ViewSidebar,
                                contentDescription = if (isTaskbarPinned) "Unpin from Taskbar" else "Pin to Taskbar",
                                tint = if (isTaskbarPinned) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { onTogglePinStart(appId) }) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = if (isStartPinned) "Unpin from Start" else "Pin to Start",
                                tint = if (isStartPinned) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Button(onClick = { onOpenApp(appId) }) {
                            Text("Open", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "External Android System Intents",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    OutlinedButton(
                        onClick = {
                            launchSystemIntent(Intent(Settings.ACTION_SETTINGS), "Android Settings")
                        }
                    ) {
                        Icon(Icons.Rounded.SettingsApplications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Android Settings")
                    }
                    OutlinedButton(
                        onClick = {
                            launchSystemIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS), "Display Settings")
                        }
                    ) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Display Settings")
                    }
                    OutlinedButton(
                        onClick = {
                            launchSystemIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS), "System Clock")
                        }
                    ) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Device Alarms")
                    }
                }
            }
        }
    }
}
