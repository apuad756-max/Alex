package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val author: String = "Alex",
    val category: String = "Romantic",
    val dialogueJson: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val isPreset: Boolean = false,
    val timesPlayed: Int = 0
)

@JsonClass(generateAdapter = true)
data class DialogueScript(
    val title: String,
    val initialNodeId: String,
    val nodes: List<DialogueNode>
)

@JsonClass(generateAdapter = true)
data class DialogueNode(
    val id: String,
    val speaker: String = "Alex", // "Alex", "Diana", "Narrator"
    val text: String,
    val emotion: String = "loving", // "loving", "excited", "heroic", "gentle", "playful"
    val nextNodeId: String? = null,
    val choices: List<DialogueChoice> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DialogueChoice(
    val text: String,
    val targetNodeId: String,
    val emotionTrigger: String = "loving"
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "Alex" or "Diana"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromAi: Boolean = false
)

enum class AlexVoiceStyle(val displayName: String, val pitch: Float, val speed: Float, val tag: String) {
    WARM_ROMANTIC("Warm & Devoted", 0.92f, 0.95f, "Romantic voice full of love"),
    HEROIC_COMPANION("Heroic Partner", 1.05f, 1.05f, "Energetic & battle-ready"),
    GENTLE_WHISPER("Gentle Whisper", 0.88f, 0.88f, "Soft, tender, and caring"),
    PLAYFUL_WITTY("Playful & Witty", 1.15f, 1.1f, "Fun, lighthearted, and bright")
}
