package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.desktopDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "uiforge_desktop_prefs"
)

class DesktopPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val WALLPAPER = stringPreferencesKey("wallpaper")
        val TASKBAR_TRANSPARENCY = floatPreferencesKey("taskbar_transparency")
        val WINDOW_TRANSPARENCY = floatPreferencesKey("window_transparency")
        val COMPACT_TASKBAR = booleanPreferencesKey("compact_taskbar")
        val SHOW_CLOCK_SECONDS = booleanPreferencesKey("show_clock_seconds")
        val SHOW_DESKTOP_LABELS = booleanPreferencesKey("show_desktop_labels")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val ICON_SCALE = floatPreferencesKey("icon_scale")
        val CONFIRM_WINDOW_CLOSE = booleanPreferencesKey("confirm_window_close")
        val STARTUP_APP = stringPreferencesKey("startup_app")
        val DESKTOP_SHORTCUTS_JSON = stringPreferencesKey("desktop_shortcuts_json")
        val TASKBAR_PINNED_JSON = stringPreferencesKey("taskbar_pinned_json")
        val START_PINNED_JSON = stringPreferencesKey("start_pinned_json")
        val RECENT_APPS_JSON = stringPreferencesKey("recent_apps_json")
    }

    val preferencesFlow: Flow<DesktopPreferences> = context.desktopDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                emit(emptyPreferences())
            }
        }
        .map { prefs ->
            val accent = prefs[Keys.ACCENT_PRESET]?.let { name ->
                AccentPreset.entries.firstOrNull { it.name == name }
            } ?: AccentPreset.ELECTRIC_CYAN

            val wallpaper = prefs[Keys.WALLPAPER]?.let { name ->
                WallpaperOption.entries.firstOrNull { it.name == name }
            } ?: WallpaperOption.AURORA_MESH

            val startupApp = prefs[Keys.STARTUP_APP]?.let { name ->
                BuiltInAppId.entries.firstOrNull { it.name == name }
            }

            DesktopPreferences(
                isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
                accentPreset = accent,
                wallpaper = wallpaper,
                taskbarTransparency = (prefs[Keys.TASKBAR_TRANSPARENCY] ?: 0.88f).coerceIn(0.45f, 1.0f),
                windowTransparency = (prefs[Keys.WINDOW_TRANSPARENCY] ?: 0.96f).coerceIn(0.65f, 1.0f),
                compactTaskbar = prefs[Keys.COMPACT_TASKBAR] ?: false,
                showClockSeconds = prefs[Keys.SHOW_CLOCK_SECONDS] ?: true,
                showDesktopIconLabels = prefs[Keys.SHOW_DESKTOP_LABELS] ?: true,
                fontScaleMultiplier = (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.2f),
                iconScaleMultiplier = (prefs[Keys.ICON_SCALE] ?: 1.0f).coerceIn(0.8f, 1.25f),
                confirmWindowClose = prefs[Keys.CONFIRM_WINDOW_CLOSE] ?: false,
                startupApp = startupApp,
                desktopShortcuts = decodeShortcuts(prefs[Keys.DESKTOP_SHORTCUTS_JSON])
                    ?: defaultDesktopShortcuts(),
                taskbarPinnedApps = decodeAppList(prefs[Keys.TASKBAR_PINNED_JSON])
                    ?: defaultTaskbarPinnedApps(),
                startMenuPinnedApps = decodeAppList(prefs[Keys.START_PINNED_JSON])
                    ?: defaultStartMenuPinnedApps(),
                recentApps = decodeAppList(prefs[Keys.RECENT_APPS_JSON])
                    ?: listOf(BuiltInAppId.TEXT_EDITOR, BuiltInAppId.CALCULATOR, BuiltInAppId.SYSTEM_INFO)
            )
        }

    suspend fun updatePreferences(transform: (DesktopPreferences) -> DesktopPreferences) {
        // Read-modify-write safely inside DataStore edit transaction
        context.desktopDataStore.edit { prefs ->
            val current = DesktopPreferences(
                isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
                accentPreset = prefs[Keys.ACCENT_PRESET]?.let { n ->
                    AccentPreset.entries.firstOrNull { it.name == n }
                } ?: AccentPreset.ELECTRIC_CYAN,
                wallpaper = prefs[Keys.WALLPAPER]?.let { n ->
                    WallpaperOption.entries.firstOrNull { it.name == n }
                } ?: WallpaperOption.AURORA_MESH,
                taskbarTransparency = prefs[Keys.TASKBAR_TRANSPARENCY] ?: 0.88f,
                windowTransparency = prefs[Keys.WINDOW_TRANSPARENCY] ?: 0.96f,
                compactTaskbar = prefs[Keys.COMPACT_TASKBAR] ?: false,
                showClockSeconds = prefs[Keys.SHOW_CLOCK_SECONDS] ?: true,
                showDesktopIconLabels = prefs[Keys.SHOW_DESKTOP_LABELS] ?: true,
                fontScaleMultiplier = prefs[Keys.FONT_SCALE] ?: 1.0f,
                iconScaleMultiplier = prefs[Keys.ICON_SCALE] ?: 1.0f,
                confirmWindowClose = prefs[Keys.CONFIRM_WINDOW_CLOSE] ?: false,
                startupApp = prefs[Keys.STARTUP_APP]?.let { n ->
                    BuiltInAppId.entries.firstOrNull { it.name == n }
                },
                desktopShortcuts = decodeShortcuts(prefs[Keys.DESKTOP_SHORTCUTS_JSON])
                    ?: defaultDesktopShortcuts(),
                taskbarPinnedApps = decodeAppList(prefs[Keys.TASKBAR_PINNED_JSON])
                    ?: defaultTaskbarPinnedApps(),
                startMenuPinnedApps = decodeAppList(prefs[Keys.START_PINNED_JSON])
                    ?: defaultStartMenuPinnedApps(),
                recentApps = decodeAppList(prefs[Keys.RECENT_APPS_JSON])
                    ?: listOf(BuiltInAppId.TEXT_EDITOR, BuiltInAppId.CALCULATOR, BuiltInAppId.SYSTEM_INFO)
            )

            val updated = transform(current)
            prefs[Keys.IS_DARK_THEME] = updated.isDarkTheme
            prefs[Keys.ACCENT_PRESET] = updated.accentPreset.name
            prefs[Keys.WALLPAPER] = updated.wallpaper.name
            prefs[Keys.TASKBAR_TRANSPARENCY] = updated.taskbarTransparency.coerceIn(0.45f, 1.0f)
            prefs[Keys.WINDOW_TRANSPARENCY] = updated.windowTransparency.coerceIn(0.65f, 1.0f)
            prefs[Keys.COMPACT_TASKBAR] = updated.compactTaskbar
            prefs[Keys.SHOW_CLOCK_SECONDS] = updated.showClockSeconds
            prefs[Keys.SHOW_DESKTOP_LABELS] = updated.showDesktopIconLabels
            prefs[Keys.FONT_SCALE] = updated.fontScaleMultiplier.coerceIn(0.85f, 1.2f)
            prefs[Keys.ICON_SCALE] = updated.iconScaleMultiplier.coerceIn(0.8f, 1.25f)
            prefs[Keys.CONFIRM_WINDOW_CLOSE] = updated.confirmWindowClose
            if (updated.startupApp != null) {
                prefs[Keys.STARTUP_APP] = updated.startupApp.name
            } else {
                prefs.remove(Keys.STARTUP_APP)
            }
            prefs[Keys.DESKTOP_SHORTCUTS_JSON] = encodeShortcuts(updated.desktopShortcuts)
            prefs[Keys.TASKBAR_PINNED_JSON] = encodeAppList(updated.taskbarPinnedApps)
            prefs[Keys.START_PINNED_JSON] = encodeAppList(updated.startMenuPinnedApps)
            prefs[Keys.RECENT_APPS_JSON] = encodeAppList(updated.recentApps)
        }
    }

    suspend fun resetToDefaults() {
        context.desktopDataStore.edit { it.clear() }
    }

    private fun encodeShortcuts(shortcuts: List<DesktopShortcut>): String {
        val arr = JSONArray()
        shortcuts.forEach { sc ->
            arr.put(
                JSONObject().apply {
                    put("id", sc.id)
                    put("appId", sc.appId.name)
                    put("label", sc.label)
                    put("col", sc.gridCol)
                    put("row", sc.gridRow)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeShortcuts(raw: String?): List<DesktopShortcut>? {
        if (raw.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val appId = BuiltInAppId.entries.firstOrNull { it.name == obj.optString("appId") }
                        ?: continue
                    add(
                        DesktopShortcut(
                            id = obj.optString("id", "sc_${appId.name}"),
                            appId = appId,
                            label = obj.optString("label", appId.title),
                            gridCol = obj.optInt("col", 0).coerceAtLeast(0),
                            gridRow = obj.optInt("row", 0).coerceAtLeast(0)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encodeAppList(apps: List<BuiltInAppId>): String {
        val arr = JSONArray()
        apps.forEach { arr.put(it.name) }
        return arr.toString()
    }

    private fun decodeAppList(raw: String?): List<BuiltInAppId>? {
        if (raw.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val name = arr.optString(i)
                    BuiltInAppId.entries.firstOrNull { it.name == name }?.let { add(it) }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
