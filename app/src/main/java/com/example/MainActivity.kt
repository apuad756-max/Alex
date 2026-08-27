package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.ConversationCreatorScreen
import com.example.ui.screens.ConversationPlayerScreen
import com.example.ui.screens.ConversationStudioScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SkinCustomizerScreen
import com.example.ui.screens.VoiceCompanionScreen
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CleanMinimalBackground
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            ScreenState.HOME -> HomeScreen(viewModel = viewModel)
                            ScreenState.GAME -> GameScreen(viewModel = viewModel)
                            ScreenState.CUSTOMIZER -> SkinCustomizerScreen(viewModel = viewModel)
                            ScreenState.VOICE_COMPANION -> VoiceCompanionScreen(viewModel = viewModel)
                            ScreenState.CONVERSATION_STUDIO -> ConversationStudioScreen(viewModel = viewModel)
                            ScreenState.CONVERSATION_PLAYER -> ConversationPlayerScreen(viewModel = viewModel)
                            ScreenState.CONVERSATION_CREATOR -> ConversationCreatorScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
