package com.example.ui.apps

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import java.util.Locale

private data class SystemSnapshot(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val abis: String,
    val cpuCores: Int,
    val totalRamBytes: Long,
    val availRamBytes: Long,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val batteryPercent: Int?,
    val isCharging: Boolean,
    val appVersion: String
)

@Composable
fun SystemInfoApp() {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val palette = LocalDesktopPalette.current
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val snapshot = remember(refreshTrigger) { readSystemSnapshot(context) }

    val usedRam = (snapshot.totalRamBytes - snapshot.availRamBytes).coerceAtLeast(0L)
    val ramRatio = if (snapshot.totalRamBytes > 0) (usedRam.toFloat() / snapshot.totalRamBytes).coerceIn(0f, 1f) else 0f

    val usedStorage = (snapshot.totalStorageBytes - snapshot.freeStorageBytes).coerceAtLeast(0L)
    val storageRatio = if (snapshot.totalStorageBytes > 0) (usedStorage.toFloat() / snapshot.totalStorageBytes).coerceIn(0f, 1f) else 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("System Information", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${snapshot.manufacturer} ${snapshot.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.accent
                )
            }
            OutlinedButton(onClick = { refreshTrigger += 1 }) {
                Text("Refresh")
            }
        }

        // Memory & Storage Progress Cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(12.dp)
            ) {
                Text("System Memory (RAM)", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = { ramRatio },
                    color = palette.accent,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${formatGiB(snapshot.availRamBytes)} free of ${formatGiB(snapshot.totalRamBytes)}",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(12.dp)
            ) {
                Text("Internal Storage", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = { storageRatio },
                    color = palette.accent,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${formatGiB(snapshot.freeStorageBytes)} free of ${formatGiB(snapshot.totalStorageBytes)}",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Key-Value System Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(12.dp)
        ) {
            SysInfoLine("UIForge Desktop Version", snapshot.appVersion)
            SysInfoLine("Android OS", "Android ${snapshot.androidVersion} (API ${snapshot.sdkInt})")
            SysInfoLine("Manufacturer / Model", "${snapshot.manufacturer} ${snapshot.model}")
            SysInfoLine("CPU Cores / ABIs", "${snapshot.cpuCores} cores • ${snapshot.abis}")
            SysInfoLine("Viewport Size", "${config.screenWidthDp}dp × ${config.screenHeightDp}dp (${config.densityDpi} dpi)")
            SysInfoLine(
                "Battery",
                snapshot.batteryPercent?.let {
                    "$it% (${if (snapshot.isCharging) "Charging" else "On Battery"})"
                } ?: "Unavailable"
            )
        }
    }
}

@Composable
private fun SysInfoLine(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
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

private fun readSystemSnapshot(context: Context): SystemSnapshot {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am?.getMemoryInfo(memInfo)

    val statFs = StatFs(Environment.getDataDirectory().path)
    val totalStorage = statFs.totalBytes
    val freeStorage = statFs.availableBytes

    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else null
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    return SystemSnapshot(
        manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
        model = Build.MODEL,
        androidVersion = Build.VERSION.RELEASE,
        sdkInt = Build.VERSION.SDK_INT,
        abis = Build.SUPPORTED_ABIS.joinToString(", "),
        cpuCores = Runtime.getRuntime().availableProcessors(),
        totalRamBytes = memInfo.totalMem,
        availRamBytes = memInfo.availMem,
        totalStorageBytes = totalStorage,
        freeStorageBytes = freeStorage,
        batteryPercent = pct,
        isCharging = charging,
        appVersion = BuildConfig.VERSION_NAME
    )
}

private fun formatGiB(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format(Locale.US, "%.2f GB", gb)
}
