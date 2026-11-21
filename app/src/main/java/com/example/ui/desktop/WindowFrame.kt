package com.example.ui.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.DesktopWindowState
import com.example.ui.theme.LocalDesktopPalette
import com.example.ui.theme.WindowCloseRed
import com.example.ui.theme.WindowMaximizeGreen
import com.example.ui.theme.WindowMinimizeAmber
import kotlin.math.roundToInt

@Composable
fun WindowFrame(
    window: DesktopWindowState,
    isActive: Boolean,
    windowTransparency: Float,
    workspaceWidthDp: Float,
    workspaceHeightDp: Float,
    onFocus: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    onMove: (deltaXDp: Float, deltaYDp: Float) -> Unit,
    onResize: (deltaWDp: Float, deltaHDp: Float) -> Unit,
    content: @Composable () -> Unit
) {
    val palette = LocalDesktopPalette.current
    val density = LocalDensity.current

    val targetX = if (window.isMaximized) 0f else window.offsetX.coerceIn(0f, (workspaceWidthDp - 140f).coerceAtLeast(0f))
    val targetY = if (window.isMaximized) 0f else window.offsetY.coerceIn(0f, (workspaceHeightDp - 80f).coerceAtLeast(0f))
    val targetWidth = if (window.isMaximized) workspaceWidthDp else window.widthDp.coerceAtMost(workspaceWidthDp)
    val targetHeight = if (window.isMaximized) workspaceHeightDp else window.heightDp.coerceAtMost(workspaceHeightDp)

    val cornerShape = if (window.isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp)

    AnimatedVisibility(
        visible = !window.isMinimized,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
            scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
            scaleOut(targetScale = 0.92f),
        modifier = Modifier
            .zIndex(window.zIndex.toFloat())
            .offset {
                with(density) {
                    IntOffset(targetX.dp.roundToPx(), targetY.dp.roundToPx())
                }
            }
            .width(targetWidth.dp)
            .height(targetHeight.dp)
    ) {
        Surface(
            shape = cornerShape,
            color = palette.windowSurface.copy(alpha = if (window.isMaximized) 1f else windowTransparency),
            tonalElevation = if (isActive) 8.dp else 2.dp,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isActive) 18.dp else 6.dp,
                    shape = cornerShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .border(
                    width = if (isActive) 1.25.dp else 1.dp,
                    color = if (isActive) palette.accent.copy(alpha = 0.55f) else palette.borderSubtle,
                    shape = cornerShape
                )
                .pointerInput(window.instanceId) {
                    detectTapGestures(onPress = {
                        onFocus()
                    })
                }
                .testTag("window_frame_${window.appId.name}")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Draggable Title Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(
                                if (isActive) palette.windowTitleBarActive else palette.windowTitleBarInactive
                            )
                            .pointerInput(window.instanceId, window.isMaximized) {
                                detectTapGestures(
                                    onPress = { onFocus() },
                                    onDoubleTap = { onToggleMaximize() }
                                )
                            }
                            .pointerInput(window.instanceId, window.isMaximized) {
                                detectDragGestures(
                                    onDragStart = { onFocus() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (!window.isMaximized) {
                                            val dxDp = with(density) { dragAmount.x.toDp().value }
                                            val dyDp = with(density) { dragAmount.y.toDp().value }
                                            onMove(dxDp, dyDp)
                                        }
                                    }
                                )
                            }
                            .padding(start = 12.dp, end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(window.appId.badgeColor.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = window.appId.icon,
                                    contentDescription = null,
                                    tint = window.appId.badgeColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = window.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!window.isMaximized) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = "Drag window",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Window control buttons (Minimize, Maximize/Restore, Close)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = onMinimize,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("window_minimize_${window.appId.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(WindowMinimizeAmber.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.HorizontalRule,
                                        contentDescription = "Minimize ${window.title}",
                                        tint = WindowMinimizeAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleMaximize,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("window_maximize_${window.appId.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(WindowMaximizeGreen.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (window.isMaximized) {
                                            Icons.Rounded.FilterNone
                                        } else {
                                            Icons.Rounded.CropSquare
                                        },
                                        contentDescription = if (window.isMaximized) {
                                            "Restore ${window.title}"
                                        } else {
                                            "Maximize ${window.title}"
                                        },
                                        tint = WindowMaximizeGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("window_close_${window.appId.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(WindowCloseRed.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close ${window.title}",
                                        tint = WindowCloseRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.6f), thickness = 1.dp)

                    // Window content area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        content()
                    }
                }

                // Bottom-right resize handle for floating windows
                if (!window.isMaximized) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .pointerInput(window.instanceId) {
                                detectDragGestures(
                                    onDragStart = { onFocus() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val dwDp = with(density) { dragAmount.x.toDp().value }
                                        val dhDp = with(density) { dragAmount.y.toDp().value }
                                        onResize(dwDp, dhDp)
                                    }
                                )
                            },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(palette.accent.copy(alpha = 0.45f))
                        )
                    }
                }
            }
        }
    }
}
