package com.uiforge.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.BufferedInputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private data class DesktopApp(val name: String, val symbol: String, val description: String)
private val apps = listOf(
    DesktopApp("Calculator", "＋", "Quick calculations"),
    DesktopApp("Text Editor", "▤", "Write and edit notes"),
    DesktopApp("System Info", "ⓘ", "View device information"),
    DesktopApp("Clock", "◷", "Time and stopwatch"),
    DesktopApp("Calendar", "▦", "Calendar and notes"),
    DesktopApp("File Explorer", "▣", "Browse workspace files"),
    DesktopApp("Appearance", "◐", "Personalize your desktop"),
    DesktopApp("Settings", "⚙", "Configure UIForge Desktop"),
    DesktopApp("About UIForge", "◆", "About this application")
)

fun main() = application {
    val state = rememberWindowState(width = 1280.dp, height = 800.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "UIForge Desktop",
        state = state
    ) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            UIForgeDesktop()
        }
    }
}

@Composable
private fun UIForgeDesktop() {
    var startOpen by remember { mutableStateOf(false) }
    var selectedWallpaper by remember { mutableStateOf("aurora") }
    var activeApp by remember { mutableStateOf<DesktopApp?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }

    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Wallpaper(selectedWallpaper)
        Row(Modifier.fillMaxSize().padding(22.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                DesktopShortcut("This PC", "▣") { activeApp = DesktopApp("This PC", "▣", "Your UIForge workspace") }
                DesktopShortcut("Applications", "▦") { startOpen = true }
                DesktopShortcut("About", "◆") { showAbout = true }
            }
        }

        activeApp?.let { app ->
            AppWindow(app = app, onClose = { activeApp = null })
        }

        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                title = { Text("UIForge Desktop") },
                text = { Text("Windows desktop edition • Version 1.0.0\nA desktop-style workspace by UIForge." ) },
                confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close") } }
            )
        }

        if (startOpen) {
            StartMenu(
                onOpen = { app -> activeApp = app; startOpen = false },
                onClose = { startOpen = false },
                onWallpaper = { selectedWallpaper = it; startOpen = false }
            )
        }

        Taskbar(clock = clock, startOpen = startOpen, onStart = { startOpen = !startOpen }, onAbout = { showAbout = true })
    }
}

@Composable
private fun Wallpaper(name: String) {
    val bitmap = remember(name) { loadWallpaper(name) }
    if (bitmap != null) {
        Image(bitmap, contentDescription = "Desktop wallpaper", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    } else {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF14213D), Color(0xFF345995), Color(0xFF5C4D7D)))))
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.10f)))
}

private fun loadWallpaper(name: String): ImageBitmap? = try {
    val stream = object {}.javaClass.getResourceAsStream("/wallpapers/$name.jpg") ?: return null
    BufferedInputStream(stream).use { loadImageBitmap(it) }
} catch (_: Exception) { null }

@Composable
private fun DesktopShortcut(title: String, icon: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(92.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .18f)).border(1.dp, Color.White.copy(alpha = .22f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text(icon, color = Color.White, fontSize = 26.sp)
        }
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BoxScope.StartMenu(onOpen: (DesktopApp) -> Unit, onClose: () -> Unit, onWallpaper: (String) -> Unit) {
    Surface(
        modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 76.dp).width(390.dp).heightIn(max = 610.dp),
        shape = RoundedCornerShape(24.dp), color = Color(0xF21B2231), tonalElevation = 16.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("UIForge", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White) }
            }
            Text("Pinned applications", color = Color(0xFFB9C4D5), modifier = Modifier.padding(top = 12.dp, bottom = 10.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(270.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apps) { app ->
                    Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable { onOpen(app) }.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(app.symbol, fontSize = 26.sp, color = Color(0xFFB8D8FF))
                        Text(app.name, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = .12f), modifier = Modifier.padding(vertical = 12.dp))
            Text("Personalize wallpaper", color = Color(0xFFB9C4D5), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                listOf("aurora" to "Aurora", "daylight" to "Daylight", "obsidian" to "Obsidian").forEach { (id, label) ->
                    OutlinedButton(onClick = { onWallpaper(id) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Text(label, fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.Taskbar(clock: String, startOpen: Boolean, onStart: () -> Unit, onAbout: () -> Unit) {
    Row(
        Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(62.dp).padding(horizontal = 14.dp, vertical = 7.dp)
            .clip(RoundedCornerShape(18.dp)).background(Color(0xE91A2030)).border(1.dp, Color.White.copy(alpha = .14f), RoundedCornerShape(18.dp)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onStart, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (startOpen) Color(0xFF4263A5) else Color.White.copy(alpha = .08f))) {
            Icon(Icons.Rounded.Widgets, contentDescription = "Start menu", tint = Color.White)
        }
        Spacer(Modifier.width(10.dp))
        Text("UIForge Desktop", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onAbout) { Icon(Icons.Rounded.Info, contentDescription = "About", tint = Color.White) }
        Column(horizontalAlignment = Alignment.End) {
            Text(clock, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(java.time.LocalDate.now().toString(), color = Color(0xFFB9C4D5), fontSize = 10.sp)
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun BoxScope.AppWindow(app: DesktopApp, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.align(Alignment.Center).width(560.dp).height(390.dp),
        shape = RoundedCornerShape(18.dp), color = Color(0xF21C2433), tonalElevation = 18.dp, shadowElevation = 22.dp
    ) {
        Column {
            Row(Modifier.fillMaxWidth().height(48.dp).background(Color.White.copy(alpha = .06f)).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(app.symbol, color = Color(0xFFB8D8FF), fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(app.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "Close window", tint = Color.White) }
            }
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(app.symbol, fontSize = 48.sp, color = Color(0xFFB8D8FF))
                Spacer(Modifier.height(12.dp))
                Text(app.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(app.description, color = Color(0xFFB9C4D5), modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(18.dp))
                Text("This Windows edition is being built in stages.", color = Color(0xFFB9C4D5), fontSize = 12.sp)
            }
        }
    }
}
