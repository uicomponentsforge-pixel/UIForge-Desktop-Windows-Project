package com.example.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.DesktopShortcut
import com.example.data.WallpaperOption
import com.example.ui.theme.LocalDesktopPalette
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DesktopIconsGrid(
    shortcuts: List<DesktopShortcut>,
    selectedShortcutId: String?,
    showLabels: Boolean,
    iconScale: Float,
    wallpaper: WallpaperOption,
    onSelectShortcut: (String?) -> Unit,
    onOpenShortcut: (DesktopShortcut) -> Unit,
    onShortcutLongPress: (DesktopShortcut, Offset) -> Unit,
    onMoveShortcut: (String, Int, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalDesktopPalette.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        val cellWidthDp = (88f * iconScale).coerceIn(74f, 114f)
        val cellHeightDp = (84f * iconScale).coerceIn(72f, 108f)
        val cols = max(1, (maxWidth.value / cellWidthDp).toInt())
        val rows = max(1, (maxHeight.value / cellHeightDp).toInt())
        val cellWidthPx = with(density) { cellWidthDp.dp.toPx() }
        val cellHeightPx = with(density) { cellHeightDp.dp.toPx() }

        val labelColor = if (wallpaper.isLightFriendly && !palette.isDark) {
            Color(0xFF0F172A)
        } else {
            Color(0xFFF8FAFC)
        }
        val labelBgColor = if (wallpaper.isLightFriendly && !palette.isDark) {
            Color(0x99FFFFFF)
        } else {
            Color(0x88080C14)
        }

        shortcuts.forEach { shortcut ->
            val clampedCol = shortcut.gridCol.coerceIn(0, cols - 1)
            val clampedRow = shortcut.gridRow.coerceIn(0, rows - 1)
            val baseXPx = clampedCol * cellWidthPx
            val baseYPx = clampedRow * cellHeightPx

            var dragDeltaX by remember(shortcut.id, clampedCol, clampedRow) { mutableFloatStateOf(0f) }
            var dragDeltaY by remember(shortcut.id, clampedCol, clampedRow) { mutableFloatStateOf(0f) }
            var isDragging by remember(shortcut.id) { mutableStateOf(false) }

            val isSelected = selectedShortcutId == shortcut.id

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 10f else 1f)
                    .offset {
                        IntOffset(
                            (baseXPx + dragDeltaX).roundToInt(),
                            (baseYPx + dragDeltaY).roundToInt()
                        )
                    }
                    .width(cellWidthDp.dp)
                    .height(cellHeightDp.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isDragging -> palette.accent.copy(alpha = 0.25f)
                            isSelected -> palette.iconSelectionBg
                            else -> Color.Transparent
                        }
                    )
                    .then(
                        if (isSelected || isDragging) {
                            Modifier.border(
                                width = 1.dp,
                                color = palette.accent.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .testTag("desktop_icon_${shortcut.appId.name}")
                    .pointerInput(shortcut.id, cols, rows, cellWidthPx, cellHeightPx) {
                        detectTapGestures(
                            onTap = {
                                onSelectShortcut(shortcut.id)
                                onOpenShortcut(shortcut)
                            },
                            onLongPress = { localOffset ->
                                val globalOffset = Offset(
                                    baseXPx + localOffset.x,
                                    baseYPx + localOffset.y
                                )
                                onShortcutLongPress(shortcut, globalOffset)
                            }
                        )
                    }
                    .pointerInput(shortcut.id, cols, rows, cellWidthPx, cellHeightPx) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                onSelectShortcut(shortcut.id)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragDeltaX += dragAmount.x
                                dragDeltaY += dragAmount.y
                            },
                            onDragEnd = {
                                val targetCol = ((baseXPx + dragDeltaX + cellWidthPx * 0.5f) / cellWidthPx).toInt()
                                val targetRow = ((baseYPx + dragDeltaY + cellHeightPx * 0.5f) / cellHeightPx).toInt()
                                dragDeltaX = 0f
                                dragDeltaY = 0f
                                isDragging = false
                                if (targetCol != clampedCol || targetRow != clampedRow) {
                                    onMoveShortcut(shortcut.id, targetCol, targetRow, cols, rows)
                                }
                            },
                            onDragCancel = {
                                dragDeltaX = 0f
                                dragDeltaY = 0f
                                isDragging = false
                            }
                        )
                    }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val badgeSize = (42f * iconScale).coerceIn(34f, 52f).dp
                    Box(
                        modifier = Modifier
                            .size(badgeSize)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        shortcut.appId.badgeColor.copy(alpha = 0.92f),
                                        shortcut.appId.badgeColor.copy(alpha = 0.68f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = shortcut.appId.icon,
                            contentDescription = shortcut.label,
                            tint = Color.White,
                            modifier = Modifier.size((24f * iconScale).coerceIn(20f, 30f).dp)
                        )
                    }

                    if (showLabels) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = shortcut.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(labelBgColor)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
