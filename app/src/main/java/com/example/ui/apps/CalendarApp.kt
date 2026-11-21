package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalDesktopPalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarApp(
    calendarNotes: Map<String, String>,
    onSaveNote: (dateKey: String, note: String) -> Unit
) {
    val palette = LocalDesktopPalette.current
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var displayYear by remember { mutableIntStateOf(todayYear) }
    var displayMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedDateKey by remember {
        mutableStateOf(formatDateKey(todayYear, todayMonth, todayDay))
    }

    var noteDraft by remember(selectedDateKey, calendarNotes) {
        mutableStateOf(calendarNotes[selectedDateKey].orEmpty())
    }

    val monthCal = remember(displayYear, displayMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayYear)
            set(Calendar.MONTH, displayMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val monthHeader = remember(displayYear, displayMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time)
    }
    val firstDayOfWeekOffset = monthCal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Left: Monthly Calendar Grid
        Column(
            modifier = Modifier
                .weight(1.35f)
                .fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = monthHeader,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            displayYear = todayYear
                            displayMonth = todayMonth
                            selectedDateKey = formatDateKey(todayYear, todayMonth, todayDay)
                        }
                    ) {
                        Text("Today", style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = {
                        if (displayMonth == 0) {
                            displayMonth = 11
                            displayYear -= 1
                        } else {
                            displayMonth -= 1
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    IconButton(onClick = {
                        if (displayMonth == 11) {
                            displayMonth = 0
                            displayYear += 1
                        } else {
                            displayMonth += 1
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val totalCells = firstDayOfWeekOffset + daysInMonth
            val rowCount = ((totalCells + 6) / 7).coerceAtLeast(5)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                for (row in 0 until rowCount) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeekOffset + 1
                            if (dayNumber in 1..daysInMonth) {
                                val key = formatDateKey(displayYear, displayMonth, dayNumber)
                                val isToday = displayYear == todayYear && displayMonth == todayMonth && dayNumber == todayDay
                                val isSelected = selectedDateKey == key
                                val hasNote = !calendarNotes[key].isNullOrBlank()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> palette.accent.copy(alpha = 0.25f)
                                                isToday -> palette.accent.copy(alpha = 0.10f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            }
                                        )
                                        .then(
                                            if (isToday || isSelected) {
                                                Modifier.border(
                                                    width = 1.dp,
                                                    color = if (isSelected) palette.accent else palette.accent.copy(alpha = 0.55f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clickable { selectedDateKey = key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) palette.accent else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasNote) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Right: Selected Day Details & Persistent Note
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(12.dp)
        ) {
            Text(
                text = "Selected Date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedDateKey,
                style = MaterialTheme.typography.titleLarge,
                color = palette.accent
            )
            OutlinedTextField(
                value = noteDraft,
                onValueChange = { noteDraft = it },
                label = { Text("Day Note / Reminder") },
                placeholder = { Text("Add a local note for this date…") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onSaveNote(selectedDateKey, noteDraft) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Note")
                }
                if (!calendarNotes[selectedDateKey].isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            noteDraft = ""
                            onSaveNote(selectedDateKey, "")
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

private fun formatDateKey(year: Int, monthZeroBased: Int, day: Int): String {
    return String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day)
}
