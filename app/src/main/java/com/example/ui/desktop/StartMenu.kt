package com.example.ui.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppCategory
import com.example.data.BuiltInAppId
import com.example.data.DesktopPreferences
import com.example.ui.theme.LocalDesktopPalette

private enum class StartFilter(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    PRODUCTIVITY("Productivity"),
    TOOLS("Tools"),
    SYSTEM("System")
}

@Composable
fun StartMenu(
    visible: Boolean,
    preferences: DesktopPreferences,
    onLaunchApp: (BuiltInAppId) -> Unit,
    onTogglePinStart: (BuiltInAppId) -> Unit,
    onPowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    var searchQuery by remember(visible) { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(StartFilter.ALL) }

    val filteredApps = remember(searchQuery, activeFilter, preferences.startMenuPinnedApps) {
        val base = when (activeFilter) {
            StartFilter.ALL -> BuiltInAppId.entries
            StartFilter.PINNED -> preferences.startMenuPinnedApps
            StartFilter.PRODUCTIVITY -> BuiltInAppId.entries.filter { it.category == AppCategory.PRODUCTIVITY }
            StartFilter.TOOLS -> BuiltInAppId.entries.filter { it.category == AppCategory.TOOLS }
            StartFilter.SYSTEM -> BuiltInAppId.entries.filter { it.category == AppCategory.SYSTEM }
        }
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) base
        else base.filter {
            it.title.lowercase().contains(query) || it.subtitle.lowercase().contains(query)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
            slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
            slideOutVertically(targetOffsetY = { it / 5 }),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.startMenuSurface.copy(alpha = 0.97f),
            tonalElevation = 14.dp,
            modifier = Modifier
                .widthIn(min = 380.dp, max = 560.dp)
                .fillMaxWidth(0.56f)
                .height(370.dp)
                .shadow(22.dp, RoundedCornerShape(16.dp))
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .testTag("start_menu_panel")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search apps, tools & settings…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = palette.accent
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(11.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.borderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_menu_search")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    StartFilter.entries.forEach { filter ->
                        val selected = activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { activeFilter = filter },
                            label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.accent.copy(alpha = 0.24f),
                                selectedLabelColor = palette.accent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Applications Grid
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching apps found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 112.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredApps, key = { it.name }) { appId ->
                            val isPinned = appId in preferences.startMenuPinnedApps
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .clickable { onLaunchApp(appId) }
                                    .testTag("start_app_${appId.name}")
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(appId.badgeColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = appId.icon,
                                            contentDescription = appId.title,
                                            tint = appId.badgeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = appId.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .clickable { onTogglePinStart(appId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PushPin,
                                        contentDescription = if (isPinned) "Unpin from Start" else "Pin to Start",
                                        tint = if (isPinned) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Apps Strip
                if (preferences.recentApps.isNotEmpty() && searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Recent:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        preferences.recentApps.take(4).forEach { appId ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { onLaunchApp(appId) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = appId.icon,
                                    contentDescription = null,
                                    tint = appId.badgeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = appId.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))

                // Footer Bar (Workspace identity, Settings, Appearance, About, Power)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Computer,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "UIForge Desktop",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Local Offline Session",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { onLaunchApp(BuiltInAppId.APPEARANCE) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = "Appearance",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onLaunchApp(BuiltInAppId.SETTINGS) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onLaunchApp(BuiltInAppId.ABOUT) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "About UIForge",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onPowerClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("start_power_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PowerSettingsNew,
                                contentDescription = "Power & Session Options",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
