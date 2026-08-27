package com.example.game.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import com.example.data.models.AccessoryType
import com.example.data.models.BodyShape
import com.example.data.models.GameMode
import com.example.data.models.SkinEntity
import com.example.data.models.TrailType
import com.example.data.models.WeaponFx
import com.example.game.audio.SoundSynth
import com.example.voice.AlexVoiceEngine
import com.example.voice.GameVoiceMoment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    private val context: Context,
    val soundSynth: SoundSynth,
    val voiceEngine: AlexVoiceEngine? = null
) {
    val player = PlayerState()
    val enemies = mutableListOf<Enemy>()
    val shards = mutableListOf<Shard>()
    val particles = mutableListOf<GameParticle>()
    val slashes = mutableListOf<SlashArc>()
    val novaWaves = mutableListOf<NovaWave>()
    val floatingTexts = mutableListOf<FloatingText>()
    var boss: BossData? = null

    // Game state tracking
    var screenWidth = 1080f
    var screenHeight = 2160f
    var isInitialized = false

    var score = 0
    var shardsCollected = 0
    var enemiesDefeated = 0
    var currentCombo = 0
    var maxComboInRun = 0
    var comboTimer = 0f
    var gameTime = 0f
    var blitzTimeRemaining = 60f
    var isGameOver = false
    var isPaused = false
    var isVictory = false
    var currentGameMode = GameMode.ENDLESS

    var screenShake = 0f
    var hitStopTimer = 0f

    private var spawnTimer = 0f
    private var nextEntityId = 1L

    private val _gameStateEvent = MutableStateFlow<Long>(0L)
    val gameStateEvent = _gameStateEvent.asStateFlow()

    fun initialize(width: Float, height: Float, skin: SkinEntity, mode: GameMode) {
        screenWidth = width
        screenHeight = height
        currentGameMode = mode

        player.x = width / 2f
        player.y = height * 0.7f
        player.targetX = player.x
        player.targetY = player.y
        player.hp = 3
        player.maxHp = 3
        player.energy = 0f
        player.isDashing = false
        player.dashCooldown = 0f
        player.invulnerableTime = 1f
        player.skin = skin
        loadSkinBitmap(skin)

        enemies.clear()
        shards.clear()
        particles.clear()
        slashes.clear()
        novaWaves.clear()
        floatingTexts.clear()
        boss = null

        score = 0
        shardsCollected = 0
        enemiesDefeated = 0
        currentCombo = 0
        maxComboInRun = 0
        comboTimer = 0f
        gameTime = 0f
        blitzTimeRemaining = 60f
        isGameOver = false
        isPaused = false
        isVictory = false
        screenShake = 0f
        hitStopTimer = 0f
        spawnTimer = 0f

        if (mode == GameMode.BOSS_TRIAL) {
            spawnBoss()
        } else {
            voiceEngine?.speakGameMoment(GameVoiceMoment.START_RUN)
        }

        isInitialized = true
        _gameStateEvent.value = System.currentTimeMillis()
    }

    private fun loadSkinBitmap(skin: SkinEntity) {
        try {
            if (skin.galleryFilePath != null) {
                val file = File(skin.galleryFilePath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(skin.galleryFilePath)
                    player.customBitmap = bmp
                    return
                }
            }
            if (skin.builtInDrawableResName != null) {
                val resId = context.resources.getIdentifier(
                    skin.builtInDrawableResName,
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    val bmp = BitmapFactory.decodeResource(context.resources, resId)
                    player.customBitmap = bmp
                    return
                }
            }
            player.customBitmap = null
        } catch (e: Exception) {
            player.customBitmap = null
        }
    }

    fun updatePlayerTarget(tx: Float, ty: Float) {
        if (isGameOver || isPaused) return
        player.targetX = tx.coerceIn(player.radius, screenWidth - player.radius)
        player.targetY = ty.coerceIn(player.radius + 60f, screenHeight - player.radius - 80f)
    }

    fun triggerDash(targetX: Float? = null, targetY: Float? = null) {
        if (isGameOver || isPaused || player.dashCooldown > 0f) return

        val destX = targetX ?: player.targetX
        val destY = targetY ?: player.targetY
        val dx = destX - player.x
        val dy = destY - player.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist > 10f) {
            player.isDashing = true
            player.dashTimeRemaining = 0.22f
            player.dashCooldown = 0.45f
            player.invulnerableTime = 0.28f

            val dashSpeed = 1900f
            player.vx = (dx / dist) * dashSpeed
            player.vy = (dy / dist) * dashSpeed

            val slashAngle = (atan2(dy, dx) * 180f / PI).toFloat()
            val auraColor = Color(player.skin?.auraColorHex ?: 0xFF00F5FF)
            slashes.add(
                SlashArc(
                    x = player.x + (dx / dist) * 40f,
                    y = player.y + (dy / dist) * 40f,
                    angle = slashAngle,
                    radius = 90f,
                    color = auraColor
                )
            )

            // Emit burst particles
            emitBurstParticles(player.x, player.y, auraColor, 18)
            soundSynth.playDash()
        }
    }

    fun triggerNova() {
        if (isGameOver || isPaused || player.energy < 100f) return

        player.energy = 0f
        val auraColor = Color(player.skin?.auraColorHex ?: 0xFF00F5FF)
        novaWaves.add(
            NovaWave(
                x = player.x,
                y = player.y,
                currentRadius = player.radius,
                maxRadius = maxOf(screenWidth, screenHeight),
                color = auraColor
            )
        )

        screenShake = 16f
        soundSynth.playNova()
        addFloatingText(player.x, player.y - 40f, "NOVA BURST!", auraColor)

        // Clear all nearby enemies
        val it = enemies.iterator()
        while (it.hasNext()) {
            val enemy = it.next()
            val dx = enemy.x - player.x
            val dy = enemy.y - player.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 800f) {
                enemy.hp = 0
                defeatEnemy(enemy)
                it.remove()
            }
        }

        // Damage boss heavily if present
        boss?.let { b ->
            b.hp -= 25
            emitBurstParticles(b.x, b.y, Color.Magenta, 30)
            if (b.hp <= 0) {
                defeatBoss()
            }
        }
    }

    fun tick(delta: Float) {
        if (isGameOver || isPaused || !isInitialized) return

        if (hitStopTimer > 0f) {
            hitStopTimer -= delta
            return
        }

        val dt = delta.coerceIn(0.001f, 0.05f)
        gameTime += dt

        if (screenShake > 0f) {
            screenShake = (screenShake - dt * 25f).coerceAtLeast(0f)
        }

        // Combo timer decay
        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                currentCombo = 0
            }
        }

        // Blitz mode timer
        if (currentGameMode == GameMode.BLITZ_60) {
            blitzTimeRemaining -= dt
            if (blitzTimeRemaining <= 0f) {
                blitzTimeRemaining = 0f
                isVictory = true
                isGameOver = true
                soundSynth.playCombo(10)
                return
            }
        }

        // Update player
        updatePlayer(dt)

        // Spawning logic
        updateSpawning(dt)

        // Update enemies
        updateEnemies(dt)

        // Update boss
        updateBoss(dt)

        // Update shards
        updateShards(dt)

        // Update particles, slashes, nova waves, floating texts
        updateEffects(dt)

        _gameStateEvent.value = System.currentTimeMillis()
    }

    private fun updatePlayer(dt: Float) {
        if (player.invulnerableTime > 0f) {
            player.invulnerableTime -= dt
        }
        if (player.dashCooldown > 0f) {
            player.dashCooldown -= dt
        }

        if (player.isDashing) {
            player.dashTimeRemaining -= dt
            player.x += player.vx * dt
            player.y += player.vy * dt

            // Trail particles
            emitTrailParticle(player.x, player.y, player.skin?.trailType ?: TrailType.CYBER_SPARKS)

            // Dash blade collision check
            val it = enemies.iterator()
            while (it.hasNext()) {
                val enemy = it.next()
                val dx = enemy.x - player.x
                val dy = enemy.y - player.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < player.radius + enemy.radius + 25f) {
                    enemy.hp -= 2
                    if (enemy.hp <= 0) {
                        defeatEnemy(enemy)
                        it.remove()
                    } else {
                        emitBurstParticles(enemy.x, enemy.y, Color.Yellow, 8)
                        soundSynth.playSlash()
                    }
                }
            }

            boss?.let { b ->
                val dx = b.x - player.x
                val dy = b.y - player.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < player.radius + b.radius + 20f) {
                    b.hp -= 2
                    emitBurstParticles(player.x, player.y, Color.Yellow, 10)
                    soundSynth.playSlash()
                    if (b.hp <= 0) {
                        defeatBoss()
                    }
                }
            }

            if (player.dashTimeRemaining <= 0f) {
                player.isDashing = false
                player.vx = 0f
                player.vy = 0f
            }
        } else {
            // Smooth movement toward target touch point
            val dx = player.targetX - player.x
            val dy = player.targetY - player.y
            val dist = sqrt(dx * dx + dy * dy)
            val moveSpeed = 750f
            if (dist > 4f) {
                val step = moveSpeed * dt
                if (dist <= step) {
                    player.x = player.targetX
                    player.y = player.targetY
                } else {
                    player.x += (dx / dist) * step
                    player.y += (dy / dist) * step
                }
                // Normal moving trail
                if (Random.nextFloat() < 0.35f) {
                    emitTrailParticle(player.x, player.y, player.skin?.trailType ?: TrailType.CYBER_SPARKS)
                }
            }
        }

        player.x = player.x.coerceIn(player.radius, screenWidth - player.radius)
        player.y = player.y.coerceIn(player.radius + 60f, screenHeight - player.radius - 80f)
    }

    private fun updateSpawning(dt: Float) {
        if (currentGameMode == GameMode.BOSS_TRIAL) {
            // Minor minion spawns during boss fight
            spawnTimer += dt
            if (spawnTimer > 4.5f && enemies.size < 4) {
                spawnTimer = 0f
                spawnDrone()
            }
            return
        }

        spawnTimer += dt
        val baseInterval = if (currentGameMode == GameMode.BLITZ_60) 0.85f else 1.4f
        val difficultyScaling = (gameTime / 40f).coerceAtMost(0.65f)
        val currentInterval = maxOf(0.45f, baseInterval - difficultyScaling)

        if (spawnTimer >= currentInterval) {
            spawnTimer = 0f
            val r = Random.nextFloat()
            when {
                r < 0.45f -> spawnDrone()
                r < 0.70f -> spawnLaserOrb()
                r < 0.88f -> spawnBouncer()
                else -> spawnPhantom()
            }
        }
    }

    private fun spawnDrone() {
        val side = Random.nextInt(4)
        var sx = 0f
        var sy = 0f
        when (side) {
            0 -> { sx = Random.nextFloat() * screenWidth; sy = -30f }
            1 -> { sx = screenWidth + 30f; sy = Random.nextFloat() * screenHeight }
            2 -> { sx = Random.nextFloat() * screenWidth; sy = screenHeight + 30f }
            3 -> { sx = -30f; sy = Random.nextFloat() * screenHeight }
        }
        enemies.add(
            Enemy(
                id = nextEntityId++,
                type = EnemyType.DRONE,
                x = sx,
                y = sy,
                hp = 1,
                maxHp = 1,
                radius = 24f,
                color = Color(0xFFFF1744) // Neon Red
            )
        )
    }

    private fun spawnLaserOrb() {
        val sx = Random.nextFloat() * (screenWidth - 120f) + 60f
        val sy = Random.nextFloat() * (screenHeight * 0.4f) + 80f
        enemies.add(
            Enemy(
                id = nextEntityId++,
                type = EnemyType.LASER_ORB,
                x = sx,
                y = sy,
                hp = 2,
                maxHp = 2,
                radius = 28f,
                color = Color(0xFFFF9100) // Neon Amber
            )
        )
    }

    private fun spawnBouncer() {
        val sx = if (Random.nextBoolean()) -20f else screenWidth + 20f
        val sy = Random.nextFloat() * (screenHeight * 0.6f) + 100f
        val angle = Random.nextFloat() * PI.toFloat() * 2f
        val speed = 360f
        enemies.add(
            Enemy(
                id = nextEntityId++,
                type = EnemyType.SPIKE_BOUNCER,
                x = sx,
                y = sy,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                hp = 1,
                maxHp = 1,
                radius = 22f,
                color = Color(0xFF00E676) // Neon Green
            )
        )
    }

    private fun spawnPhantom() {
        val sx = Random.nextFloat() * (screenWidth - 140f) + 70f
        val sy = Random.nextFloat() * (screenHeight - 240f) + 120f
        enemies.add(
            Enemy(
                id = nextEntityId++,
                type = EnemyType.PHANTOM_SPECTRE,
                x = sx,
                y = sy,
                hp = 2,
                maxHp = 2,
                radius = 26f,
                color = Color(0xFFD500F9) // Vivid Purple
            )
        )
    }

    private fun spawnBoss() {
        boss = BossData(
            name = "TITAN NEXUS",
            hp = 100,
            maxHp = 100,
            phase = 1,
            x = screenWidth / 2f,
            y = screenHeight * 0.25f,
            targetX = screenWidth / 2f,
            targetY = screenHeight * 0.25f,
            radius = 68f
        )
        addFloatingText(screenWidth / 2f, screenHeight * 0.25f - 80f, "BOSS DETECTED!", Color.Red)
        voiceEngine?.speakGameMoment(GameVoiceMoment.BOSS_SPAWN)
    }

    private fun updateEnemies(dt: Float) {
        val it = enemies.iterator()
        while (it.hasNext()) {
            val enemy = it.next()
            enemy.stateTimer += dt

            when (enemy.type) {
                EnemyType.DRONE -> {
                    val dx = player.x - enemy.x
                    val dy = player.y - enemy.y
                    val dist = sqrt(dx * dx + dy * dy)
                    val speed = 230f + (gameTime * 2.5f).coerceAtMost(180f)
                    if (dist > 1f) {
                        enemy.vx = (dx / dist) * speed
                        enemy.vy = (dy / dist) * speed
                        enemy.x += enemy.vx * dt
                        enemy.y += enemy.vy * dt
                    }
                }
                EnemyType.SPIKE_BOUNCER -> {
                    enemy.x += enemy.vx * dt
                    enemy.y += enemy.vy * dt
                    if (enemy.x <= enemy.radius || enemy.x >= screenWidth - enemy.radius) {
                        enemy.vx = -enemy.vx
                    }
                    if (enemy.y <= enemy.radius + 60f || enemy.y >= screenHeight - enemy.radius - 60f) {
                        enemy.vy = -enemy.vy
                    }
                }
                EnemyType.LASER_ORB -> {
                    if (enemy.stateTimer < 2.0f) {
                        enemy.isWarning = true
                        enemy.warningProgress = enemy.stateTimer / 2.0f
                        val dx = player.x - enemy.x
                        val dy = player.y - enemy.y
                        enemy.laserAngle = (atan2(dy, dx) * 180f / PI).toFloat()
                    } else if (enemy.stateTimer < 2.7f) {
                        enemy.isWarning = false
                        enemy.isFiringLaser = true
                        // Laser line hit check
                        checkLaserCollision(enemy)
                    } else {
                        enemy.stateTimer = 0f
                        enemy.isFiringLaser = false
                    }
                }
                EnemyType.PHANTOM_SPECTRE -> {
                    val cycle = (enemy.stateTimer % 3.0f)
                    if (cycle < 1.2f) {
                        enemy.alpha = (1.2f - cycle) / 1.2f
                    } else if (cycle < 2.0f) {
                        enemy.alpha = 0.05f
                    } else {
                        enemy.alpha = (cycle - 2.0f) / 1.0f
                        val dx = player.x - enemy.x
                        val dy = player.y - enemy.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist > 1f) {
                            enemy.x += (dx / dist) * 160f * dt
                            enemy.y += (dy / dist) * 160f * dt
                        }
                    }
                }
                EnemyType.BOSS_TITAN -> {}
            }

            // Check collision with player
            val dx = player.x - enemy.x
            val dy = player.y - enemy.y
            val dist = sqrt(dx * dx + dy * dy)

            // Tap / proximity auto slash if player has weapon and in range
            if (dist < player.radius + enemy.radius + 15f) {
                if (player.isDashing || player.invulnerableTime > 0.15f) {
                    enemy.hp -= 2
                    if (enemy.hp <= 0) {
                        defeatEnemy(enemy)
                        it.remove()
                        continue
                    }
                } else {
                    // Player takes damage
                    damagePlayer(1)
                }
            }
        }
    }

    private fun checkLaserCollision(laserOrb: Enemy) {
        if (player.invulnerableTime > 0f) return

        val rad = (laserOrb.laserAngle * PI / 180f).toFloat()
        val dirX = cos(rad)
        val dirY = sin(rad)

        val px = player.x - laserOrb.x
        val py = player.y - laserOrb.y
        val proj = px * dirX + py * dirY

        if (proj > 0f && proj < 1500f) {
            val perpDist = kotlin.math.abs(px * dirY - py * dirX)
            if (perpDist < player.radius + 18f) {
                damagePlayer(1)
            }
        }
    }

    private fun updateBoss(dt: Float) {
        val b = boss ?: return
        b.attackTimer += dt

        // Hover movement
        val dx = b.targetX - b.x
        val dy = b.targetY - b.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > 5f) {
            b.x += (dx / dist) * 150f * dt
            b.y += (dy / dist) * 150f * dt
        } else {
            b.targetX = Random.nextFloat() * (screenWidth - 200f) + 100f
            b.targetY = Random.nextFloat() * (screenHeight * 0.35f) + 120f
        }

        // Boss attack cycle
        if (b.attackTimer > 2.8f) {
            b.attackTimer = 0f
            // Emit nova bullets / minion orbs
            for (i in 0 until 8) {
                val ang = (i * 45f * PI / 180f).toFloat()
                enemies.add(
                    Enemy(
                        id = nextEntityId++,
                        type = EnemyType.SPIKE_BOUNCER,
                        x = b.x,
                        y = b.y,
                        vx = cos(ang) * 280f,
                        vy = sin(ang) * 280f,
                        hp = 1,
                        radius = 18f,
                        color = Color(0xFFFF0055)
                    )
                )
            }
            soundSynth.playDash()
        }

        // Player collision
        val pdx = player.x - b.x
        val pdy = player.y - b.y
        val pdist = sqrt(pdx * pdx + pdy * pdy)
        if (pdist < player.radius + b.radius) {
            if (player.isDashing) {
                b.hp -= 2
                emitBurstParticles(b.x, b.y, Color.Yellow, 10)
                soundSynth.playSlash()
                if (b.hp <= 0) defeatBoss()
            } else {
                damagePlayer(1)
            }
        }
    }

    private fun updateShards(dt: Float) {
        val it = shards.iterator()
        while (it.hasNext()) {
            val shard = it.next()
            shard.lifeTime += dt

            // Magnetic attraction to player
            val dx = player.x - shard.x
            val dy = player.y - shard.y
            val dist = sqrt(dx * dx + dy * dy)

            val magnetRange = 280f
            if (dist < magnetRange) {
                val pullSpeed = 650f * (1f - dist / magnetRange) + 300f
                shard.vx = (dx / dist) * pullSpeed
                shard.vy = (dy / dist) * pullSpeed
                shard.x += shard.vx * dt
                shard.y += shard.vy * dt
            }

            // Collection check
            if (dist < player.radius + shard.radius) {
                collectShard(shard)
                it.remove()
            } else if (shard.lifeTime > 25f) {
                it.remove()
            }
        }
    }

    private fun collectShard(shard: Shard) {
        shardsCollected++
        val multiplier = 1 + (currentCombo / 3)
        val points = shard.value * multiplier
        score += points

        when (shard.type) {
            ShardType.ENERGY_CYAN -> {
                player.energy = (player.energy + 8f).coerceAtMost(100f)
            }
            ShardType.ENERGY_GOLD -> {
                player.energy = (player.energy + 20f).coerceAtMost(100f)
            }
            ShardType.HEALTH_HEART -> {
                player.hp = (player.hp + 1).coerceAtMost(player.maxHp)
                addFloatingText(player.x, player.y - 30f, "+1 HP", Color.Green)
            }
            ShardType.OVERDRIVE_STAR -> {
                player.energy = 100f
                addFloatingText(player.x, player.y - 30f, "MAX NOVA!", Color.Cyan)
            }
        }

        emitBurstParticles(shard.x, shard.y, Color.Cyan, 6)
        soundSynth.playShardPickup()
    }

    private fun defeatEnemy(enemy: Enemy) {
        enemiesDefeated++
        currentCombo++
        maxComboInRun = maxOf(maxComboInRun, currentCombo)
        comboTimer = 2.5f

        val comboBonus = currentCombo * 50
        val points = 200 + comboBonus
        score += points

        hitStopTimer = 0.04f
        screenShake = 6f
        soundSynth.playSlash()
        soundSynth.playCombo(currentCombo)

        emitBurstParticles(enemy.x, enemy.y, enemy.color, 14)

        if (currentCombo > 1) {
            val comboText = if (currentCombo >= 10) "FRENZY x$currentCombo!" else "x$currentCombo COMBO"
            val textColor = when {
                currentCombo >= 10 -> Color(0xFFFF0055)
                currentCombo >= 5 -> Color(0xFFFFD700)
                else -> Color(0xFF00E5FF)
            }
            addFloatingText(enemy.x, enemy.y - 30f, comboText, textColor)

            if (currentCombo == 5 || currentCombo == 10 || currentCombo == 20) {
                voiceEngine?.speakGameMoment(GameVoiceMoment.HIGH_COMBO)
            }
        }

        // Spawn energy shard at defeated position
        val shardType = when {
            Random.nextFloat() < 0.08f && player.hp < player.maxHp -> ShardType.HEALTH_HEART
            Random.nextFloat() < 0.20f -> ShardType.ENERGY_GOLD
            else -> ShardType.ENERGY_CYAN
        }
        shards.add(
            Shard(
                id = nextEntityId++,
                type = shardType,
                x = enemy.x,
                y = enemy.y
            )
        )
    }

    private fun defeatBoss() {
        val b = boss ?: return
        emitBurstParticles(b.x, b.y, Color(0xFFFFD700), 50)
        soundSynth.playNova()
        addFloatingText(b.x, b.y, "BOSS DEFEATED! +10,000", Color(0xFFFFD700))
        score += 10000
        boss = null
        isVictory = true
        isGameOver = true
        voiceEngine?.speakGameMoment(GameVoiceMoment.VICTORY)
    }

    private fun damagePlayer(amount: Int) {
        if (player.invulnerableTime > 0f) return

        player.hp -= amount
        player.invulnerableTime = 1.2f
        currentCombo = 0
        screenShake = 14f
        hitStopTimer = 0.08f
        soundSynth.playHit()
        emitBurstParticles(player.x, player.y, Color.Red, 20)

        if (player.hp <= 0) {
            player.hp = 0
            isGameOver = true
            soundSynth.playGameOver()
            voiceEngine?.speakGameMoment(GameVoiceMoment.DEFEAT)
        }
    }

    private fun updateEffects(dt: Float) {
        // Particles
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) pIt.remove()
        }

        // Slashes
        val sIt = slashes.iterator()
        while (sIt.hasNext()) {
            val s = sIt.next()
            s.life -= dt
            if (s.life <= 0f) sIt.remove()
        }

        // Nova waves
        val nIt = novaWaves.iterator()
        while (nIt.hasNext()) {
            val n = nIt.next()
            n.life -= dt
            n.currentRadius += (n.maxRadius / n.maxLife) * dt
            if (n.life <= 0f) nIt.remove()
        }

        // Floating texts
        val tIt = floatingTexts.iterator()
        while (tIt.hasNext()) {
            val t = tIt.next()
            t.life -= dt
            t.y -= 45f * dt
            if (t.life <= 0f) tIt.remove()
        }
    }

    private fun emitBurstParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = Random.nextFloat() * 320f + 60f
            val maxLife = Random.nextFloat() * 0.35f + 0.15f
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    radius = Random.nextFloat() * 5f + 3f,
                    life = maxLife,
                    maxLife = maxLife,
                    isGlow = true
                )
            )
        }
    }

    private fun emitTrailParticle(x: Float, y: Float, trailType: TrailType) {
        val maxLife = 0.28f
        val color = when (trailType) {
            TrailType.CYBER_SPARKS -> Color(player.skin?.auraColorHex ?: 0xFF00F5FF)
            TrailType.PLASMA_FLAME -> if (Random.nextBoolean()) Color(0xFFFF9100) else Color(0xFFFF1744)
            TrailType.RAINBOW_DUST -> listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.Green).random()
            TrailType.VOID_SMOKE -> Color(0xFF7C4DFF)
            TrailType.NEON_STREAM -> Color(player.skin?.secondaryColorHex ?: 0xFF8A2BE2)
        }
        particles.add(
            GameParticle(
                x = x + (Random.nextFloat() - 0.5f) * 16f,
                y = y + (Random.nextFloat() - 0.5f) * 16f,
                vx = (Random.nextFloat() - 0.5f) * 40f,
                vy = (Random.nextFloat() - 0.5f) * 40f,
                color = color,
                radius = Random.nextFloat() * 6f + 2f,
                life = maxLife,
                maxLife = maxLife
            )
        )
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                x = x,
                y = y,
                text = text,
                color = color
            )
        )
    }
}
