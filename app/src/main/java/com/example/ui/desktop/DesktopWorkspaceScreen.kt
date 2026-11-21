package com.example.ui.desktop

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BuiltInAppId
import com.example.ui.apps.AboutApp
import com.example.ui.apps.AppearanceApp
import com.example.ui.apps.ApplicationsLauncherApp
import com.example.ui.apps.CalculatorApp
import com.example.ui.apps.CalendarApp
import com.example.ui.apps.ClockApp
import com.example.ui.apps.FileInfoApp
import com.example.ui.apps.SettingsApp
import com.example.ui.apps.SystemInfoApp
import com.example.ui.apps.TextEditorApp
import com.example.ui.apps.UnitConverterApp
import com.example.ui.apps.WorkspaceFolderApp
import com.example.ui.theme.LocalDesktopPalette
import com.example.viewmodel.ContextMenuState
import com.example.viewmodel.DesktopViewModel
import kotlinx.coroutines.delay

@Composable
fun DesktopWorkspaceScreen(viewModel: DesktopViewModel) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val palette = LocalDesktopPalette.current

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val windows by viewModel.windows.collectAsStateWithLifecycle()
    val activeWindowId by viewModel.activeWindowId.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val calendarNotes by viewModel.calendarNotes.collectAsStateWithLifecycle()
    val selectedEditorDocId by viewModel.selectedEditorDocumentId.collectAsStateWithLifecycle()
    val isStartMenuOpen by viewModel.isStartMenuOpen.collectAsStateWithLifecycle()
    val isQuickSettingsOpen by viewModel.isQuickSettingsOpen.collectAsStateWithLifecycle()
    val isNotificationsOpen by viewModel.isNotificationsOpen.collectAsStateWithLifecycle()
    val contextMenuState by viewModel.contextMenuState.collectAsStateWithLifecycle()
    val selectedShortcutId by viewModel.selectedShortcutId.collectAsStateWithLifecycle()
    val pendingCloseWindowId by viewModel.pendingCloseWindowId.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showPowerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(2600L)
            viewModel.clearStatusBanner()
        }
    }

    // Handle Android Back gesture gracefully inside desktop environment
    val hasActiveOverlay = isStartMenuOpen || isQuickSettingsOpen || isNotificationsOpen ||
        contextMenuState !is ContextMenuState.None
    val topVisibleWindow = windows.filterNot { it.isMinimized }.maxByOrNull { it.zIndex }

    BackHandler(enabled = hasActiveOverlay || topVisibleWindow != null) {
        when {
            hasActiveOverlay -> viewModel.closeAllOverlays()
            topVisibleWindow != null -> viewModel.minimizeWindow(topVisibleWindow.instanceId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main Desktop Workspace Area (above Taskbar)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
        ) {
            val workspaceWidthDp = maxWidth.value
            val workspaceHeightDp = maxHeight.value
            val workspaceWidthPx = with(density) { maxWidth.toPx() }
            val workspaceHeightPx = with(density) { maxHeight.toPx() }

            // 1. Wallpaper Background
            WallpaperLayer(
                wallpaper = preferences.wallpaper,
                accentPreset = preferences.accentPreset,
                isDarkTheme = preferences.isDarkTheme,
                onDesktopTap = {
                    viewModel.closeAllOverlays()
                    viewModel.selectShortcut(null)
                },
                onDesktopLongPress = { offset ->
                    viewModel.showDesktopContextMenu(offset.x, offset.y)
                }
            )

            // 2. Desktop Icons & Shortcuts Grid
            DesktopIconsGrid(
                shortcuts = preferences.desktopShortcuts,
                selectedShortcutId = selectedShortcutId,
                showLabels = preferences.showDesktopIconLabels,
                iconScale = preferences.iconScaleMultiplier,
                wallpaper = preferences.wallpaper,
                onSelectShortcut = { viewModel.selectShortcut(it) },
                onOpenShortcut = { shortcut -> viewModel.openApp(shortcut.appId) },
                onShortcutLongPress = { shortcut, offset ->
                    viewModel.showShortcutContextMenu(shortcut, offset.x, offset.y)
                },
                onMoveShortcut = { id, col, row, cols, rows ->
                    viewModel.moveDesktopShortcut(id, col, row, cols, rows)
                }
            )

            // 3. Open Application Windows Layer
            windows.forEach { win ->
                WindowFrame(
                    window = win,
                    isActive = win.instanceId == activeWindowId,
                    windowTransparency = preferences.windowTransparency,
                    workspaceWidthDp = workspaceWidthDp,
                    workspaceHeightDp = workspaceHeightDp,
                    onFocus = {
                        viewModel.closeAllOverlays()
                        viewModel.focusWindow(win.instanceId)
                    },
                    onMinimize = { viewModel.minimizeWindow(win.instanceId) },
                    onToggleMaximize = { viewModel.toggleMaximizeWindow(win.instanceId) },
                    onClose = { viewModel.requestCloseWindow(win.instanceId) },
                    onMove = { dx, dy ->
                        viewModel.moveWindow(win.instanceId, dx, dy, workspaceWidthDp, workspaceHeightDp)
                    },
                    onResize = { dw, dh ->
                        viewModel.resizeWindow(win.instanceId, dw, dh, workspaceWidthDp, workspaceHeightDp)
                    }
                ) {
                    when (win.appId) {
                        BuiltInAppId.CALCULATOR -> CalculatorApp()
                        BuiltInAppId.TEXT_EDITOR -> TextEditorApp(
                            documents = documents,
                            requestedDocumentId = selectedEditorDocId,
                            onConsumeRequestedDoc = { viewModel.consumeSelectedEditorDocumentId() },
                            onSaveDocument = { id, title, content, onSaved ->
                                viewModel.saveDocument(id, title, content, onSaved)
                            },
                            onDeleteDocument = { id, title ->
                                viewModel.deleteDocument(id, title)
                            },
                            onShowMessage = { viewModel.showStatus(it) }
                        )
                        BuiltInAppId.SYSTEM_INFO -> SystemInfoApp()
                        BuiltInAppId.CLOCK -> ClockApp(
                            onTimerComplete = { msg ->
                                viewModel.addNotification("Timer Completed", msg, BuiltInAppId.CLOCK)
                                viewModel.showStatus(msg)
                            }
                        )
                        BuiltInAppId.CALENDAR -> CalendarApp(
                            calendarNotes = calendarNotes,
                            onSaveNote = { key, note -> viewModel.saveCalendarNote(key, note) }
                        )
                        BuiltInAppId.FILE_INFO -> FileInfoApp(documents = documents)
                        BuiltInAppId.UNIT_CONVERTER -> UnitConverterApp()
                        BuiltInAppId.WORKSPACE_FOLDER -> WorkspaceFolderApp(
                            documents = documents,
                            onOpenDocumentInEditor = { docId -> viewModel.openDocumentInEditor(docId) },
                            onCreateNewDocument = { viewModel.openApp(BuiltInAppId.TEXT_EDITOR) },
                            onDeleteDocument = { id, title -> viewModel.deleteDocument(id, title) },
                            onOpenApp = { appId -> viewModel.openApp(appId) }
                        )
                        BuiltInAppId.APPLICATIONS -> ApplicationsLauncherApp(
                            preferences = preferences,
                            onOpenApp = { viewModel.openApp(it) },
                            onTogglePinDesktop = { viewModel.togglePinToDesktop(it) },
                            onTogglePinTaskbar = { viewModel.togglePinToTaskbar(it) },
                            onTogglePinStart = { viewModel.togglePinToStartMenu(it) },
                            onShowMessage = { viewModel.showStatus(it) }
                        )
                        BuiltInAppId.APPEARANCE -> AppearanceApp(
                            preferences = preferences,
                            onSetDarkTheme = { viewModel.setDarkTheme(it) },
                            onSetAccent = { viewModel.setAccentPreset(it) },
                            onSetWallpaper = { viewModel.setWallpaper(it) },
                            onSetTaskbarTransparency = { viewModel.setTaskbarTransparency(it) },
                            onSetWindowTransparency = { viewModel.setWindowTransparency(it) },
                            onSetCompactTaskbar = { viewModel.setCompactTaskbar(it) },
                            onSetShowDesktopLabels = { viewModel.setShowDesktopIconLabels(it) },
                            onSetFontScale = { viewModel.setFontScaleMultiplier(it) },
                            onSetIconScale = { viewModel.setIconScaleMultiplier(it) }
                        )
                        BuiltInAppId.SETTINGS -> SettingsApp(
                            preferences = preferences,
                            documentCount = documents.size,
                            onSetDarkTheme = { viewModel.setDarkTheme(it) },
                            onSetAccent = { viewModel.setAccentPreset(it) },
                            onSetWallpaper = { viewModel.setWallpaper(it) },
                            onSetTaskbarTransparency = { viewModel.setTaskbarTransparency(it) },
                            onSetWindowTransparency = { viewModel.setWindowTransparency(it) },
                            onSetCompactTaskbar = { viewModel.setCompactTaskbar(it) },
                            onSetShowClockSeconds = { viewModel.setShowClockSeconds(it) },
                            onSetShowDesktopLabels = { viewModel.setShowDesktopIconLabels(it) },
                            onSetFontScale = { viewModel.setFontScaleMultiplier(it) },
                            onSetIconScale = { viewModel.setIconScaleMultiplier(it) },
                            onSetConfirmWindowClose = { viewModel.setConfirmWindowClose(it) },
                            onSetStartupApp = { viewModel.setStartupApp(it) },
                            onOpenApplications = { viewModel.openApp(BuiltInAppId.APPLICATIONS) },
                            onOpenAbout = { viewModel.openApp(BuiltInAppId.ABOUT) },
                            onResetAppearanceOnly = { viewModel.resetAppearanceOnly() },
                            onResetAll = { clearDocs -> viewModel.resetAllSettingsAndData(clearDocs) }
                        )
                        BuiltInAppId.ABOUT -> AboutApp()
                    }
                }
            }

            // 4. Floating Start Menu (bottom-left)
            StartMenu(
                visible = isStartMenuOpen,
                preferences = preferences,
                onLaunchApp = { viewModel.openApp(it) },
                onTogglePinStart = { viewModel.togglePinToStartMenu(it) },
                onPowerClick = {
                    viewModel.closeAllOverlays()
                    showPowerDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .zIndex(900f)
                    .padding(start = 10.dp, bottom = 10.dp)
            )

            // 5. Quick Settings Flyout (bottom-right)
            QuickSettingsFlyout(
                visible = isQuickSettingsOpen,
                preferences = preferences,
                onToggleTheme = { viewModel.setDarkTheme(it) },
                onSelectAccent = { viewModel.setAccentPreset(it) },
                onSelectWallpaper = { viewModel.setWallpaper(it) },
                onWindowTransparencyChange = { viewModel.setWindowTransparency(it) },
                onCompactTaskbarChange = { viewModel.setCompactTaskbar(it) },
                onOpenFullSettings = { viewModel.openApp(BuiltInAppId.SETTINGS) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(900f)
                    .padding(end = 10.dp, bottom = 10.dp)
            )

            // 6. Notification Center Flyout (bottom-right)
            NotificationCenterFlyout(
                visible = isNotificationsOpen,
                notifications = notifications,
                onDismissNotification = { viewModel.dismissNotification(it) },
                onClearAll = { viewModel.clearNotifications() },
                onOpenApp = { viewModel.openApp(it) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(900f)
                    .padding(end = 10.dp, bottom = 10.dp)
            )

            // 7. Desktop / Shortcut Context Menu Overlay
            Box(modifier = Modifier.zIndex(950f)) {
                DesktopContextMenuOverlay(
                    state = contextMenuState,
                    workspaceWidthPx = workspaceWidthPx,
                    workspaceHeightPx = workspaceHeightPx,
                    taskbarPinnedApps = preferences.taskbarPinnedApps,
                    onDismiss = { viewModel.closeAllOverlays() },
                    onRefreshDesktop = { viewModel.refreshDesktop() },
                    onOpenWallpaperSettings = { viewModel.openApp(BuiltInAppId.APPEARANCE) },
                    onOpenApplications = { viewModel.openApp(BuiltInAppId.APPLICATIONS) },
                    onOpenSettings = { viewModel.openApp(BuiltInAppId.SETTINGS) },
                    onOpenAbout = { viewModel.openApp(BuiltInAppId.ABOUT) },
                    onOpenApp = { viewModel.openApp(it) },
                    onTogglePinTaskbar = { viewModel.togglePinToTaskbar(it) },
                    onRemoveShortcut = { viewModel.removeDesktopShortcut(it) }
                )
            }

            // 8. Non-intrusive Desktop Status Toast
            androidx.compose.animation.AnimatedVisibility(
                visible = statusBanner != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(990f)
                    .padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.startMenuSurface.copy(alpha = 0.95f),
                    tonalElevation = 10.dp,
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(10.dp))
                        .border(1.dp, palette.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(
                            text = statusBanner.orEmpty(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Bottom Desktop Taskbar
        Taskbar(
            preferences = preferences,
            windows = windows,
            activeWindowId = activeWindowId,
            isStartMenuOpen = isStartMenuOpen,
            isQuickSettingsOpen = isQuickSettingsOpen,
            isNotificationsOpen = isNotificationsOpen,
            notificationCount = notifications.size,
            onStartClick = { viewModel.toggleStartMenu() },
            onTaskbarAppClick = { viewModel.onTaskbarWindowClick(it) },
            onQuickSettingsClick = { viewModel.toggleQuickSettings() },
            onNotificationsClick = { viewModel.toggleNotifications() },
            onShowDesktopClick = { viewModel.minimizeAllWindows() },
            onClockClick = { viewModel.openApp(BuiltInAppId.CLOCK) }
        )
    }

    // Confirm Window Close Dialog (if user enabled confirmWindowClose in Settings)
    if (pendingCloseWindowId != null) {
        val targetTitle = windows.firstOrNull { it.instanceId == pendingCloseWindowId }?.title ?: "this window"
        AlertDialog(
            onDismissRequest = { viewModel.dismissCloseWindow() },
            title = { Text("Close Window?") },
            text = { Text("Are you sure you want to close $targetTitle?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmCloseWindow() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCloseWindow() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Power / Session Dialog
    if (showPowerDialog) {
        PowerMenuDialog(
            onDismiss = { showPowerDialog = false },
            onShowDesktop = { viewModel.minimizeAllWindows() },
            onRefreshDesktop = { viewModel.refreshDesktop() },
            onExitApp = {
                showPowerDialog = false
                (context as? Activity)?.finish()
            }
        )
    }
}
