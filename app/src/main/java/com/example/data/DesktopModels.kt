package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppCategory(val label: String) {
    PRODUCTIVITY("Productivity"),
    TOOLS("Tools & Utilities"),
    SYSTEM("System & Personalization")
}

enum class BuiltInAppId(
    val title: String,
    val subtitle: String,
    val category: AppCategory,
    val icon: ImageVector,
    val badgeColor: Color,
    val defaultWidthDp: Float,
    val defaultHeightDp: Float,
    val isFolder: Boolean = false
) {
    CALCULATOR(
        title = "Calculator",
        subtitle = "Arithmetic & quick math tape",
        category = AppCategory.TOOLS,
        icon = Icons.Rounded.Calculate,
        badgeColor = Color(0xFF06B6D4),
        defaultWidthDp = 340f,
        defaultHeightDp = 420f
    ),
    TEXT_EDITOR(
        title = "Text Editor",
        subtitle = "Create, edit & save local documents",
        category = AppCategory.PRODUCTIVITY,
        icon = Icons.Rounded.Description,
        badgeColor = Color(0xFF3B82F6),
        defaultWidthDp = 580f,
        defaultHeightDp = 420f
    ),
    SYSTEM_INFO(
        title = "System Info",
        subtitle = "Device, OS, memory & storage metrics",
        category = AppCategory.SYSTEM,
        icon = Icons.Rounded.Memory,
        badgeColor = Color(0xFF10B981),
        defaultWidthDp = 520f,
        defaultHeightDp = 400f
    ),
    CLOCK(
        title = "Clock",
        subtitle = "Digital clock, stopwatch & timer",
        category = AppCategory.TOOLS,
        icon = Icons.Rounded.Schedule,
        badgeColor = Color(0xFFF59E0B),
        defaultWidthDp = 460f,
        defaultHeightDp = 370f
    ),
    CALENDAR(
        title = "Calendar",
        subtitle = "Monthly calendar & day details",
        category = AppCategory.PRODUCTIVITY,
        icon = Icons.Rounded.CalendarMonth,
        badgeColor = Color(0xFFEC4899),
        defaultWidthDp = 500f,
        defaultHeightDp = 400f
    ),
    FILE_INFO(
        title = "File Inspector",
        subtitle = "Inspect device files & metadata",
        category = AppCategory.TOOLS,
        icon = Icons.Rounded.Storage,
        badgeColor = Color(0xFF8B5CF6),
        defaultWidthDp = 500f,
        defaultHeightDp = 390f
    ),
    UNIT_CONVERTER(
        title = "Unit Converter",
        subtitle = "Length, weight, temp, volume & data",
        category = AppCategory.TOOLS,
        icon = Icons.Rounded.SwapHoriz,
        badgeColor = Color(0xFF14B8A6),
        defaultWidthDp = 480f,
        defaultHeightDp = 400f
    ),
    WORKSPACE_FOLDER(
        title = "Workspace Files",
        subtitle = "Local documents & saved workspace",
        category = AppCategory.PRODUCTIVITY,
        icon = Icons.Rounded.Folder,
        badgeColor = Color(0xFFFBBF24),
        defaultWidthDp = 520f,
        defaultHeightDp = 380f,
        isFolder = true
    ),
    APPLICATIONS(
        title = "Applications",
        subtitle = "App launcher & shortcut manager",
        category = AppCategory.SYSTEM,
        icon = Icons.Rounded.Apps,
        badgeColor = Color(0xFF6366F1),
        defaultWidthDp = 600f,
        defaultHeightDp = 430f
    ),
    APPEARANCE(
        title = "Appearance",
        subtitle = "Theme, wallpaper, taskbar & icons",
        category = AppCategory.SYSTEM,
        icon = Icons.Rounded.Palette,
        badgeColor = Color(0xFFD946EF),
        defaultWidthDp = 590f,
        defaultHeightDp = 430f
    ),
    SETTINGS(
        title = "Settings",
        subtitle = "Desktop preferences & system controls",
        category = AppCategory.SYSTEM,
        icon = Icons.Rounded.Settings,
        badgeColor = Color(0xFF64748B),
        defaultWidthDp = 640f,
        defaultHeightDp = 440f
    ),
    ABOUT(
        title = "About UIForge",
        subtitle = "Version, stack & license info",
        category = AppCategory.SYSTEM,
        icon = Icons.Rounded.Info,
        badgeColor = Color(0xFF0EA5E9),
        defaultWidthDp = 470f,
        defaultHeightDp = 380f
    )
}

enum class AccentPreset(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color
) {
    ELECTRIC_CYAN("Electric Cyan", Color(0xFF06B6D4), Color(0xFF38BDF8), Color(0xFF818CF8)),
    COBALT_BLUE("Cobalt Blue", Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFF2DD4BF)),
    EMERALD_MINT("Emerald Mint", Color(0xFF10B981), Color(0xFF34D399), Color(0xFF38BDF8)),
    VIOLET_PULSE("Violet Pulse", Color(0xFF8B5CF6), Color(0xFFA78BFA), Color(0xFFF472B6)),
    AMBER_GOLD("Amber Gold", Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFB7185)),
    CORAL_ROSE("Coral Rose", Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFFA78BFA))
}

enum class WallpaperOption(
    val displayName: String,
    val subtitle: String,
    val isLightFriendly: Boolean
) {
    AURORA_MESH("Aurora Mesh", "Deep navy & cyan light ribbons", false),
    OBSIDIAN_BEAM("Obsidian Beam", "Brushed dark titanium studio", false),
    DAYLIGHT_GLASS("Daylight Glass", "Airy frosted alabaster waves", true),
    NEON_BLUEPRINT("Neon Grid", "Procedural accent blueprint grid", false),
    SOLAR_HORIZON("Solar Horizon", "Procedural soft dusk gradient", false)
}

data class DesktopShortcut(
    val id: String,
    val appId: BuiltInAppId,
    val label: String = appId.title,
    val gridCol: Int,
    val gridRow: Int
)

data class DesktopWindowState(
    val instanceId: String,
    val appId: BuiltInAppId,
    val title: String,
    val offsetX: Float,
    val offsetY: Float,
    val widthDp: Float,
    val heightDp: Float,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val zIndex: Int = 1
)

data class DesktopNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val appId: BuiltInAppId? = null
)

data class DesktopPreferences(
    val isDarkTheme: Boolean = true,
    val accentPreset: AccentPreset = AccentPreset.ELECTRIC_CYAN,
    val wallpaper: WallpaperOption = WallpaperOption.AURORA_MESH,
    val taskbarTransparency: Float = 0.88f,
    val windowTransparency: Float = 0.96f,
    val compactTaskbar: Boolean = false,
    val showClockSeconds: Boolean = true,
    val showDesktopIconLabels: Boolean = true,
    val fontScaleMultiplier: Float = 1.0f,
    val iconScaleMultiplier: Float = 1.0f,
    val confirmWindowClose: Boolean = false,
    val startupApp: BuiltInAppId? = null,
    val desktopShortcuts: List<DesktopShortcut> = defaultDesktopShortcuts(),
    val taskbarPinnedApps: List<BuiltInAppId> = defaultTaskbarPinnedApps(),
    val startMenuPinnedApps: List<BuiltInAppId> = defaultStartMenuPinnedApps(),
    val recentApps: List<BuiltInAppId> = listOf(
        BuiltInAppId.TEXT_EDITOR,
        BuiltInAppId.CALCULATOR,
        BuiltInAppId.SYSTEM_INFO
    )
)

fun defaultDesktopShortcuts(): List<DesktopShortcut> = listOf(
    DesktopShortcut("sc_workspace", BuiltInAppId.WORKSPACE_FOLDER, "Workspace Files", 0, 0),
    DesktopShortcut("sc_apps", BuiltInAppId.APPLICATIONS, "Applications", 0, 1),
    DesktopShortcut("sc_editor", BuiltInAppId.TEXT_EDITOR, "Text Editor", 0, 2),
    DesktopShortcut("sc_calc", BuiltInAppId.CALCULATOR, "Calculator", 0, 3),
    DesktopShortcut("sc_converter", BuiltInAppId.UNIT_CONVERTER, "Unit Converter", 1, 0),
    DesktopShortcut("sc_clock", BuiltInAppId.CLOCK, "Clock", 1, 1),
    DesktopShortcut("sc_calendar", BuiltInAppId.CALENDAR, "Calendar", 1, 2),
    DesktopShortcut("sc_files", BuiltInAppId.FILE_INFO, "File Inspector", 1, 3),
    DesktopShortcut("sc_sysinfo", BuiltInAppId.SYSTEM_INFO, "System Info", 2, 0),
    DesktopShortcut("sc_appearance", BuiltInAppId.APPEARANCE, "Appearance", 2, 1),
    DesktopShortcut("sc_settings", BuiltInAppId.SETTINGS, "Settings", 2, 2),
    DesktopShortcut("sc_about", BuiltInAppId.ABOUT, "About UIForge", 2, 3)
)

fun defaultTaskbarPinnedApps(): List<BuiltInAppId> = listOf(
    BuiltInAppId.APPLICATIONS,
    BuiltInAppId.WORKSPACE_FOLDER,
    BuiltInAppId.TEXT_EDITOR,
    BuiltInAppId.CALCULATOR,
    BuiltInAppId.SETTINGS
)

fun defaultStartMenuPinnedApps(): List<BuiltInAppId> = listOf(
    BuiltInAppId.CALCULATOR,
    BuiltInAppId.TEXT_EDITOR,
    BuiltInAppId.CALENDAR,
    BuiltInAppId.CLOCK,
    BuiltInAppId.UNIT_CONVERTER,
    BuiltInAppId.FILE_INFO,
    BuiltInAppId.SYSTEM_INFO,
    BuiltInAppId.APPEARANCE
)
