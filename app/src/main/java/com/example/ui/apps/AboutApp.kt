package com.example.ui.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette

@Composable
fun AboutApp() {
    val palette = LocalDesktopPalette.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_uiforge_foreground),
                contentDescription = "UIForge Desktop Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column {
                Text("UIForge Desktop", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME} • Developer: UIForge",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.accent
                )
            }
        }

        HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.6f))

        Text(
            text = "UIForge Desktop is a modern, lightweight, offline-first desktop-style environment built natively for Android devices. It provides a multi-window workspace, customizable taskbar, Start menu, desktop shortcut grid, and productivity tools designed specifically for landscape touch interaction.",
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(12.dp)
        ) {
            Text("Technology Stack", style = MaterialTheme.typography.labelLarge, color = palette.accent)
            TechRow("Language", "Kotlin (100% Native Android)")
            TechRow("UI Toolkit", "Jetpack Compose + Material Design 3")
            TechRow("Settings Storage", "Jetpack DataStore Preferences")
            TechRow("Local Database", "Room SQLite + KSP")
            TechRow("Connectivity", "Offline-First (Zero Cloud Dependency)")
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.about_copyright),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
