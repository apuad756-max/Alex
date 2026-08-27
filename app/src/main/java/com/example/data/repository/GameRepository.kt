package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.db.GameDatabase
import com.example.data.models.AccessoryType
import com.example.data.models.BodyShape
import com.example.data.models.GameStatsEntity
import com.example.data.models.SkinEntity
import com.example.data.models.TrailType
import com.example.data.models.WeaponFx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class GameRepository(private val context: Context) {
    private val database = GameDatabase.getDatabase(context)
    private val skinDao = database.skinDao()
    private val statsDao = database.gameStatsDao()

    val allSkins: Flow<List<SkinEntity>> = skinDao.getAllSkins()
    val equippedSkin: Flow<SkinEntity?> = skinDao.getEquippedSkinFlow()
    val gameStats: Flow<GameStatsEntity?> = statsDao.getStatsFlow()
    val allConversations: Flow<List<com.example.data.models.ConversationEntity>> = database.conversationDao().getAllConversations()
    private val conversationDao = database.conversationDao()

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        if (conversationDao.getConversationCount() == 0) {
            val defaultStories = listOf(
                com.example.data.models.ConversationEntity(
                    id = "conv_message_from_alex",
                    title = "A Heartfelt Message from Alex",
                    description = "A special opening message from Alex dedicated to Diana.",
                    author = "Alex",
                    category = "Romantic",
                    dialogueJson = """
                        {
                            "title": "A Heartfelt Message from Alex",
                            "initialNodeId": "node_1",
                            "nodes": [
                                {
                                    "id": "node_1",
                                    "speaker": "Alex",
                                    "text": "Diana, I built this entire world and this game just for you. Every star in the sky and every neon trail we blaze is because you inspire me.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "Alex, how did you make all this?", "targetNodeId": "node_2_sweet", "emotionTrigger": "loving"},
                                        {"text": "Are you ready to be my battle partner?", "targetNodeId": "node_2_battle", "emotionTrigger": "heroic"},
                                        {"text": "Tell me what you love about me.", "targetNodeId": "node_2_compliment", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_2_sweet",
                                    "speaker": "Alex",
                                    "text": "I poured my heart into every line and every color, making sure you would always have a smile and a companion right here. You deserve everything beautiful, Diana.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "Thank you, Alex. I love it so much!", "targetNodeId": "node_3_end", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_2_battle",
                                    "speaker": "Alex",
                                    "text": "Always, Diana! We will fight back to back, collect every glowing shard, and shatter every high score together!",
                                    "emotion": "heroic",
                                    "choices": [
                                        {"text": "Let's dash into the arena together!", "targetNodeId": "node_3_end", "emotionTrigger": "excited"}
                                    ]
                                },
                                {
                                    "id": "node_2_compliment",
                                    "speaker": "Alex",
                                    "text": "I love your radiant smile, your boundless kindness, your fiery determination, and the sweet way you brighten every single day.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "You always melt my heart, Alex.", "targetNodeId": "node_3_end", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_3_end",
                                    "speaker": "Alex",
                                    "text": "Whenever you are ready, I am right here by your side, Diana. Forever and always.",
                                    "emotion": "loving",
                                    "choices": []
                                }
                            ]
                        }
                    """.trimIndent(),
                    isPreset = true,
                    dateCreated = 2000
                ),
                com.example.data.models.ConversationEntity(
                    id = "conv_battle_tactics",
                    title = "The Neon Duel Tactics",
                    description = "Alex briefs Diana on strategy before taking on high-tier arenas.",
                    author = "Alex",
                    category = "Battle Briefing",
                    dialogueJson = """
                        {
                            "title": "The Neon Duel Tactics",
                            "initialNodeId": "node_1",
                            "nodes": [
                                {
                                    "id": "node_1",
                                    "speaker": "Alex",
                                    "text": "Diana, before we dive into the combat zone, what is your battle plan for today?",
                                    "emotion": "heroic",
                                    "choices": [
                                        {"text": "Speed & Maximum Combo Slashes!", "targetNodeId": "node_speed", "emotionTrigger": "excited"},
                                        {"text": "Boss Trial! I want to defeat the Titan.", "targetNodeId": "node_boss", "emotionTrigger": "heroic"}
                                    ]
                                },
                                {
                                    "id": "node_speed",
                                    "speaker": "Alex",
                                    "text": "That is my lightning champion! Keep your fingers dragging smoothly to slice through neon shards and unleash our Nova burst as soon as the gauge fills.",
                                    "emotion": "excited",
                                    "choices": [
                                        {"text": "Got it! Watch me set a new record, Alex.", "targetNodeId": "node_end", "emotionTrigger": "heroic"}
                                    ]
                                },
                                {
                                    "id": "node_boss",
                                    "speaker": "Alex",
                                    "text": "Understood! When the Titan charges its plasma blasts, use your dash slash to phase right through. I will be tracking its weak points for you!",
                                    "emotion": "heroic",
                                    "choices": [
                                        {"text": "We will take it down together!", "targetNodeId": "node_end", "emotionTrigger": "heroic"}
                                    ]
                                },
                                {
                                    "id": "node_end",
                                    "speaker": "Alex",
                                    "text": "I will be calling out the action and watching your back every second. Let's do this, Diana!",
                                    "emotion": "heroic",
                                    "choices": []
                                }
                            ]
                        }
                    """.trimIndent(),
                    isPreset = true,
                    dateCreated = 1900
                ),
                com.example.data.models.ConversationEntity(
                    id = "conv_starlit_promise",
                    title = "Starlit Night & Whispers",
                    description = "A quiet romantic moment under the neon constellations.",
                    author = "Alex",
                    category = "Romantic",
                    dialogueJson = """
                        {
                            "title": "Starlit Night & Whispers",
                            "initialNodeId": "node_1",
                            "nodes": [
                                {
                                    "id": "node_1",
                                    "speaker": "Alex",
                                    "text": "Look up at the night sky with me for a moment, Diana. Even with all the action in this world, having you in my life is the greatest victory I could ever ask for.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "You make every moment special, Alex.", "targetNodeId": "node_special", "emotionTrigger": "loving"},
                                        {"text": "Will you always cheer for me in our adventures?", "targetNodeId": "node_cheer", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_special",
                                    "speaker": "Alex",
                                    "text": "Because you are truly special, Diana. Every game we play, every story we write, is a new memory I cherish.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "I love you, Alex.", "targetNodeId": "node_love", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_cheer",
                                    "speaker": "Alex",
                                    "text": "In every run, every challenge, and every step of our life, my voice will always be cheering for you, Diana.",
                                    "emotion": "loving",
                                    "choices": [
                                        {"text": "I love you, Alex.", "targetNodeId": "node_love", "emotionTrigger": "loving"}
                                    ]
                                },
                                {
                                    "id": "node_love",
                                    "speaker": "Alex",
                                    "text": "I love you endlessly, Diana. Let's make every adventure we share pure magic.",
                                    "emotion": "loving",
                                    "choices": []
                                }
                            ]
                        }
                    """.trimIndent(),
                    isPreset = true,
                    dateCreated = 1800
                )
            )
            conversationDao.insertConversations(defaultStories)
        }
        if (skinDao.getSkinCount() == 0) {
            val defaults = listOf(
                SkinEntity(
                    id = "preset_cyber_blade",
                    name = "Cyber Runner",
                    isCustomGallery = false,
                    builtInDrawableResName = "img_skin_cyber_ninja_1787850502328",
                    bodyShape = BodyShape.CUSTOM_GALLERY,
                    auraColorHex = 0xFF00E5FF, // Neon Cyan
                    secondaryColorHex = 0xFF7C4DFF, // Electric Purple
                    trailType = TrailType.CYBER_SPARKS,
                    accessory = AccessoryType.CYBER_VISOR,
                    weaponFx = WeaponFx.BLADE_SLASH,
                    isEquipped = true,
                    isUnlocked = true,
                    dateCreated = 1000
                ),
                SkinEntity(
                    id = "preset_solar_beast",
                    name = "Solar Phoenix",
                    isCustomGallery = false,
                    builtInDrawableResName = "img_skin_solar_beast_1787850519217",
                    bodyShape = BodyShape.CUSTOM_GALLERY,
                    auraColorHex = 0xFFFF9100, // Fiery Orange
                    secondaryColorHex = 0xFFFFD700, // Gold
                    trailType = TrailType.PLASMA_FLAME,
                    accessory = AccessoryType.ANGEL_WINGS,
                    weaponFx = WeaponFx.PLASMA_PULSE,
                    isEquipped = false,
                    isUnlocked = true,
                    dateCreated = 900
                ),
                SkinEntity(
                    id = "preset_mecha_core",
                    name = "Void Mecha",
                    isCustomGallery = false,
                    builtInDrawableResName = "img_skin_mecha_core_1787850539763",
                    bodyShape = BodyShape.CUSTOM_GALLERY,
                    auraColorHex = 0xFFD500F9, // Vivid Magenta
                    secondaryColorHex = 0xFF00E676, // Neon Green
                    trailType = TrailType.VOID_SMOKE,
                    accessory = AccessoryType.ORBITING_ORBS,
                    weaponFx = WeaponFx.STAR_SHURIKEN,
                    isEquipped = false,
                    isUnlocked = true,
                    dateCreated = 800
                ),
                SkinEntity(
                    id = "preset_prism_diamond",
                    name = "Prism Shifter",
                    isCustomGallery = false,
                    bodyShape = BodyShape.DIAMOND,
                    auraColorHex = 0xFF00E676, // Emerald
                    secondaryColorHex = 0xFF00B0FF, // Deep Sky Blue
                    trailType = TrailType.RAINBOW_DUST,
                    accessory = AccessoryType.NEON_CROWN,
                    weaponFx = WeaponFx.DUAL_SABERS,
                    isEquipped = false,
                    isUnlocked = true,
                    dateCreated = 700
                ),
                SkinEntity(
                    id = "preset_nova_star",
                    name = "Nova Shuriken",
                    isCustomGallery = false,
                    bodyShape = BodyShape.SHURIKEN,
                    auraColorHex = 0xFFFF1744, // Crimson Red
                    secondaryColorHex = 0xFFFFEA00, // Bright Yellow
                    trailType = TrailType.NEON_STREAM,
                    accessory = AccessoryType.CYBER_HORNS,
                    weaponFx = WeaponFx.STAR_SHURIKEN,
                    isEquipped = false,
                    isUnlocked = true,
                    dateCreated = 600
                ),
                SkinEntity(
                    id = "preset_aegis_shield",
                    name = "Aegis Sentinel",
                    isCustomGallery = false,
                    bodyShape = BodyShape.SHIELD,
                    auraColorHex = 0xFFFFD700, // Pure Gold
                    secondaryColorHex = 0xFFFFFFFF, // Pure White
                    trailType = TrailType.PLASMA_FLAME,
                    accessory = AccessoryType.NINJA_BANDANA,
                    weaponFx = WeaponFx.BLADE_SLASH,
                    isEquipped = false,
                    isUnlocked = true,
                    dateCreated = 500
                )
            )
            skinDao.insertSkins(defaults)
        }

        if (statsDao.getStats() == null) {
            statsDao.insertStats(
                GameStatsEntity(
                    id = 1,
                    highScoreEndless = 0,
                    highScoreBlitz = 0,
                    highScoreBossTrial = 0,
                    totalShardsCollected = 0,
                    totalEnemiesDefeated = 0,
                    totalGamesPlayed = 0,
                    highestCombo = 0
                )
            )
        }
    }

    suspend fun equipSkin(id: String) = withContext(Dispatchers.IO) {
        skinDao.equipSkin(id)
    }

    suspend fun saveSkin(skin: SkinEntity) = withContext(Dispatchers.IO) {
        skinDao.insertSkin(skin)
    }

    suspend fun deleteSkin(id: String) = withContext(Dispatchers.IO) {
        val skin = skinDao.getSkinById(id)
        if (skin?.galleryFilePath != null) {
            try {
                val file = File(skin.galleryFilePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        skinDao.deleteSkinById(id)
        // If deleted was equipped, equip first available
        val currentEquipped = skinDao.getEquippedSkin()
        if (currentEquipped == null) {
            val all = skinDao.getSkinCount()
            if (all > 0) {
                skinDao.setEquipped("preset_cyber_blade")
            }
        }
    }

    suspend fun importGalleryImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val skinsDir = File(context.filesDir, "custom_skins").apply { mkdirs() }
            val fileName = "skin_${UUID.randomUUID()}.png"
            val destFile = File(skinsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                val originalBitmap = BitmapFactory.decodeStream(input) ?: return@withContext null
                // Scale down if huge to keep game performance ultra high
                val maxDim = 512
                val scaledBitmap = if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                    val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                    val targetW: Int
                    val targetH: Int
                    if (ratio > 1f) {
                        targetW = maxDim
                        targetH = (maxDim / ratio).toInt()
                    } else {
                        targetH = maxDim
                        targetW = (maxDim * ratio).toInt()
                    }
                    Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                } else {
                    originalBitmap
                }

                FileOutputStream(destFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveConversation(conversation: com.example.data.models.ConversationEntity) = withContext(Dispatchers.IO) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun recordConversationPlayed(id: String) = withContext(Dispatchers.IO) {
        conversationDao.incrementPlayCount(id)
    }

    suspend fun updateRunStats(
        score: Int,
        mode: String,
        shards: Int,
        enemiesDefeated: Int,
        maxCombo: Int
    ) = withContext(Dispatchers.IO) {
        val current = statsDao.getStats() ?: GameStatsEntity()
        val updatedHighEndless = if (mode == "ENDLESS") maxOf(current.highScoreEndless, score) else current.highScoreEndless
        val updatedHighBlitz = if (mode == "BLITZ_60") maxOf(current.highScoreBlitz, score) else current.highScoreBlitz
        val updatedHighBoss = if (mode == "BOSS_TRIAL") maxOf(current.highScoreBossTrial, score) else current.highScoreBossTrial
        val updatedCombo = maxOf(current.highestCombo, maxCombo)

        statsDao.updateStats(
            current.copy(
                highScoreEndless = updatedHighEndless,
                highScoreBlitz = updatedHighBlitz,
                highScoreBossTrial = updatedHighBoss,
                totalShardsCollected = current.totalShardsCollected + shards,
                totalEnemiesDefeated = current.totalEnemiesDefeated + enemiesDefeated,
                totalGamesPlayed = current.totalGamesPlayed + 1,
                highestCombo = updatedCombo
            )
        )
    }
}
