package com.costular.atomtasks.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.costular.atomtasks.data.settings.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var uiState: AppState by mutableStateOf(AppState.Empty)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state
                    .collect {
                        uiState = it
                    }
            }
        }

        enableEdgeToEdge()

        setContent {

            val isDarkTheme = when (uiState.theme) {
                is Theme.Light -> false
                is Theme.Dark -> true
                is Theme.System -> isSystemInDarkTheme()
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDarkTheme }),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = LightScrim,
                        darkScrim = DarkScrim,
                        detectDarkMode = { isDarkTheme })
                )

                onDispose { }
            }

            uiState.defaultTab?.let { defaultTab ->
                App(isDarkTheme = isDarkTheme, defaultTab = defaultTab)
            }
        }
    }
}

private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
