package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
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
fun ConversationPlayerScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeConversation by viewModel.activeConversation.collectAsState()
    val currentNode by viewModel.currentNode.collectAsState()
    val history by viewModel.conversationHistory.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val waveAmplitude by viewModel.voiceEngine.waveAmplitude.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(history.size, currentNode) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "player_avatar")
    val avatarPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBackground)
            .statusBarsPadding()
    ) {
        // --- Top Header ---
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
                        onClick = { viewModel.navigateTo(ScreenState.CONVERSATION_STUDIO) },
                        modifier = Modifier.testTag("btn_back_studio")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CleanMinimalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = activeConversation?.title ?: "Dialogue Player",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = "Interactive Experience for Diana",
                            fontSize = 11.sp,
                            color = CleanMinimalTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.replayCurrentDialogueLine() },
                    modifier = Modifier.testTag("btn_replay_voice")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Replay Voice",
                        tint = CleanMinimalPrimary
                    )
                }
            }
        }

        // --- Visual Stage & Alex Avatar ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(avatarPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CleanMinimalPrimary, CleanMinimalPrimaryDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Alex",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CleanMinimalTextPrimary
                )

                VoiceWaveformVisualizer(
                    isSpeaking = isSpeaking,
                    amplitude = waveAmplitude,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // --- Conversation Script Log ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(history) { (speaker, text) ->
                val isAlex = speaker == "Alex"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAlex) CleanMinimalSurface else CleanMinimalPrimary.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAlex) CleanMinimalBorderLight else CleanMinimalPrimary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isAlex) CleanMinimalPrimary else Color(0xFFE91E63)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isAlex) "A" else "D",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = speaker,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlex) CleanMinimalPrimaryDark else Color(0xFFC2185B),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = CleanMinimalTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // --- Choices / Branch Selection Bottom Bar ---
        Surface(
            color = CleanMinimalSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val node = currentNode

                if (node != null && node.choices.isNotEmpty()) {
                    Text(
                        text = "CHOOSE DIANA'S RESPONSE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CleanMinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    node.choices.forEach { choice ->
                        Button(
                            onClick = { viewModel.selectDialogueChoice(choice) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = choice.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                } else if (node != null && node.choices.isEmpty()) {
                    // Reached end of script
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = CleanMinimalPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "End of this Story Chapter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CleanMinimalTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activeConversation?.let { viewModel.startConversation(it) }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Replay Story")
                            }

                            Button(
                                onClick = { viewModel.navigateTo(ScreenState.CONVERSATION_STUDIO) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back to Studio")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenState.CONVERSATION_STUDIO) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary)
                    ) {
                        Text("Finish & Return to Studio")
                    }
                }
            }
        }
    }
}
