package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ConversationEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.components.AlexVoiceStatusBadge
import com.example.ui.components.VoiceSettingsDialog
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorderLight
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryDark
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalSurfaceVariant
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun ConversationStudioScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.allConversations.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var showVoiceSettings by remember { mutableStateOf(false) }

    val categories = listOf("All", "Romantic", "Battle Briefing", "Custom")

    val filteredConversations = conversations.filter {
        if (selectedCategory == "All") true
        else if (selectedCategory == "Custom") !it.isPreset
        else it.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startNewConversationCreation() },
                containerColor = CleanMinimalPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_conversation")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Experience")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Experience", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(CleanMinimalBackground)
                .padding(paddingValues)
                .statusBarsPadding()
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
                                contentDescription = "Back",
                                tint = CleanMinimalTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Dialogue Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = CleanMinimalTextPrimary
                            )
                            Text(
                                text = "Interactive Voice Experiences",
                                fontSize = 11.sp,
                                color = CleanMinimalTextSecondary
                            )
                        }
                    }

                    AlexVoiceStatusBadge(
                        viewModel = viewModel,
                        onClick = { showVoiceSettings = true }
                    )
                }
            }

            // Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CleanMinimalPrimary, CleanMinimalPrimaryDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Alex & Diana Conversations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CleanMinimalTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Play voiced branching dialogues or write your own conversational stories for Diana with custom choices and voice acting!",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = CleanMinimalTextSecondary
                        )
                    }
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CleanMinimalPrimary.copy(alpha = 0.18f),
                            selectedLabelColor = CleanMinimalPrimaryDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conversation List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredConversations.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CleanMinimalSurfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = CleanMinimalTextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No custom experiences yet!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CleanMinimalTextPrimary
                                )
                                Text(
                                    text = "Tap the 'Create Experience' button below to create your own voiced dialogue story.",
                                    fontSize = 12.sp,
                                    color = CleanMinimalTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(filteredConversations) { conv ->
                    ConversationCard(
                        conversation = conv,
                        onPlay = { viewModel.startConversation(conv) },
                        onDelete = { viewModel.deleteConversation(conv.id) }
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

@Composable
fun ConversationCard(
    conversation: ConversationEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CleanMinimalPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = conversation.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinimalPrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Played ${conversation.timesPlayed}x",
                        fontSize = 10.sp,
                        color = CleanMinimalTextSecondary
                    )
                    if (!conversation.isPreset) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFB3261E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = conversation.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CleanMinimalTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = CleanMinimalTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CleanMinimalPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "By ${conversation.author}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CleanMinimalTextSecondary
                    )
                }

                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Experience", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
