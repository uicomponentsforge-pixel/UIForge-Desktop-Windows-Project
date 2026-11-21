package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockApp(onTimerComplete: (String) -> Unit) {
    val palette = LocalDesktopPalette.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Clock") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Stopwatch") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Timer") })
        }

        when (selectedTab) {
            0 -> DigitalAndAnalogClockTab()
            1 -> StopwatchTab()
            2 -> TimerTab(onTimerComplete = onTimerComplete)
        }
    }
}

@Composable
private fun DigitalAndAnalogClockTab() {
    val palette = LocalDesktopPalette.current
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val cal = remember(nowMillis) { Calendar.getInstance().apply { timeInMillis = nowMillis } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Custom Canvas Analog Clock
        val dialColor = MaterialTheme.colorScheme.surfaceVariant
        val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
        val handColor = MaterialTheme.colorScheme.onSurface
        val accentColor = palette.accent

        Canvas(modifier = Modifier.size(165.dp)) {
            val radius = size.minDimension / 2f - 8f
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(color = dialColor, radius = radius, center = Offset(cx, cy))
            drawCircle(color = accentColor.copy(alpha = 0.5f), radius = radius, center = Offset(cx, cy), style = Stroke(width = 3f))

            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30 - 90).toDouble())
                val inner = radius * 0.82f
                val outer = radius * 0.93f
                drawLine(
                    color = tickColor,
                    start = Offset((cx + inner * cos(angle)).toFloat(), (cy + inner * sin(angle)).toFloat()),
                    end = Offset((cx + outer * cos(angle)).toFloat(), (cy + outer * sin(angle)).toFloat()),
                    strokeWidth = if (i % 3 == 0) 4f else 2f,
                    cap = StrokeCap.Round
                )
            }

            val sec = cal.get(Calendar.SECOND)
            val min = cal.get(Calendar.MINUTE) + sec / 60f
            val hour = (cal.get(Calendar.HOUR) % 12) + min / 60f

            val hourAngle = Math.toRadians((hour * 30 - 90).toDouble())
            val minAngle = Math.toRadians((min * 6 - 90).toDouble())
            val secAngle = Math.toRadians((sec * 6 - 90).toDouble())

            drawLine(
                color = handColor,
                start = Offset(cx, cy),
                end = Offset((cx + radius * 0.5f * cos(hourAngle)).toFloat(), (cy + radius * 0.5f * sin(hourAngle)).toFloat()),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = handColor,
                start = Offset(cx, cy),
                end = Offset((cx + radius * 0.72f * cos(minAngle)).toFloat(), (cy + radius * 0.72f * sin(minAngle)).toFloat()),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(cx, cy),
                end = Offset((cx + radius * 0.80f * cos(secAngle)).toFloat(), (cy + radius * 0.80f * sin(secAngle)).toFloat()),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = accentColor, radius = 5f, center = Offset(cx, cy))
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = timeFmt.format(Date(nowMillis)),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = palette.accent
            )
            Text(
                text = dateFmt.format(Date(nowMillis)),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = TimeZone.getDefault().displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StopwatchTab() {
    val palette = LocalDesktopPalette.current
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    var lastTickMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            lastTickMillis = System.currentTimeMillis()
            while (isRunning) {
                delay(50L)
                val now = System.currentTimeMillis()
                elapsedMillis += (now - lastTickMillis)
                lastTickMillis = now
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = formatElapsed(elapsedMillis),
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = palette.accent
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Pause" else if (elapsedMillis > 0L) "Resume" else "Start")
            }
            OutlinedButton(
                onClick = { laps.add(0, elapsedMillis) },
                enabled = isRunning || elapsedMillis > 0L
            ) {
                Text("Lap")
            }
            OutlinedButton(
                onClick = {
                    isRunning = false
                    elapsedMillis = 0L
                    laps.clear()
                }
            ) {
                Text("Reset")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(laps) { idx, lapTime ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Lap ${laps.size - idx}", style = MaterialTheme.typography.labelLarge)
                    Text(formatElapsed(lapTime), fontFamily = JetBrainsMonoFamily)
                }
            }
        }
    }
}

@Composable
private fun TimerTab(onTimerComplete: (String) -> Unit) {
    val palette = LocalDesktopPalette.current
    var totalSeconds by remember { mutableIntStateOf(300) }
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                onTimerComplete("Countdown timer (${totalSeconds / 60}m ${totalSeconds % 60}s) finished!")
            }
        }
    }

    val presets = listOf(60 to "1 min", 300 to "5 min", 600 to "10 min", 1500 to "25 min")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (sec, label) ->
                FilterChip(
                    selected = totalSeconds == sec,
                    onClick = {
                        isRunning = false
                        totalSeconds = sec
                        remainingSeconds = sec
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accent.copy(alpha = 0.22f),
                        selectedLabelColor = palette.accent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            color = if (remainingSeconds == 0) MaterialTheme.colorScheme.error else palette.accent
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (remainingSeconds == 0) remainingSeconds = totalSeconds
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent)
            ) {
                Text(if (isRunning) "Pause" else "Start Timer")
            }
            OutlinedButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = totalSeconds
                }
            ) {
                Text("Reset")
            }
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSec = ms / 1000
    val mins = totalSec / 60
    val secs = totalSec % 60
    val centis = (ms % 1000) / 10
    return String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, centis)
}
