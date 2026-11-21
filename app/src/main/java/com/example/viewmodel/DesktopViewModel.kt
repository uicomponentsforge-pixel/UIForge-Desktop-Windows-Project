package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccentPreset
import com.example.data.BuiltInAppId
import com.example.data.DesktopNotification
import com.example.data.DesktopPreferences
import com.example.data.DesktopPreferencesRepository
import com.example.data.DesktopShortcut
import com.example.data.DesktopWindowState
import com.example.data.WallpaperOption
import com.example.data.local.DesktopDatabase
import com.example.data.local.DocumentRepository
import com.example.data.local.TextDocumentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

sealed interface ContextMenuState {
    data object None : ContextMenuState
    data class DesktopEmpty(val xPx: Float, val yPx: Float) : ContextMenuState
    data class ShortcutIcon(val shortcut: DesktopShortcut, val xPx: Float, val yPx: Float) : ContextMenuState
}

class DesktopViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = DesktopPreferencesRepository(application)
    private val docRepo = DocumentRepository(DesktopDatabase.getInstance(application).documentDao())

    val preferences: StateFlow<DesktopPreferences> = prefsRepo.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DesktopPreferences()
        )

    val documents: StateFlow<List<TextDocumentEntity>> = docRepo.allDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val calendarNotes: StateFlow<Map<String, String>> = docRepo.allCalendarNotes
        .map { list -> list.associate { it.dateKey to it.note } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _windows = MutableStateFlow<List<DesktopWindowState>>(emptyList())
    val windows: StateFlow<List<DesktopWindowState>> = _windows.asStateFlow()

    private val _activeWindowId = MutableStateFlow<String?>(null)
    val activeWindowId: StateFlow<String?> = _activeWindowId.asStateFlow()

    private val _selectedEditorDocumentId = MutableStateFlow<Long?>(null)
    val selectedEditorDocumentId: StateFlow<Long?> = _selectedEditorDocumentId.asStateFlow()

    private val _isStartMenuOpen = MutableStateFlow(false)
    val isStartMenuOpen: StateFlow<Boolean> = _isStartMenuOpen.asStateFlow()

    private val _isQuickSettingsOpen = MutableStateFlow(false)
    val isQuickSettingsOpen: StateFlow<Boolean> = _isQuickSettingsOpen.asStateFlow()

    private val _isNotificationsOpen = MutableStateFlow(false)
    val isNotificationsOpen: StateFlow<Boolean> = _isNotificationsOpen.asStateFlow()

    private val _contextMenuState = MutableStateFlow<ContextMenuState>(ContextMenuState.None)
    val contextMenuState: StateFlow<ContextMenuState> = _contextMenuState.asStateFlow()

    private val _selectedShortcutId = MutableStateFlow<String?>(null)
    val selectedShortcutId: StateFlow<String?> = _selectedShortcutId.asStateFlow()

    private val _pendingCloseWindowId = MutableStateFlow<String?>(null)
    val pendingCloseWindowId: StateFlow<String?> = _pendingCloseWindowId.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            DesktopNotification(
                id = "notif_welcome",
                title = "UIForge Desktop Ready",
                message = "Offline workspace initialized. All tools and local storage are active.",
                appId = BuiltInAppId.ABOUT
            )
        )
    )
    val notifications: StateFlow<List<DesktopNotification>> = _notifications.asStateFlow()

    private var nextZIndex = 10
    private var launchCounter = 0

    init {
        viewModelScope.launch {
            docRepo.ensureSeededWelcomeNote()
            val initialPrefs = prefsRepo.preferencesFlow.first()
            initialPrefs.startupApp?.let { app ->
                openApp(app)
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showStatus(message: String) {
        _statusBanner.value = message
    }

    fun addNotification(title: String, message: String, appId: BuiltInAppId? = null) {
        val item = DesktopNotification(
            id = "notif_${System.currentTimeMillis()}_${_notifications.value.size}",
            title = title,
            message = message,
            appId = appId
        )
        _notifications.update { (listOf(item) + it).take(20) }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun dismissNotification(id: String) {
        _notifications.update { list -> list.filterNot { it.id == id } }
    }

    fun closeAllOverlays() {
        _isStartMenuOpen.value = false
        _isQuickSettingsOpen.value = false
        _isNotificationsOpen.value = false
        _contextMenuState.value = ContextMenuState.None
    }

    fun toggleStartMenu() {
        val next = !_isStartMenuOpen.value
        closeAllOverlays()
        _isStartMenuOpen.value = next
    }

    fun toggleQuickSettings() {
        val next = !_isQuickSettingsOpen.value
        closeAllOverlays()
        _isQuickSettingsOpen.value = next
    }

    fun toggleNotifications() {
        val next = !_isNotificationsOpen.value
        closeAllOverlays()
        _isNotificationsOpen.value = next
    }

    fun showDesktopContextMenu(xPx: Float, yPx: Float) {
        closeAllOverlays()
        _selectedShortcutId.value = null
        _contextMenuState.value = ContextMenuState.DesktopEmpty(xPx, yPx)
    }

    fun showShortcutContextMenu(shortcut: DesktopShortcut, xPx: Float, yPx: Float) {
        closeAllOverlays()
        _selectedShortcutId.value = shortcut.id
        _contextMenuState.value = ContextMenuState.ShortcutIcon(shortcut, xPx, yPx)
    }

    fun selectShortcut(id: String?) {
        _selectedShortcutId.value = id
    }

    fun refreshDesktop() {
        closeAllOverlays()
        _selectedShortcutId.value = null
        _statusBanner.value = "Desktop workspace refreshed"
    }

    fun openDocumentInEditor(docId: Long) {
        _selectedEditorDocumentId.value = docId
        openApp(BuiltInAppId.TEXT_EDITOR)
    }

    fun consumeSelectedEditorDocumentId() {
        _selectedEditorDocumentId.value = null
    }

    fun openApp(appId: BuiltInAppId) {
        closeAllOverlays()
        val existing = _windows.value.firstOrNull { it.appId == appId }
        if (existing != null) {
            focusWindow(existing.instanceId)
            recordRecentApp(appId)
            return
        }

        launchCounter += 1
        nextZIndex += 1
        val cascadeOffset = ((launchCounter - 1) % 5) * 24f
        val newWindow = DesktopWindowState(
            instanceId = "win_${appId.name}_$launchCounter",
            appId = appId,
            title = appId.title,
            offsetX = 110f + cascadeOffset,
            offsetY = 26f + cascadeOffset * 0.75f,
            widthDp = appId.defaultWidthDp,
            heightDp = appId.defaultHeightDp,
            isMinimized = false,
            isMaximized = false,
            zIndex = nextZIndex
        )
        _windows.update { it + newWindow }
        _activeWindowId.value = newWindow.instanceId
        recordRecentApp(appId)
    }

    private fun recordRecentApp(appId: BuiltInAppId) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val updated = (listOf(appId) + prefs.recentApps.filterNot { it == appId }).take(6)
                prefs.copy(recentApps = updated)
            }
        }
    }

    fun focusWindow(instanceId: String) {
        nextZIndex += 1
        _windows.update { list ->
            list.map { win ->
                if (win.instanceId == instanceId) {
                    win.copy(isMinimized = false, zIndex = nextZIndex)
                } else {
                    win
                }
            }
        }
        _activeWindowId.value = instanceId
    }

    fun onTaskbarWindowClick(appId: BuiltInAppId) {
        closeAllOverlays()
        val existing = _windows.value.firstOrNull { it.appId == appId }
        if (existing == null) {
            openApp(appId)
            return
        }
        if (existing.isMinimized) {
            focusWindow(existing.instanceId)
        } else if (_activeWindowId.value == existing.instanceId) {
            minimizeWindow(existing.instanceId)
        } else {
            focusWindow(existing.instanceId)
        }
    }

    fun minimizeWindow(instanceId: String) {
        _windows.update { list ->
            list.map { win ->
                if (win.instanceId == instanceId) win.copy(isMinimized = true) else win
            }
        }
        if (_activeWindowId.value == instanceId) {
            val topVisible = _windows.value
                .filter { it.instanceId != instanceId && !it.isMinimized }
                .maxByOrNull { it.zIndex }
            _activeWindowId.value = topVisible?.instanceId
        }
    }

    fun minimizeAllWindows() {
        closeAllOverlays()
        _windows.update { list -> list.map { it.copy(isMinimized = true) } }
        _activeWindowId.value = null
    }

    fun toggleMaximizeWindow(instanceId: String) {
        nextZIndex += 1
        _windows.update { list ->
            list.map { win ->
                if (win.instanceId == instanceId) {
                    win.copy(isMaximized = !win.isMaximized, isMinimized = false, zIndex = nextZIndex)
                } else {
                    win
                }
            }
        }
        _activeWindowId.value = instanceId
    }

    fun requestCloseWindow(instanceId: String) {
        if (preferences.value.confirmWindowClose) {
            _pendingCloseWindowId.value = instanceId
        } else {
            closeWindowNow(instanceId)
        }
    }

    fun confirmCloseWindow() {
        val id = _pendingCloseWindowId.value ?: return
        _pendingCloseWindowId.value = null
        closeWindowNow(id)
    }

    fun dismissCloseWindow() {
        _pendingCloseWindowId.value = null
    }

    private fun closeWindowNow(instanceId: String) {
        _windows.update { list -> list.filterNot { it.instanceId == instanceId } }
        if (_activeWindowId.value == instanceId) {
            val topVisible = _windows.value.filterNot { it.isMinimized }.maxByOrNull { it.zIndex }
            _activeWindowId.value = topVisible?.instanceId
        }
    }

    fun moveWindow(instanceId: String, deltaX: Float, deltaY: Float, workspaceW: Float, workspaceH: Float) {
        _windows.update { list ->
            list.map { win ->
                if (win.instanceId == instanceId && !win.isMaximized) {
                    val maxX = max(40f, workspaceW - 120f)
                    val maxY = max(20f, workspaceH - 64f)
                    val nextX = (win.offsetX + deltaX).coerceIn(-win.widthDp + 120f, maxX)
                    val nextY = (win.offsetY + deltaY).coerceIn(0f, maxY)
                    win.copy(offsetX = nextX, offsetY = nextY)
                } else {
                    win
                }
            }
        }
    }

    fun resizeWindow(instanceId: String, deltaW: Float, deltaH: Float, workspaceW: Float, workspaceH: Float) {
        _windows.update { list ->
            list.map { win ->
                if (win.instanceId == instanceId && !win.isMaximized) {
                    val nextW = (win.widthDp + deltaW).coerceIn(280f, max(320f, workspaceW - 16f))
                    val nextH = (win.heightDp + deltaH).coerceIn(240f, max(260f, workspaceH - 16f))
                    win.copy(widthDp = nextW, heightDp = nextH)
                } else {
                    win
                }
            }
        }
    }

    // Room document & calendar note mutations
    fun saveDocument(id: Long, title: String, content: String, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val savedId = docRepo.saveDocument(id, title, content)
            _statusBanner.value = "Saved \"$title\""
            onSaved(savedId)
        }
    }

    fun deleteDocument(id: Long, title: String) {
        viewModelScope.launch {
            docRepo.deleteDocument(id)
            _statusBanner.value = "Deleted \"$title\""
        }
    }

    fun saveCalendarNote(dateKey: String, note: String) {
        viewModelScope.launch {
            docRepo.saveCalendarNote(dateKey, note)
            _statusBanner.value = if (note.isBlank()) "Cleared note for $dateKey" else "Saved note for $dateKey"
        }
    }

    // Desktop shortcuts & pinning
    fun isPinnedToDesktop(appId: BuiltInAppId): Boolean =
        preferences.value.desktopShortcuts.any { it.appId == appId }

    fun togglePinToDesktop(appId: BuiltInAppId) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val existing = prefs.desktopShortcuts.firstOrNull { it.appId == appId }
                if (existing != null) {
                    prefs.copy(desktopShortcuts = prefs.desktopShortcuts.filterNot { it.appId == appId })
                } else {
                    val occupied = prefs.desktopShortcuts.map { it.gridCol to it.gridRow }.toSet()
                    var placed = 0 to 0
                    outer@ for (col in 0..7) {
                        for (row in 0..3) {
                            if ((col to row) !in occupied) {
                                placed = col to row
                                break@outer
                            }
                        }
                    }
                    val newShortcut = DesktopShortcut(
                        id = "sc_${appId.name}_${System.currentTimeMillis()}",
                        appId = appId,
                        label = appId.title,
                        gridCol = placed.first,
                        gridRow = placed.second
                    )
                    prefs.copy(desktopShortcuts = prefs.desktopShortcuts + newShortcut)
                }
            }
        }
    }

    fun removeDesktopShortcut(shortcutId: String) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                prefs.copy(desktopShortcuts = prefs.desktopShortcuts.filterNot { it.id == shortcutId })
            }
            _statusBanner.value = "Shortcut removed from desktop"
        }
    }

    fun moveDesktopShortcut(shortcutId: String, targetCol: Int, targetRow: Int, maxCols: Int, maxRows: Int) {
        val safeCol = targetCol.coerceIn(0, max(0, maxCols - 1))
        val safeRow = targetRow.coerceIn(0, max(0, maxRows - 1))
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val moving = prefs.desktopShortcuts.firstOrNull { it.id == shortcutId } ?: return@updatePreferences prefs
                val occupant = prefs.desktopShortcuts.firstOrNull {
                    it.id != shortcutId && it.gridCol == safeCol && it.gridRow == safeRow
                }
                val updated = prefs.desktopShortcuts.map { sc ->
                    when (sc.id) {
                        moving.id -> sc.copy(gridCol = safeCol, gridRow = safeRow)
                        occupant?.id -> sc.copy(gridCol = moving.gridCol, gridRow = moving.gridRow)
                        else -> sc
                    }
                }
                prefs.copy(desktopShortcuts = updated)
            }
        }
    }

    fun togglePinToTaskbar(appId: BuiltInAppId) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val list = prefs.taskbarPinnedApps
                val updated = if (appId in list) list - appId else list + appId
                prefs.copy(taskbarPinnedApps = updated)
            }
        }
    }

    fun togglePinToStartMenu(appId: BuiltInAppId) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val list = prefs.startMenuPinnedApps
                val updated = if (appId in list) list - appId else list + appId
                prefs.copy(startMenuPinnedApps = updated)
            }
        }
    }

    // Personalization & Settings setters
    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            prefsRepo.updatePreferences { prefs ->
                val nextWallpaper = if (!isDark && prefs.wallpaper == WallpaperOption.AURORA_MESH) {
                    WallpaperOption.DAYLIGHT_GLASS
                } else if (isDark && prefs.wallpaper == WallpaperOption.DAYLIGHT_GLASS) {
                    WallpaperOption.AURORA_MESH
                } else {
                    prefs.wallpaper
                }
                prefs.copy(isDarkTheme = isDark, wallpaper = nextWallpaper)
            }
        }
    }

    fun setAccentPreset(preset: AccentPreset) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(accentPreset = preset) } }
    }

    fun setWallpaper(wallpaper: WallpaperOption) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(wallpaper = wallpaper) } }
    }

    fun setTaskbarTransparency(alpha: Float) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(taskbarTransparency = alpha) } }
    }

    fun setWindowTransparency(alpha: Float) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(windowTransparency = alpha) } }
    }

    fun setCompactTaskbar(compact: Boolean) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(compactTaskbar = compact) } }
    }

    fun setShowClockSeconds(show: Boolean) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(showClockSeconds = show) } }
    }

    fun setShowDesktopIconLabels(show: Boolean) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(showDesktopIconLabels = show) } }
    }

    fun setFontScaleMultiplier(scale: Float) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(fontScaleMultiplier = scale) } }
    }

    fun setIconScaleMultiplier(scale: Float) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(iconScaleMultiplier = scale) } }
    }

    fun setConfirmWindowClose(confirm: Boolean) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(confirmWindowClose = confirm) } }
    }

    fun setStartupApp(appId: BuiltInAppId?) {
        viewModelScope.launch { prefsRepo.updatePreferences { it.copy(startupApp = appId) } }
    }

    fun resetAppearanceOnly() {
        viewModelScope.launch {
            val defaults = DesktopPreferences()
            prefsRepo.updatePreferences { current ->
                current.copy(
                    isDarkTheme = defaults.isDarkTheme,
                    accentPreset = defaults.accentPreset,
                    wallpaper = defaults.wallpaper,
                    taskbarTransparency = defaults.taskbarTransparency,
                    windowTransparency = defaults.windowTransparency,
                    compactTaskbar = defaults.compactTaskbar,
                    showDesktopIconLabels = defaults.showDesktopIconLabels,
                    fontScaleMultiplier = defaults.fontScaleMultiplier,
                    iconScaleMultiplier = defaults.iconScaleMultiplier
                )
            }
            _statusBanner.value = "Default desktop appearance restored"
        }
    }

    fun resetAllSettingsAndData(clearDocuments: Boolean) {
        viewModelScope.launch {
            prefsRepo.resetToDefaults()
            if (clearDocuments) {
                docRepo.clearAllUserData()
            }
            _windows.value = emptyList()
            _activeWindowId.value = null
            _statusBanner.value = if (clearDocuments) {
                "All settings and local workspace data reset"
            } else {
                "Desktop settings restored to defaults"
            }
        }
    }
}
