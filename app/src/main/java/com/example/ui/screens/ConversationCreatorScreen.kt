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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DialogueNode
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorderLight
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryDark
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalSurfaceVariant
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun ConversationCreatorScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val title by viewModel.creatorTitle.collectAsState()
    val category by viewModel.creatorCategory.collectAsState()
    val description by viewModel.creatorDescription.collectAsState()
    val nodes by viewModel.creatorNodes.collectAsState()

    val categories = listOf("Romantic", "Battle Briefing", "Adventure", "Daily Secrets", "Custom")

    var newChoiceText by remember { mutableStateOf("") }
    var selectedNodeIndexForChoice by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBackground)
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
                        onClick = { viewModel.navigateTo(ScreenState.CONVERSATION_STUDIO) },
                        modifier = Modifier.testTag("btn_cancel_creator")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                            tint = CleanMinimalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Experience Creator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = "Build Voice Dialogue for Diana",
                            fontSize = 11.sp,
                            color = CleanMinimalTextSecondary
                        )
                    }
                }

                Button(
                    onClick = { viewModel.saveCreatorConversation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_save_experience")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // General Info Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EXPERIENCE DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CleanMinimalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { viewModel.updateCreatorTitle(it) },
                            label = { Text("Experience Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { viewModel.updateCreatorDescription(it) },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CATEGORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CleanMinimalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.take(3).forEach { cat ->
                                val sel = category == cat
                                FilterChip(
                                    selected = sel,
                                    onClick = { viewModel.updateCreatorCategory(cat) },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CleanMinimalPrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = CleanMinimalPrimaryDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Dialogue Nodes Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIALOGUE SCRIPT NODES (${nodes.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CleanMinimalTextSecondary
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.addCreatorNode(
                                speaker = "Alex",
                                text = "Diana, you are wonderful!",
                                emotion = "loving"
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Node", fontSize = 12.sp)
                    }
                }
            }

            // List of nodes
            itemsIndexed(nodes) { index, node ->
                CreatorNodeCard(
                    index = index,
                    node = node,
                    allNodes = nodes,
                    onUpdate = { updated -> viewModel.updateCreatorNode(index, updated) },
                    onDelete = { viewModel.deleteCreatorNode(index) },
                    onTestVoice = { viewModel.testPlayNodeVoice(node.text) },
                    onAddChoice = { text, targetId ->
                        viewModel.addChoiceToNode(index, text, targetId)
                    },
                    onRemoveChoice = { choiceIdx ->
                        viewModel.removeChoiceFromNode(index, choiceIdx)
                    }
                )
            }
        }
    }
}

@Composable
fun CreatorNodeCard(
    index: Int,
    node: DialogueNode,
    allNodes: List<DialogueNode>,
    onUpdate: (DialogueNode) -> Unit,
    onDelete: () -> Unit,
    onTestVoice: () -> Unit,
    onAddChoice: (String, String) -> Unit,
    onRemoveChoice: (Int) -> Unit
) {
    var choiceInput by remember { mutableStateOf("") }
    var showAddChoice by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleanMinimalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CleanMinimalPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Node ID: ${node.id}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanMinimalTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTestVoice,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Test Voice",
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (allNodes.size > 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete Node",
                                tint = Color(0xFFB3261E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Speaker toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Alex", "Diana", "Narrator").forEach { spk ->
                    val isSel = node.speaker == spk
                    FilterChip(
                        selected = isSel,
                        onClick = { onUpdate(node.copy(speaker = spk)) },
                        label = { Text(spk, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CleanMinimalPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = CleanMinimalPrimaryDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dialogue Line Text
            OutlinedTextField(
                value = node.text,
                onValueChange = { onUpdate(node.copy(text = it)) },
                label = { Text("Spoken Dialogue Line") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Choices list
            Text(
                text = "BRANCH CHOICES FOR DIANA (${node.choices.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CleanMinimalTextSecondary
            )

            node.choices.forEachIndexed { cIdx, choice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CleanMinimalSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(choice.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("➔ leads to ${choice.targetNodeId}", fontSize = 10.sp, color = CleanMinimalPrimaryDark)
                    }
                    IconButton(
                        onClick = { onRemoveChoice(cIdx) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                    }
                }
            }

            if (showAddChoice) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = choiceInput,
                        onValueChange = { choiceInput = it },
                        placeholder = { Text("Choice text...", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (choiceInput.isNotBlank()) {
                                val nextNodeId = "node_${index + 2}"
                                onAddChoice(choiceInput, nextNodeId)
                                choiceInput = ""
                                showAddChoice = false
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary)
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { showAddChoice = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Branch Choice", fontSize = 11.sp, color = CleanMinimalPrimary)
                }
            }
        }
    }
}
