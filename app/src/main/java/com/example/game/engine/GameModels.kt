package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.data.models.SkinEntity

enum class EnemyType {
    DRONE,          // Moves directly towards player with acceleration
    LASER_ORB,      // Charges laser beam with warning line, then fires
    SPIKE_BOUNCER,  // High speed bouncing orb
    PHANTOM_SPECTRE,// Fades in and out, teleports near player
    BOSS_TITAN      // Colossal multi-phase boss
}

enum class ShardType {
    ENERGY_CYAN,
    ENERGY_GOLD,
    HEALTH_HEART,
    OVERDRIVE_STAR
}

data class PlayerState(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 32f,
    var hp: Int = 3,
    var maxHp: Int = 3,
    var energy: Float = 0f, // 0 to 100
    var isDashing: Boolean = false,
    var dashTimeRemaining: Float = 0f,
    var dashCooldown: Float = 0f,
    var invulnerableTime: Float = 0f,
    var skin: SkinEntity? = null,
    var customBitmap: android.graphics.Bitmap? = null
)

data class Enemy(
    val id: Long,
    val type: EnemyType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var hp: Int = 1,
    val maxHp: Int = 1,
    var radius: Float = 26f,
    val color: Color = Color.Red,
    var stateTimer: Float = 0f,
    var isWarning: Boolean = false,
    var warningProgress: Float = 0f,
    var alpha: Float = 1f,
    var laserAngle: Float = 0f,
    var isFiringLaser: Boolean = false
)

data class Shard(
    val id: Long,
    val type: ShardType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 16f,
    var lifeTime: Float = 0f,
    val value: Int = 100
)

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var radius: Float,
    var life: Float,
    val maxLife: Float,
    val isGlow: Boolean = false
)

data class SlashArc(
    val x: Float,
    val y: Float,
    val angle: Float,
    val radius: Float,
    val color: Color,
    var life: Float = 0.18f,
    val maxLife: Float = 0.18f
)

data class NovaWave(
    val x: Float,
    val y: Float,
    var currentRadius: Float = 10f,
    val maxRadius: Float = 900f,
    val color: Color = Color(0xFF00F5FF),
    var life: Float = 0.6f,
    val maxLife: Float = 0.6f
)

data class FloatingText(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 0.8f,
    val maxLife: Float = 0.8f
)

data class BossData(
    val name: String,
    var hp: Int,
    val maxHp: Int,
    var phase: Int = 1,
    var attackTimer: Float = 0f,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var radius: Float = 64f,
    var isEnraged: Boolean = false
)
