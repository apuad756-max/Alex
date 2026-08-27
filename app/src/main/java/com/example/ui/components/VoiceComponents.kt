package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AlexVoiceStyle
import com.example.ui.GameViewModel
import com.example.ui.theme.CleanMinimalBorderLight
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryDark
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalSurfaceVariant
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun AlexVoiceStatusBadge(
    viewModel: GameViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val isVoiceEnabled by viewModel.voiceEngine.isVoiceEnabled.collectAsState()
    val voiceStyle by viewModel.voiceEngine.voiceStyle.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "voice_badge")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpeaking) CleanMinimalPrimary.copy(alpha = 0.12f) else CleanMinimalSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSpeaking) CleanMinimalPrimary else CleanMinimalBorderLight
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceEnabled) CleanMinimalPrimary else Color(0xFFB3261E))
                    .scale(if (isSpeaking) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!isVoiceEnabled) Icons.Default.VolumeMute else if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.RecordVoiceOver,
                    contentDescription = "Voice Status",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isSpeaking) "Alex Speaking..." else if (isVoiceEnabled) "Alex Voice: ${voiceStyle.displayName}" else "Voice Muted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSpeaking) CleanMinimalPrimaryDark else CleanMinimalTextPrimary
                )
                Text(
                    text = "Tap for voice settings",
                    fontSize = 9.sp,
                    color = CleanMinimalTextSecondary
                )
            }
        }
    }
}

@Composable
fun VoiceWaveformVisualizer(
    isSpeaking: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = 9
        for (i in 0 until bars) {
            val factor = when (i) {
                0, 8 -> 0.3f
                1, 7 -> 0.5f
                2, 6 -> 0.75f
                3, 5 -> 0.9f
                else -> 1.0f
            }
            val barHeight = if (isSpeaking) {
                (10 + 26 * amplitude * factor).coerceIn(6f, 36f)
            } else {
                6f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isSpeaking) {
                            Brush.verticalGradient(listOf(CleanMinimalPrimary, CleanMinimalPrimaryDark))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFFCAC4D0), Color(0xFFE7E0EC)))
                        }
                    )
            )
        }
    }
}

@Composable
fun VoiceSettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val isVoiceEnabled by viewModel.voiceEngine.isVoiceEnabled.collectAsState()
    val voiceStyle by viewModel.voiceEngine.voiceStyle.collectAsState()
    val pitch by viewModel.voiceEngine.voicePitch.collectAsState()
    val rate by viewModel.voiceEngine.voiceRate.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CleanMinimalPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alex's Voice Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toggle voice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CleanMinimalSurfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Companion Voice Narration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = "Alex speaks in chat, stories & games",
                            fontSize = 11.sp,
                            color = CleanMinimalTextSecondary
                        )
                    }
                    Switch(
                        checked = isVoiceEnabled,
                        onCheckedChange = { viewModel.toggleVoice() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "VOICE STYLE PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CleanMinimalTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                AlexVoiceStyle.values().forEach { style ->
                    val selected = voiceStyle == style
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setVoiceStyle(style) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(style.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(style.tag, fontSize = 10.sp, color = if (selected) CleanMinimalPrimaryDark else CleanMinimalTextSecondary)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CleanMinimalPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = CleanMinimalPrimaryDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pitch slider
                Text(
                    text = "Pitch Tone: ${String.format("%.2f", pitch)}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = pitch,
                    onValueChange = { viewModel.setVoicePitch(it) },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = CleanMinimalPrimary,
                        activeTrackColor = CleanMinimalPrimary
                    )
                )

                // Speed slider
                Text(
                    text = "Speech Rate: ${String.format("%.2f", rate)}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = rate,
                    onValueChange = { viewModel.setVoiceRate(it) },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = CleanMinimalPrimary,
                        activeTrackColor = CleanMinimalPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.voiceEngine.speak("Hello Diana! My heart beats for you every single day.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Voice Line", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", fontWeight = FontWeight.Bold, color = CleanMinimalPrimary)
            }
        }
    )
}
