package com.example.game.renderer

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.engine.Enemy
import com.example.game.engine.EnemyType
import com.example.game.engine.GameEngine
import com.example.game.engine.Shard
import com.example.game.engine.ShardType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GameCanvasRenderer {

    private val textPaint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun render(
        drawScope: DrawScope,
        engine: GameEngine,
        animTime: Float
    ) {
        with(drawScope) {
            val width = size.width
            val height = size.height

            // 1. Sleek minimalist arena background & cyber grid
            drawBackground(width, height, animTime)

            // 2. Nova shockwaves
            for (nova in engine.novaWaves) {
                val alpha = (nova.life / nova.maxLife).coerceIn(0f, 1f)
                drawCircle(
                    color = nova.color.copy(alpha = alpha * 0.75f),
                    radius = nova.currentRadius,
                    center = Offset(nova.x, nova.y),
                    style = Stroke(width = 12f * alpha + 2f)
                )
            }

            // 3. Shards & Collectibles
            for (shard in engine.shards) {
                drawShard(shard, animTime)
            }

            // 4. Enemy Laser Warnings & Laser Beams
            for (enemy in engine.enemies) {
                if (enemy.type == EnemyType.LASER_ORB) {
                    if (enemy.isWarning) {
                        drawLaserWarning(enemy)
                    } else if (enemy.isFiringLaser) {
                        drawLaserBeam(enemy)
                    }
                }
            }

            // 5. Enemies
            for (enemy in engine.enemies) {
                drawEnemy(enemy, animTime)
            }

            // 6. Boss
            engine.boss?.let { b ->
                drawBoss(b, animTime)
            }

            // 7. Player Character with Customized Skin
            CharacterRenderer.drawCharacter(
                drawScope = this,
                x = engine.player.x,
                y = engine.player.y,
                radius = engine.player.radius,
                skin = engine.player.skin,
                customBitmap = engine.player.customBitmap,
                animTime = animTime,
                isDashing = engine.player.isDashing,
                invulnerableTime = engine.player.invulnerableTime
            )

            // 8. Slash Arcs
            for (slash in engine.slashes) {
                val alpha = (slash.life / slash.maxLife).coerceIn(0f, 1f)
                rotate(degrees = slash.angle, pivot = Offset(slash.x, slash.y)) {
                    val arcPath = Path().apply {
                        moveTo(slash.x - slash.radius, slash.y)
                        quadraticTo(
                            slash.x, slash.y - slash.radius * 0.8f,
                            slash.x + slash.radius, slash.y
                        )
                    }
                    drawPath(
                        path = arcPath,
                        color = slash.color.copy(alpha = alpha),
                        style = Stroke(width = 8f * alpha, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = arcPath,
                        color = Color.White.copy(alpha = alpha),
                        style = Stroke(width = 3f * alpha, cap = StrokeCap.Round)
                    )
                }
            }

            // 9. Particles
            for (p in engine.particles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius * (0.4f + 0.6f * alpha),
                    center = Offset(p.x, p.y)
                )
            }

            // 10. Floating Texts
            for (ft in engine.floatingTexts) {
                val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
                textPaint.color = ft.color.copy(alpha = alpha).toArgb()
                textPaint.textSize = 34f
                drawContext.canvas.nativeCanvas.drawText(
                    ft.text,
                    ft.x,
                    ft.y,
                    textPaint
                )
            }
        }
    }

    private fun DrawScope.drawBackground(width: Float, height: Float, animTime: Float) {
        // Deep obsidian canvas
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF090D16), Color(0xFF04060A))
            ),
            size = Size(width, height)
        )

        // Moving grid lines
        val gridSize = 80f
        val offsetY = (animTime * 40f) % gridSize

        var y = offsetY
        while (y < height) {
            drawLine(
                color = Color(0x1200E5FF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }

        var x = 0f
        while (x < width) {
            drawLine(
                color = Color(0x1200E5FF),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Arena boundary glow lines
        drawLine(
            color = Color(0x3300F5FF),
            start = Offset(20f, 80f),
            end = Offset(width - 20f, 80f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0x3300F5FF),
            start = Offset(20f, height - 100f),
            end = Offset(width - 20f, height - 100f),
            strokeWidth = 2f
        )
    }

    private fun DrawScope.drawShard(shard: Shard, animTime: Float) {
        val pulse = 1f + sin(animTime * 8f + shard.id) * 0.2f
        val rad = shard.radius * pulse
        val center = Offset(shard.x, shard.y)

        val color = when (shard.type) {
            ShardType.ENERGY_CYAN -> Color(0xFF00E5FF)
            ShardType.ENERGY_GOLD -> Color(0xFFFFD700)
            ShardType.HEALTH_HEART -> Color(0xFFFF1744)
            ShardType.OVERDRIVE_STAR -> Color(0xFFD500F9)
        }

        // Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.6f), Color.Transparent),
                center = center,
                radius = rad * 2f
            ),
            radius = rad * 2f,
            center = center
        )

        // Shape (Diamond prism)
        val path = Path().apply {
            moveTo(center.x, center.y - rad)
            lineTo(center.x + rad * 0.7f, center.y)
            lineTo(center.x, center.y + rad)
            lineTo(center.x - rad * 0.7f, center.y)
            close()
        }
        drawPath(path, color = color)
        drawPath(path, color = Color.White, style = Stroke(width = 2f))
    }

    private fun DrawScope.drawEnemy(enemy: Enemy, animTime: Float) {
        val center = Offset(enemy.x, enemy.y)
        val alpha = enemy.alpha.coerceIn(0f, 1f)

        when (enemy.type) {
            EnemyType.DRONE -> {
                // Triangle drone with glowing eye
                rotate(degrees = animTime * 180f, pivot = center) {
                    val path = Path().apply {
                        moveTo(center.x, center.y - enemy.radius)
                        lineTo(center.x + enemy.radius * 0.9f, center.y + enemy.radius * 0.8f)
                        lineTo(center.x - enemy.radius * 0.9f, center.y + enemy.radius * 0.8f)
                        close()
                    }
                    drawPath(path, color = enemy.color.copy(alpha = alpha))
                    drawPath(path, color = Color.White.copy(alpha = alpha), style = Stroke(width = 2f))
                }
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 4f, center = center)
            }
            EnemyType.SPIKE_BOUNCER -> {
                // Rotating spiked wheel
                rotate(degrees = animTime * 240f, pivot = center) {
                    val path = Path().apply {
                        val spikes = 6
                        for (i in 0 until spikes * 2) {
                            val r = if (i % 2 == 0) enemy.radius * 1.25f else enemy.radius * 0.65f
                            val ang = (i * PI / spikes).toFloat()
                            val px = center.x + cos(ang) * r
                            val py = center.y + sin(ang) * r
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = enemy.color.copy(alpha = alpha))
                    drawPath(path, color = Color.White.copy(alpha = alpha), style = Stroke(width = 2f))
                }
            }
            EnemyType.LASER_ORB -> {
                drawCircle(
                    color = enemy.color.copy(alpha = alpha * 0.35f),
                    radius = enemy.radius * 1.4f,
                    center = center
                )
                drawCircle(
                    color = enemy.color.copy(alpha = alpha),
                    radius = enemy.radius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = enemy.radius * 0.4f,
                    center = center
                )
            }
            EnemyType.PHANTOM_SPECTRE -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(enemy.color.copy(alpha = alpha), Color.Transparent),
                        center = center,
                        radius = enemy.radius * 1.5f
                    ),
                    radius = enemy.radius * 1.5f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = enemy.radius * 0.5f,
                    center = center
                )
            }
            EnemyType.BOSS_TITAN -> {}
        }
    }

    private fun DrawScope.drawLaserWarning(enemy: Enemy) {
        val rad = (enemy.laserAngle * PI / 180f).toFloat()
        val dirX = cos(rad)
        val dirY = sin(rad)
        val endX = enemy.x + dirX * 1600f
        val endY = enemy.y + dirY * 1600f

        val alpha = (0.25f + enemy.warningProgress * 0.65f).coerceIn(0f, 1f)
        drawLine(
            color = Color(0xFFFF1744).copy(alpha = alpha),
            start = Offset(enemy.x, enemy.y),
            end = Offset(endX, endY),
            strokeWidth = 2f + enemy.warningProgress * 3f
        )
    }

    private fun DrawScope.drawLaserBeam(enemy: Enemy) {
        val rad = (enemy.laserAngle * PI / 180f).toFloat()
        val dirX = cos(rad)
        val dirY = sin(rad)
        val endX = enemy.x + dirX * 1600f
        val endY = enemy.y + dirY * 1600f

        drawLine(
            color = Color(0xFFFF9100).copy(alpha = 0.5f),
            start = Offset(enemy.x, enemy.y),
            end = Offset(endX, endY),
            strokeWidth = 28f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFFF1744),
            start = Offset(enemy.x, enemy.y),
            end = Offset(endX, endY),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(enemy.x, enemy.y),
            end = Offset(endX, endY),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }

    private fun DrawScope.drawBoss(boss: com.example.game.engine.BossData, animTime: Float) {
        val center = Offset(boss.x, boss.y)

        // Boss outer shield ring
        rotate(degrees = animTime * 60f, pivot = center) {
            drawCircle(
                color = Color(0xFFFF0055).copy(alpha = 0.3f),
                radius = boss.radius * 1.4f,
                center = center,
                style = Stroke(width = 6f)
            )
            for (i in 0 until 4) {
                val ang = (i * 90f * PI / 180f).toFloat()
                val bx = center.x + cos(ang) * boss.radius * 1.4f
                val by = center.y + sin(ang) * boss.radius * 1.4f
                drawCircle(color = Color(0xFFFF1744), radius = 10f, center = Offset(bx, by))
            }
        }

        // Boss main body (Octagon core)
        val path = Path().apply {
            for (i in 0 until 8) {
                val ang = (i * 45f * PI / 180f).toFloat()
                val px = center.x + cos(ang) * boss.radius
                val py = center.y + sin(ang) * boss.radius
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(path, brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFF0055), Color(0xFF650020)),
            center = center,
            radius = boss.radius
        ))
        drawPath(path, color = Color.White, style = Stroke(width = 4f))

        // Boss Eye
        drawCircle(color = Color(0xFFFFD700), radius = 16f, center = center)
        drawCircle(color = Color.White, radius = 6f, center = center)
    }
}
