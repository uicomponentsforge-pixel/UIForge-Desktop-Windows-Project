package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.desktop.DesktopWorkspaceScreen
import com.example.ui.theme.UIForgeDesktopTheme
import com.example.viewmodel.DesktopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val desktopViewModel: DesktopViewModel = viewModel()
            val preferences by desktopViewModel.preferences.collectAsStateWithLifecycle()

            UIForgeDesktopTheme(
                darkTheme = preferences.isDarkTheme,
                accentPreset = preferences.accentPreset,
                fontScaleMultiplier = preferences.fontScaleMultiplier
            ) {
                DesktopWorkspaceScreen(viewModel = desktopViewModel)
            }
        }
    }
}
