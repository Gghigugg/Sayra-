package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.sayra.ui.screens.HistoryScreen
import com.example.sayra.ui.screens.MemoryScreen
import com.example.sayra.ui.screens.OnboardingScreen
import com.example.sayra.ui.screens.SayraFullScreenCharacterScreen
import com.example.sayra.ui.screens.SayraHomeScreen
import com.example.sayra.ui.screens.SettingsScreen
import com.example.sayra.ui.viewmodel.SayraViewModel
import com.example.ui.theme.SayraTheme

enum class SayraScreen {
    HOME,
    ONBOARDING,
    SETTINGS,
    HISTORY,
    MEMORY,
    FULLSCREEN_CHARACTER
}

class MainActivity : ComponentActivity() {

    private val viewModel: SayraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isOnboardingDone by viewModel.isOnboardingDone.collectAsState()

            var currentScreen by remember(isOnboardingDone) {
                mutableStateOf(if (isOnboardingDone) SayraScreen.HOME else SayraScreen.ONBOARDING)
            }

            SayraTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            SayraScreen.ONBOARDING -> {
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    onComplete = {
                                        currentScreen = SayraScreen.HOME
                                    }
                                )
                            }
                            SayraScreen.HOME -> {
                                SayraHomeScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = { currentScreen = SayraScreen.SETTINGS },
                                    onOpenHistory = { currentScreen = SayraScreen.HISTORY },
                                    onOpenMemory = { currentScreen = SayraScreen.MEMORY },
                                    onOpenFullScreen = { currentScreen = SayraScreen.FULLSCREEN_CHARACTER }
                                )
                            }
                            SayraScreen.FULLSCREEN_CHARACTER -> {
                                SayraFullScreenCharacterScreen(
                                    viewModel = viewModel,
                                    onClose = { currentScreen = SayraScreen.HOME }
                                )
                            }
                            SayraScreen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = SayraScreen.HOME },
                                    onNavigateToMemory = { currentScreen = SayraScreen.MEMORY }
                                )
                            }
                            SayraScreen.HISTORY -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = SayraScreen.HOME }
                                )
                            }
                            SayraScreen.MEMORY -> {
                                MemoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = SayraScreen.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
