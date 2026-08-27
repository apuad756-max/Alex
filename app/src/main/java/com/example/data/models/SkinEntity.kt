package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skins")
data class SkinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isCustomGallery: Boolean = false,
    val galleryFilePath: String? = null,
    val builtInDrawableResName: String? = null,
    val bodyShape: BodyShape = BodyShape.ORB,
    val auraColorHex: Long = 0xFF00F5FF, // Default Cyan
    val secondaryColorHex: Long = 0xFF8A2BE2, // Default Violet
    val trailType: TrailType = TrailType.CYBER_SPARKS,
    val accessory: AccessoryType = AccessoryType.NONE,
    val weaponFx: WeaponFx = WeaponFx.BLADE_SLASH,
    val isEquipped: Boolean = false,
    val isUnlocked: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val id: Int = 1,
    val highScoreEndless: Int = 0,
    val highScoreBlitz: Int = 0,
    val highScoreBossTrial: Int = 0,
    val totalShardsCollected: Int = 0,
    val totalEnemiesDefeated: Int = 0,
    val totalGamesPlayed: Int = 0,
    val highestCombo: Int = 0
)
