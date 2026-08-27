package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAlexCompanion
import com.example.data.models.AccessoryType
import com.example.data.models.AlexVoiceStyle
import com.example.data.models.BodyShape
import com.example.data.models.ChatMessage
import com.example.data.models.ConversationEntity
import com.example.data.models.DialogueChoice
import com.example.data.models.DialogueNode
import com.example.data.models.DialogueScript
import com.example.data.models.GameMode
import com.example.data.models.GameStatsEntity
import com.example.data.models.SkinEntity
import com.example.data.models.TrailType
import com.example.data.models.WeaponFx
import com.example.data.repository.GameRepository
import com.example.game.audio.SoundSynth
import com.example.game.engine.GameEngine
import com.example.voice.AlexVoiceEngine
import com.example.voice.GameVoiceMoment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class ScreenState {
    HOME,
    GAME,
    CUSTOMIZER,
    VOICE_COMPANION,
    CONVERSATION_STUDIO,
    CONVERSATION_PLAYER,
    CONVERSATION_CREATOR
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    val soundSynth = SoundSynth(application)
    val voiceEngine = AlexVoiceEngine(application)
    val gameEngine = GameEngine(application, soundSynth, voiceEngine)
    val geminiAi = GeminiAlexCompanion()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val dialogueScriptAdapter = moshi.adapter(DialogueScript::class.java)

    val allSkins: StateFlow<List<SkinEntity>> = repository.allSkins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equippedSkin: StateFlow<SkinEntity?> = repository.equippedSkin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val gameStats: StateFlow<GameStatsEntity?> = repository.gameStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allConversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(ScreenState.HOME)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _selectedMode = MutableStateFlow(GameMode.ENDLESS)
    val selectedMode: StateFlow<GameMode> = _selectedMode.asStateFlow()

    // Skin Customizer draft
    private val _draftSkin = MutableStateFlow<SkinEntity>(
        SkinEntity(
            id = "custom_draft",
            name = "My Custom Skin",
            isCustomGallery = false,
            bodyShape = BodyShape.ORB,
            auraColorHex = 0xFF00E5FF,
            secondaryColorHex = 0xFF7C4DFF,
            trailType = TrailType.CYBER_SPARKS,
            accessory = AccessoryType.CYBER_VISOR,
            weaponFx = WeaponFx.BLADE_SLASH
        )
    )
    val draftSkin: StateFlow<SkinEntity> = _draftSkin.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    // Live Voice Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "Alex",
                text = "Hello Diana! I am so happy you are here. Every pixel in this game and every line I speak is for you. How can I make your day brighter?",
                isFromAi = true
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Active Interactive Conversation Player State
    private val _activeConversation = MutableStateFlow<ConversationEntity?>(null)
    val activeConversation: StateFlow<ConversationEntity?> = _activeConversation.asStateFlow()

    private val _activeScript = MutableStateFlow<DialogueScript?>(null)
    val activeScript: StateFlow<DialogueScript?> = _activeScript.asStateFlow()

    private val _currentNode = MutableStateFlow<DialogueNode?>(null)
    val currentNode: StateFlow<DialogueNode?> = _currentNode.asStateFlow()

    private val _conversationHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val conversationHistory: StateFlow<List<Pair<String, String>>> = _conversationHistory.asStateFlow()

    // Conversation Creator Draft State
    private val _creatorTitle = MutableStateFlow("Our Special Story")
    val creatorTitle: StateFlow<String> = _creatorTitle.asStateFlow()

    private val _creatorCategory = MutableStateFlow("Romantic")
    val creatorCategory: StateFlow<String> = _creatorCategory.asStateFlow()

    private val _creatorDescription = MutableStateFlow("A custom conversational journey created for Diana.")
    val creatorDescription: StateFlow<String> = _creatorDescription.asStateFlow()

    private val _creatorNodes = MutableStateFlow<List<DialogueNode>>(
        listOf(
            DialogueNode(
                id = "node_1",
                speaker = "Alex",
                text = "Diana, you bring so much warmth to my heart every day.",
                emotion = "loving",
                choices = listOf(
                    DialogueChoice("Tell me more, Alex!", "node_2", "loving"),
                    DialogueChoice("Let's go on an epic adventure!", "node_3", "excited")
                )
            ),
            DialogueNode(
                id = "node_2",
                speaker = "Alex",
                text = "Whenever I look at you, I see everything good and bright in the universe.",
                emotion = "loving",
                choices = emptyList()
            ),
            DialogueNode(
                id = "node_3",
                speaker = "Alex",
                text = "Take my hand! We will conquer every realm together.",
                emotion = "heroic",
                choices = emptyList()
            )
        )
    )
    val creatorNodes: StateFlow<List<DialogueNode>> = _creatorNodes.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }

    fun navigateTo(screen: ScreenState) {
        soundSynth.playClick()
        voiceEngine.stop()
        _currentScreen.value = screen
        if (screen == ScreenState.VOICE_COMPANION) {
            val lastAlexMsg = chatMessages.value.lastOrNull { it.sender == "Alex" }
            if (lastAlexMsg != null && voiceEngine.isVoiceEnabled.value) {
                voiceEngine.speak(lastAlexMsg.text)
            }
        }
    }

    fun selectMode(mode: GameMode) {
        soundSynth.playClick()
        _selectedMode.value = mode
    }

    fun toggleSound() {
        _isSoundEnabled.value = soundSynth.toggleSound()
    }

    fun toggleHaptics() {
        _isHapticEnabled.value = soundSynth.toggleHaptics()
    }

    fun toggleVoice() {
        soundSynth.playClick()
        voiceEngine.toggleVoice()
    }

    fun setVoiceStyle(style: AlexVoiceStyle) {
        voiceEngine.setStyle(style)
        voiceEngine.speak("Voice style set to ${style.displayName}. I'm ready to speak with you, Diana!")
    }

    fun setVoicePitch(pitch: Float) {
        voiceEngine.setPitch(pitch)
    }

    fun setVoiceRate(rate: Float) {
        voiceEngine.setRate(rate)
    }

    // --- Live Voice & Chat Companion ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        soundSynth.playClick()

        val userMsg = ChatMessage(sender = "Diana", text = text.trim(), isFromAi = false)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAiThinking.value = true
        viewModelScope.launch {
            val responseText = geminiAi.generateAlexResponse(text, _chatMessages.value)
            _isAiThinking.value = false

            val alexMsg = ChatMessage(sender = "Alex", text = responseText, isFromAi = true)
            _chatMessages.value = _chatMessages.value + alexMsg

            if (voiceEngine.isVoiceEnabled.value) {
                voiceEngine.speak(responseText)
            }
        }
    }

    fun clearChat() {
        soundSynth.playClick()
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "Alex",
                text = "A fresh conversation for you, Diana! What shall we talk about?",
                isFromAi = true
            )
        )
    }

    // --- Interactive Conversation Player ---
    fun startConversation(conversation: ConversationEntity) {
        soundSynth.playClick()
        _activeConversation.value = conversation
        _conversationHistory.value = emptyList()

        try {
            val script = dialogueScriptAdapter.fromJson(conversation.dialogueJson)
            _activeScript.value = script
            val firstNode = script?.nodes?.find { it.id == script.initialNodeId } ?: script?.nodes?.firstOrNull()
            _currentNode.value = firstNode

            if (firstNode != null) {
                _conversationHistory.value = listOf(firstNode.speaker to firstNode.text)
                if (firstNode.speaker == "Alex" && voiceEngine.isVoiceEnabled.value) {
                    voiceEngine.speak(firstNode.text)
                }
            }
            _currentScreen.value = ScreenState.CONVERSATION_PLAYER

            viewModelScope.launch {
                repository.recordConversationPlayed(conversation.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectDialogueChoice(choice: DialogueChoice) {
        soundSynth.playClick()
        val script = _activeScript.value ?: return
        val targetNode = script.nodes.find { it.id == choice.targetNodeId }

        _conversationHistory.value = _conversationHistory.value + ("Diana" to choice.text)

        if (targetNode != null) {
            _currentNode.value = targetNode
            _conversationHistory.value = _conversationHistory.value + (targetNode.speaker to targetNode.text)

            if (targetNode.speaker == "Alex" && voiceEngine.isVoiceEnabled.value) {
                voiceEngine.speak(targetNode.text)
            }
        } else {
            // End of conversation
            _currentNode.value = null
        }
    }

    fun replayCurrentDialogueLine() {
        val node = _currentNode.value
        if (node != null && node.speaker == "Alex") {
            voiceEngine.speak(node.text)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            soundSynth.playClick()
        }
    }

    // --- Conversational Experience Creator ---
    fun startNewConversationCreation() {
        soundSynth.playClick()
        _creatorTitle.value = "New Story for Diana"
        _creatorCategory.value = "Romantic"
        _creatorDescription.value = "An original interactive conversation for Diana."
        _creatorNodes.value = listOf(
            DialogueNode(
                id = "node_1",
                speaker = "Alex",
                text = "Diana, what adventure shall we embark on today?",
                emotion = "loving",
                choices = listOf(
                    DialogueChoice("A starlight journey across the galaxy!", "node_2", "excited"),
                    DialogueChoice("A cozy quiet evening together.", "node_3", "loving")
                )
            ),
            DialogueNode(
                id = "node_2",
                speaker = "Alex",
                text = "Then pack your brightest dreams! We will sail past nebulae and race through the stars!",
                emotion = "excited",
                choices = emptyList()
            ),
            DialogueNode(
                id = "node_3",
                speaker = "Alex",
                text = "Nothing in this world brings me more peace than holding your hand under the moonlight.",
                emotion = "loving",
                choices = emptyList()
            )
        )
        _currentScreen.value = ScreenState.CONVERSATION_CREATOR
    }

    fun updateCreatorTitle(title: String) {
        _creatorTitle.value = title
    }

    fun updateCreatorCategory(category: String) {
        _creatorCategory.value = category
    }

    fun updateCreatorDescription(desc: String) {
        _creatorDescription.value = desc
    }

    fun addCreatorNode(speaker: String, text: String, emotion: String) {
        val newId = "node_${_creatorNodes.value.size + 1}"
        val newNode = DialogueNode(
            id = newId,
            speaker = speaker,
            text = text,
            emotion = emotion,
            choices = emptyList()
        )
        _creatorNodes.value = _creatorNodes.value + newNode
        soundSynth.playClick()
    }

    fun updateCreatorNode(index: Int, node: DialogueNode) {
        val list = _creatorNodes.value.toMutableList()
        if (index in list.indices) {
            list[index] = node
            _creatorNodes.value = list
        }
    }

    fun deleteCreatorNode(index: Int) {
        val list = _creatorNodes.value.toMutableList()
        if (index in list.indices && list.size > 1) {
            list.removeAt(index)
            _creatorNodes.value = list
            soundSynth.playClick()
        }
    }

    fun addChoiceToNode(nodeIndex: Int, choiceText: String, targetNodeId: String) {
        val list = _creatorNodes.value.toMutableList()
        if (nodeIndex in list.indices) {
            val node = list[nodeIndex]
            val updatedChoices = node.choices + DialogueChoice(choiceText, targetNodeId)
            list[nodeIndex] = node.copy(choices = updatedChoices)
            _creatorNodes.value = list
            soundSynth.playClick()
        }
    }

    fun removeChoiceFromNode(nodeIndex: Int, choiceIndex: Int) {
        val list = _creatorNodes.value.toMutableList()
        if (nodeIndex in list.indices) {
            val node = list[nodeIndex]
            val updatedChoices = node.choices.toMutableList()
            if (choiceIndex in updatedChoices.indices) {
                updatedChoices.removeAt(choiceIndex)
                list[nodeIndex] = node.copy(choices = updatedChoices)
                _creatorNodes.value = list
                soundSynth.playClick()
            }
        }
    }

    fun testPlayNodeVoice(text: String) {
        voiceEngine.speak(text)
    }

    fun saveCreatorConversation() {
        viewModelScope.launch {
            val script = DialogueScript(
                title = _creatorTitle.value,
                initialNodeId = _creatorNodes.value.firstOrNull()?.id ?: "node_1",
                nodes = _creatorNodes.value
            )
            val json = dialogueScriptAdapter.toJson(script)
            val entity = ConversationEntity(
                id = "custom_conv_${UUID.randomUUID()}",
                title = _creatorTitle.value,
                description = _creatorDescription.value,
                author = "Diana & Alex",
                category = _creatorCategory.value,
                dialogueJson = json,
                dateCreated = System.currentTimeMillis(),
                isPreset = false
            )
            repository.saveConversation(entity)
            soundSynth.playCombo(5)
            voiceEngine.speak("Your conversational experience '${_creatorTitle.value}' has been created, Diana! Let's play it together.")
            _currentScreen.value = ScreenState.CONVERSATION_STUDIO
        }
    }

    // --- Skin Customizer ---
    fun equipSkin(skinId: String) {
        viewModelScope.launch {
            repository.equipSkin(skinId)
            soundSynth.playClick()
        }
    }

    fun deleteSkin(skinId: String) {
        viewModelScope.launch {
            repository.deleteSkin(skinId)
            soundSynth.playClick()
        }
    }

    fun startCustomizing(skin: SkinEntity? = null) {
        val base = skin ?: equippedSkin.value ?: SkinEntity(
            id = "custom_draft_${UUID.randomUUID()}",
            name = "Custom Hero",
            isCustomGallery = false
        )
        _draftSkin.value = base.copy(id = if (base.id.startsWith("preset_")) "custom_${UUID.randomUUID()}" else base.id)
        navigateTo(ScreenState.CUSTOMIZER)
    }

    fun updateDraftBodyShape(shape: BodyShape) {
        _draftSkin.value = _draftSkin.value.copy(bodyShape = shape)
        soundSynth.playClick()
    }

    fun updateDraftAuraColor(colorHex: Long) {
        _draftSkin.value = _draftSkin.value.copy(auraColorHex = colorHex)
    }

    fun updateDraftSecondaryColor(colorHex: Long) {
        _draftSkin.value = _draftSkin.value.copy(secondaryColorHex = colorHex)
    }

    fun updateDraftTrail(trail: TrailType) {
        _draftSkin.value = _draftSkin.value.copy(trailType = trail)
        soundSynth.playClick()
    }

    fun updateDraftAccessory(accessory: AccessoryType) {
        _draftSkin.value = _draftSkin.value.copy(accessory = accessory)
        soundSynth.playClick()
    }

    fun updateDraftWeapon(weapon: WeaponFx) {
        _draftSkin.value = _draftSkin.value.copy(weaponFx = weapon)
        soundSynth.playClick()
    }

    fun updateDraftName(name: String) {
        _draftSkin.value = _draftSkin.value.copy(name = name)
    }

    fun importGalleryAsset(uri: Uri) {
        viewModelScope.launch {
            val localPath = repository.importGalleryImage(uri)
            if (localPath != null) {
                _draftSkin.value = _draftSkin.value.copy(
                    isCustomGallery = true,
                    galleryFilePath = localPath,
                    bodyShape = BodyShape.CUSTOM_GALLERY,
                    name = if (_draftSkin.value.name == "My Custom Skin") "Gallery Hero" else _draftSkin.value.name
                )
                soundSynth.playShardPickup()
            }
        }
    }

    fun saveDraftAndEquip() {
        viewModelScope.launch {
            val skinToSave = _draftSkin.value.copy(
                isEquipped = true,
                dateCreated = System.currentTimeMillis()
            )
            repository.saveSkin(skinToSave)
            repository.equipSkin(skinToSave.id)
            soundSynth.playCombo(3)
            _currentScreen.value = ScreenState.HOME
        }
    }

    fun startActiveGame(width: Float, height: Float) {
        val currentSkin = equippedSkin.value ?: allSkins.value.firstOrNull() ?: SkinEntity(
            id = "preset_default",
            name = "Runner"
        )
        gameEngine.initialize(width, height, currentSkin, _selectedMode.value)
        _currentScreen.value = ScreenState.GAME
    }

    fun onGameOver() {
        viewModelScope.launch {
            repository.updateRunStats(
                score = gameEngine.score,
                mode = gameEngine.currentGameMode.name,
                shards = gameEngine.shardsCollected,
                enemiesDefeated = gameEngine.enemiesDefeated,
                maxCombo = gameEngine.maxComboInRun
            )
        }
    }
}
