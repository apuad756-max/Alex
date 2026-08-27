package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GameMode
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.components.AlexVoiceStatusBadge
import com.example.ui.components.SkinPreviewBox
import com.example.ui.components.VoiceSettingsDialog
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorderLight
import com.example.ui.theme.CleanMinimalContainer
import com.example.ui.theme.CleanMinimalMuted
import com.example.ui.theme.CleanMinimalOnPrimary
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryDark
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalSurfaceVariant
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val equippedSkin by viewModel.equippedSkin.collectAsState()
    val gameStats by viewModel.gameStats.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val isVoiceEnabled by viewModel.voiceEngine.isVoiceEnabled.collectAsState()

    var showVoiceSettings by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.12f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Badge & Sound/Voice toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CleanMinimalSurfaceVariant)
                    .border(1.dp, CleanMinimalBorderLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALEX • DIANA EDITION",
                    color = CleanMinimalTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CleanMinimalSurfaceVariant)
                        .border(1.dp, CleanMinimalBorderLight, CircleShape)
                        .testTag("sound_toggle")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Sound",
                        tint = if (isSoundEnabled) CleanMinimalPrimary else CleanMinimalMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleHaptics() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CleanMinimalSurfaceVariant)
                        .border(1.dp, CleanMinimalBorderLight, CircleShape)
                        .testTag("haptic_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Toggle Haptics",
                        tint = if (isHapticEnabled) CleanMinimalPrimary else CleanMinimalMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { showVoiceSettings = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CleanMinimalSurfaceVariant)
                        .border(1.dp, CleanMinimalBorderLight, CircleShape)
                        .testTag("voice_settings_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Voice Settings",
                        tint = CleanMinimalPrimaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Text(
            text = "ALEX FOR DIANA",
            style = TextStyle(
                color = CleanMinimalTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.5.sp
            )
        )
        Text(
            text = "ACTION ARENA & VOICE COMPANION",
            color = CleanMinimalPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Alex Voice Companion & Dialogue Studio Card ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CleanMinimalPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(avatarScale)
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Alex Companion",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = CleanMinimalTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CleanMinimalPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VOICE ACTIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CleanMinimalPrimaryDark
                                    )
                                }
                            }
                            Text(
                                text = "Ready to talk, guide & cheer for you!",
                                fontSize = 11.sp,
                                color = CleanMinimalTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.voiceEngine.speak(
                                "Hello Diana! I'm right here by your side. Let's create something wonderful today!"
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CleanMinimalPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Speak Greeting",
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenState.VOICE_COMPANION) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_talk_alex"),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Talk to Alex", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(ScreenState.CONVERSATION_STUDIO) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_dialogue_studio"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalPrimary)
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp), tint = CleanMinimalPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Story Studio", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CleanMinimalPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Character Showcase & Quick Customizer Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = CleanMinimalBorderLight,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkinPreviewBox(
                    skin = equippedSkin,
                    size = 120.dp,
                    radiusRatio = 0.32f
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = equippedSkin?.name ?: "Cyber Runner",
                        color = CleanMinimalTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (equippedSkin?.isCustomGallery == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CleanMinimalContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GALLERY",
                                color = CleanMinimalPrimaryDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(
                    text = "${equippedSkin?.bodyShape?.displayName ?: "Aero Orb"} • ${equippedSkin?.trailType?.displayName ?: "Cyber Sparks"}",
                    color = CleanMinimalTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.startCustomizing(equippedSkin) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CleanMinimalSurfaceVariant,
                        contentColor = CleanMinimalPrimaryDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CleanMinimalBorderLight, RoundedCornerShape(16.dp))
                        .testTag("customize_character_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize",
                        tint = CleanMinimalPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CUSTOMIZE SKINS & GALLERY",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Mode Selector
        Text(
            text = "SELECT ARENA MISSION",
            color = CleanMinimalTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        GameMode.values().forEach { mode ->
            val isSelected = selectedMode == mode
            val modeHigh = when (mode) {
                GameMode.ENDLESS -> gameStats?.highScoreEndless ?: 0
                GameMode.BLITZ_60 -> gameStats?.highScoreBlitz ?: 0
                GameMode.BOSS_TRIAL -> gameStats?.highScoreBossTrial ?: 0
            }

            Card(
                onClick = { viewModel.selectMode(mode) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CleanMinimalContainer else CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CleanMinimalPrimary else CleanMinimalBorderLight,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("mode_${mode.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.7f) else CleanMinimalSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                GameMode.ENDLESS -> Icons.Default.Bolt
                                GameMode.BLITZ_60 -> Icons.Default.Timer
                                GameMode.BOSS_TRIAL -> Icons.Default.EmojiEvents
                            },
                            contentDescription = null,
                            tint = when (mode) {
                                GameMode.ENDLESS -> CleanMinimalPrimary
                                GameMode.BLITZ_60 -> Color(0xFFE65100)
                                GameMode.BOSS_TRIAL -> Color(0xFFB3261E)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.displayName,
                            color = CleanMinimalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Text(
                            text = mode.description,
                            color = CleanMinimalTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BEST",
                            color = CleanMinimalMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$modeHigh",
                            color = CleanMinimalPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Play Button
        Button(
            onClick = { viewModel.startActiveGame(screenWidthPx, screenHeightPx) },
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanMinimalPrimary,
                contentColor = CleanMinimalOnPrimary
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_game_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START ACTION ARENA",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CleanMinimalBorderLight, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameStats?.totalShardsCollected ?: 0}",
                        color = CleanMinimalPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SHARDS",
                        color = CleanMinimalMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameStats?.totalEnemiesDefeated ?: 0}",
                        color = Color(0xFFB3261E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DEFEATED",
                        color = CleanMinimalMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameStats?.highestCombo ?: 0}x",
                        color = Color(0xFF7D5260),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MAX COMBO",
                        color = CleanMinimalMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${gameStats?.totalGamesPlayed ?: 0}",
                        color = CleanMinimalPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RUNS",
                        color = CleanMinimalMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showVoiceSettings) {
        VoiceSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceSettings = false }
        )
    }
}
