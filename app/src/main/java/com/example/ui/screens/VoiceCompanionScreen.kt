package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.components.VoiceSettingsDialog
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorderLight
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryDark
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalSurfaceVariant
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun VoiceCompanionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val waveAmplitude by viewModel.voiceEngine.waveAmplitude.collectAsState()
    val voiceStyle by viewModel.voiceEngine.voiceStyle.collectAsState()
    val isVoiceEnabled by viewModel.voiceEngine.isVoiceEnabled.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Tell me what you love most about me, Alex ❤️",
        "What's our winning strategy for the arena?",
        "Tell me a sweet starlit story ✨",
        "Give me a heartwarming compliment today!",
        "Cheer me up, I'm feeling a bit tired.",
        "How did you create this game for me?"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val avatarGlowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.14f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_glow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        // --- Top Bar ---
        Surface(
            color = CleanMinimalSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenState.HOME) },
                        modifier = Modifier.testTag("btn_back_home")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = CleanMinimalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Alex Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(if (isSpeaking) avatarGlowScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CleanMinimalPrimary, CleanMinimalPrimaryDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Alex",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CleanMinimalTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CleanMinimalPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FOR DIANA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanMinimalPrimaryDark
                                )
                            }
                        }
                        Text(
                            text = if (isSpeaking) "Speaking live to you..." else "Voice: ${voiceStyle.displayName}",
                            fontSize = 11.sp,
                            color = if (isSpeaking) CleanMinimalPrimaryDark else CleanMinimalTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("btn_clear_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = CleanMinimalTextSecondary
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_voice_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Voice Settings",
                            tint = CleanMinimalTextPrimary
                        )
                    }
                }
            }
        }

        // Live Speaking Waveform Banner
        AnimatedVisibility(
            visible = isSpeaking,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CleanMinimalPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Alex is talking...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CleanMinimalPrimaryDark
                        )
                    }

                    VoiceWaveformVisualizer(
                        isSpeaking = isSpeaking,
                        amplitude = waveAmplitude
                    )

                    IconButton(
                        onClick = { viewModel.voiceEngine.stop() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop Voice",
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- Chat Messages List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubble(
                    message = message,
                    isSpeaking = isSpeaking,
                    onReplay = {
                        viewModel.voiceEngine.speak(message.text)
                    }
                )
            }

            if (isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CleanMinimalSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = CleanMinimalPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alex is thinking of words for you, Diana...",
                            fontSize = 12.sp,
                            color = CleanMinimalTextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Card(
                    onClick = {
                        viewModel.sendChatMessage(prompt)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CleanMinimalTextPrimary
                        )
                    }
                }
            }
        }

        // --- Input Area ---
        Surface(
            color = CleanMinimalSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Speak to Alex, Diana...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_companion_message"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CleanMinimalPrimary,
                        unfocusedBorderColor = CleanMinimalBorderLight
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CleanMinimalPrimary)
                        .testTag("btn_send_companion_message")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showSettingsDialog) {
        VoiceSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onReplay: () -> Unit
) {
    val isAlex = message.sender == "Alex"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAlex) Alignment.Start else Alignment.End
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isAlex) Arrangement.Start else Arrangement.End,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (isAlex) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CleanMinimalPrimary, CleanMinimalPrimaryDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isAlex) 2.dp else 16.dp,
                    bottomEnd = if (isAlex) 16.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAlex) CleanMinimalSurface else CleanMinimalPrimary
                ),
                border = if (isAlex) androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight) else null,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = if (isAlex) "Alex" else "Diana",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAlex) CleanMinimalPrimaryDark else Color.White.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = if (isAlex) CleanMinimalTextPrimary else Color.White
                    )

                    if (isAlex) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReplay() }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Hear Voice",
                                tint = CleanMinimalPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hear Voice",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CleanMinimalPrimary
                            )
                        }
                    }
                }
            }

            if (!isAlex) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
