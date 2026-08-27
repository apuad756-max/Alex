package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GameMode
import com.example.game.renderer.GameCanvasRenderer
import com.example.ui.GameViewModel
import com.example.ui.ScreenState

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val engine = viewModel.gameEngine
    val gameStateEvent by engine.gameStateEvent.collectAsState()

    var animTime by remember { mutableFloatStateOf(0f) }
    var showPauseMenu by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // High performance 60 FPS update loop
    LaunchedEffect(showPauseMenu) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos != 0L && !showPauseMenu) {
                    val delta = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    animTime += delta
                    engine.tick(delta)

                    if (engine.isGameOver) {
                        viewModel.onGameOver()
                    }
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    val auraColor = Color(engine.player.skin?.auraColorHex ?: 0xFF00E5FF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060910))
    ) {
        // Game Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            engine.updatePlayerTarget(offset.x, offset.y)
                            engine.triggerDash(offset.x, offset.y)
                        },
                        onDoubleTap = { offset ->
                            engine.triggerDash(offset.x, offset.y)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        engine.updatePlayerTarget(change.position.x, change.position.y)
                    }
                }
        ) {
            if (engine.screenWidth != size.width || engine.screenHeight != size.height) {
                engine.screenWidth = size.width
                engine.screenHeight = size.height
            }

            GameCanvasRenderer.render(
                drawScope = this,
                engine = engine,
                animTime = animTime
            )
        }

        // Floating HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Health Hearts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xEEFFFFFF))
                        .border(1.dp, com.example.ui.theme.CleanMinimalBorderLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    for (i in 1..engine.player.maxHp) {
                        val isFilled = i <= engine.player.hp
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "HP",
                            tint = if (isFilled) Color(0xFFB3261E) else com.example.ui.theme.CleanMinimalMuted,
                            modifier = Modifier.size(18.dp).padding(horizontal = 1.dp)
                        )
                    }
                }

                // Center Score & Combo Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xEEFFFFFF))
                        .border(1.dp, com.example.ui.theme.CleanMinimalBorderLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${engine.score}",
                        style = TextStyle(
                            color = com.example.ui.theme.CleanMinimalTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    )
                    if (engine.currentCombo > 1) {
                        Text(
                            text = "${engine.currentCombo}x COMBO",
                            color = com.example.ui.theme.CleanMinimalPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Right: Shards & Pause
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEEFFFFFF))
                            .border(1.dp, com.example.ui.theme.CleanMinimalBorderLight, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Shards",
                            tint = com.example.ui.theme.CleanMinimalPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${engine.shardsCollected}",
                            color = com.example.ui.theme.CleanMinimalTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            showPauseMenu = true
                            engine.isPaused = true
                            viewModel.soundSynth.playClick()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xEEFFFFFF))
                            .border(1.dp, com.example.ui.theme.CleanMinimalBorderLight, CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = com.example.ui.theme.CleanMinimalTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Blitz Mode Timer or Boss HP Bar
            if (engine.currentGameMode == GameMode.BLITZ_60) {
                Card(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(containerColor = Color(0xEEFFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CleanMinimalBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (engine.blitzTimeRemaining < 10f) Color(0xFFB3261E) else Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%.1fs", engine.blitzTimeRemaining),
                            color = if (engine.blitzTimeRemaining < 10f) Color(0xFFB3261E) else com.example.ui.theme.CleanMinimalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (engine.boss != null) {
                engine.boss?.let { b ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(containerColor = Color(0xEEFFFFFF)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CleanMinimalBorderLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = b.name,
                                    color = Color(0xFFB3261E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${b.hp}/${b.maxHp}",
                                    color = com.example.ui.theme.CleanMinimalTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (b.hp.toFloat() / b.maxHp).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = Color(0xFFB3261E),
                                trackColor = com.example.ui.theme.CleanMinimalSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
            val currentSpokenText by viewModel.voiceEngine.currentSpokenText.collectAsState()

            // In-Game Alex Voice Subtitles
            AnimatedVisibility(
                visible = isSpeaking && currentSpokenText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.CleanMinimalPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(com.example.ui.theme.CleanMinimalPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ALEX",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.CleanMinimalPrimaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = currentSpokenText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = com.example.ui.theme.CleanMinimalTextPrimary,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Bottom Action Controls: Dash Slash & Nova Ultimate Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Tip info
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCCFFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DRAG TO MOVE • TAP TO DASH",
                        color = com.example.ui.theme.CleanMinimalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Dash Slash Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xEEFFFFFF))
                            .border(1.5.dp, com.example.ui.theme.CleanMinimalPrimary, CircleShape)
                            .clickable {
                                engine.triggerDash()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Dash Slash",
                            tint = com.example.ui.theme.CleanMinimalPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Nova Ultimate Meter Button
                    val isNovaReady = engine.player.energy >= 100f
                    val progress = (engine.player.energy / 100f).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNovaReady) com.example.ui.theme.CleanMinimalPrimary else Color(0xEEFFFFFF)
                            )
                            .border(
                                width = if (isNovaReady) 3.dp else 1.5.dp,
                                color = if (isNovaReady) Color.White else com.example.ui.theme.CleanMinimalBorderLight,
                                shape = CircleShape
                            )
                            .clickable(enabled = isNovaReady) {
                                engine.triggerNova()
                            }
                            .testTag("nova_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isNovaReady) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(72.dp),
                                color = com.example.ui.theme.CleanMinimalPrimary,
                                strokeWidth = 3.dp,
                                trackColor = Color.Transparent
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Nova",
                                tint = if (isNovaReady) Color.White else com.example.ui.theme.CleanMinimalPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isNovaReady) "NOVA!" else "${(progress * 100).toInt()}%",
                                color = if (isNovaReady) Color.White else com.example.ui.theme.CleanMinimalTextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Pause Menu Dialog
        if (showPauseMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x991C1B1F)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CleanMinimalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CleanMinimalBorderLight),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAUSED",
                            color = com.example.ui.theme.CleanMinimalTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                showPauseMenu = false
                                engine.isPaused = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.CleanMinimalPrimary,
                                contentColor = com.example.ui.theme.CleanMinimalOnPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("RESUME", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                showPauseMenu = false
                                viewModel.startActiveGame(screenWidthPx, screenHeightPx)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("RESTART RUN", color = com.example.ui.theme.CleanMinimalTextPrimary, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                showPauseMenu = false
                                viewModel.navigateTo(ScreenState.HOME)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("RETURN TO HOME", color = com.example.ui.theme.CleanMinimalTextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Game Over / Victory Dialog
        if (engine.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x991C1B1F)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CleanMinimalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CleanMinimalBorderLight),
                    modifier = Modifier.fillMaxWidth(0.88f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (engine.isVictory) "MISSION ACCOMPLISHED!" else "RUN OVER",
                            color = if (engine.isVictory) com.example.ui.theme.CleanMinimalPrimary else Color(0xFFB3261E),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${engine.score}",
                            color = com.example.ui.theme.CleanMinimalTextPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "FINAL SCORE",
                            color = com.example.ui.theme.CleanMinimalMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(com.example.ui.theme.CleanMinimalSurfaceVariant)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${engine.shardsCollected}",
                                    color = com.example.ui.theme.CleanMinimalPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                                Text("SHARDS", color = com.example.ui.theme.CleanMinimalMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${engine.enemiesDefeated}",
                                    color = Color(0xFFB3261E),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                                Text("DEFEATED", color = com.example.ui.theme.CleanMinimalMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${engine.maxComboInRun}x",
                                    color = Color(0xFF7D5260),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                                Text("MAX COMBO", color = com.example.ui.theme.CleanMinimalMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.startActiveGame(screenWidthPx, screenHeightPx)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.CleanMinimalPrimary,
                                contentColor = com.example.ui.theme.CleanMinimalOnPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("retry_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PLAY AGAIN", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.startCustomizing(engine.player.skin)
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("customizer_shortcut_button")
                        ) {
                            Text("CUSTOMIZE SKINS", color = com.example.ui.theme.CleanMinimalPrimaryDark, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.navigateTo(ScreenState.HOME)
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gameover_home_button")
                        ) {
                            Text("RETURN TO HOME", color = com.example.ui.theme.CleanMinimalTextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
