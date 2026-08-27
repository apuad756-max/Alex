package com.example.game.renderer

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.data.models.AccessoryType
import com.example.data.models.BodyShape
import com.example.data.models.SkinEntity
import com.example.data.models.WeaponFx
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CharacterRenderer {

    fun drawCharacter(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        radius: Float,
        skin: SkinEntity?,
        customBitmap: Bitmap?,
        animTime: Float,
        isDashing: Boolean = false,
        invulnerableTime: Float = 0f
    ) {
        val auraColor = Color(skin?.auraColorHex ?: 0xFF00F5FF)
        val secColor = Color(skin?.secondaryColorHex ?: 0xFF8A2BE2)
        val bodyShape = skin?.bodyShape ?: BodyShape.ORB
        val accessory = skin?.accessory ?: AccessoryType.NONE
        val weapon = skin?.weaponFx ?: WeaponFx.BLADE_SLASH

        // Invulnerability flicker
        if (invulnerableTime > 0f && (animTime * 20).toInt() % 2 == 0) {
            return
        }

        with(drawScope) {
            val center = Offset(x, y)

            // 1. Aura Outer Glow
            val pulse = 1f + sin(animTime * 6f) * 0.12f
            val auraRadius = radius * 1.55f * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.45f), secColor.copy(alpha = 0.12f), Color.Transparent),
                    center = center,
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = center
            )

            // 2. Wings Accessory (Back layer)
            if (accessory == AccessoryType.ANGEL_WINGS) {
                drawWings(center, radius, auraColor, animTime)
            }

            // 3. Body Rendering (Gallery Sprite or Geometric Shape)
            if (customBitmap != null && (bodyShape == BodyShape.CUSTOM_GALLERY || skin?.isCustomGallery == true)) {
                drawGalleryAvatar(center, radius, customBitmap, auraColor)
            } else {
                drawGeometricBody(center, radius, bodyShape, auraColor, secColor, animTime)
            }

            // 4. Inner Core Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = radius * 0.28f,
                center = center
            )

            // 5. Fore accessories (Visor, Crown, Horns, Bandana, Orbiters)
            when (accessory) {
                AccessoryType.CYBER_VISOR -> drawVisor(center, radius, secColor)
                AccessoryType.NEON_CROWN -> drawCrown(center, radius, Color(0xFFFFD700), animTime)
                AccessoryType.ORBITING_ORBS -> drawOrbitingOrbs(center, radius, auraColor, animTime)
                AccessoryType.CYBER_HORNS -> drawHorns(center, radius, Color(0xFFFF1744))
                AccessoryType.NINJA_BANDANA -> drawBandana(center, radius, Color(0xFFFF0055), animTime)
                else -> {}
            }

            // 6. Weapon / Blade Holster / Spinning Aura
            drawWeaponGlow(center, radius, weapon, auraColor, animTime, isDashing)
        }
    }

    private fun DrawScope.drawGalleryAvatar(
        center: Offset,
        radius: Float,
        bitmap: Bitmap,
        auraColor: Color
    ) {
        val sizePx = (radius * 2f).toInt()
        val halfSize = radius

        // Draw circular border / clipping container
        drawCircle(
            color = auraColor,
            radius = radius + 3f,
            center = center,
            style = Stroke(width = 3.5f)
        )

        // Draw clipped / scaled bitmap
        val imageBitmap = bitmap.asImageBitmap()
        drawImage(
            image = imageBitmap,
            dstOffset = IntOffset((center.x - halfSize).toInt(), (center.y - halfSize).toInt()),
            dstSize = IntSize(sizePx, sizePx)
        )

        // Subtle holographic gloss line
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(center.x - radius * 0.7f, center.y - radius * 0.5f),
            end = Offset(center.x + radius * 0.7f, center.y - radius * 0.2f),
            strokeWidth = 2.5f
        )
    }

    private fun DrawScope.drawGeometricBody(
        center: Offset,
        radius: Float,
        shape: BodyShape,
        primary: Color,
        secondary: Color,
        animTime: Float
    ) {
        val brush = Brush.linearGradient(
            colors = listOf(primary, secondary),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        )

        when (shape) {
            BodyShape.ORB -> {
                drawCircle(brush = brush, radius = radius, center = center)
                drawCircle(color = Color.White.copy(alpha = 0.85f), radius = radius, center = center, style = Stroke(width = 2.5f))
            }
            BodyShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius * 1.2f)
                    lineTo(center.x + radius * 0.9f, center.y)
                    lineTo(center.x, center.y + radius * 1.2f)
                    lineTo(center.x - radius * 0.9f, center.y)
                    close()
                }
                drawPath(path = path, brush = brush)
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
            }
            BodyShape.SHURIKEN -> {
                rotate(degrees = animTime * 120f, pivot = center) {
                    val path = Path().apply {
                        val spikes = 4
                        for (i in 0 until spikes * 2) {
                            val r = if (i % 2 == 0) radius * 1.35f else radius * 0.45f
                            val ang = (i * PI / spikes).toFloat()
                            val px = center.x + cos(ang) * r
                            val py = center.y + sin(ang) * r
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path = path, brush = brush)
                    drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
                }
            }
            BodyShape.HEXAGON -> {
                val path = Path().apply {
                    for (i in 0 until 6) {
                        val ang = (i * 60f * PI / 180f).toFloat()
                        val px = center.x + cos(ang) * radius
                        val py = center.y + sin(ang) * radius
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(path = path, brush = brush)
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
            }
            BodyShape.SHIELD -> {
                val path = Path().apply {
                    moveTo(center.x - radius * 0.8f, center.y - radius)
                    lineTo(center.x + radius * 0.8f, center.y - radius)
                    lineTo(center.x + radius * 0.7f, center.y + radius * 0.3f)
                    lineTo(center.x, center.y + radius * 1.2f)
                    lineTo(center.x - radius * 0.7f, center.y + radius * 0.3f)
                    close()
                }
                drawPath(path = path, brush = brush)
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
            }
            BodyShape.STAR -> {
                rotate(degrees = -animTime * 90f, pivot = center) {
                    val path = Path().apply {
                        val points = 5
                        for (i in 0 until points * 2) {
                            val r = if (i % 2 == 0) radius * 1.3f else radius * 0.55f
                            val ang = (i * PI / points - PI / 2).toFloat()
                            val px = center.x + cos(ang) * r
                            val py = center.y + sin(ang) * r
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path = path, brush = brush)
                    drawPath(path = path, color = Color.White, style = Stroke(width = 2f))
                }
            }
            BodyShape.CUSTOM_GALLERY -> {
                drawCircle(brush = brush, radius = radius, center = center)
            }
        }
    }

    private fun DrawScope.drawWings(center: Offset, radius: Float, color: Color, animTime: Float) {
        val wingFlap = sin(animTime * 8f) * 0.2f
        val wingPathLeft = Path().apply {
            moveTo(center.x - radius * 0.3f, center.y)
            cubicTo(
                center.x - radius * 2.2f, center.y - radius * (1.2f + wingFlap),
                center.x - radius * 2.6f, center.y + radius * 0.5f,
                center.x - radius * 0.5f, center.y + radius * 0.6f
            )
            close()
        }
        val wingPathRight = Path().apply {
            moveTo(center.x + radius * 0.3f, center.y)
            cubicTo(
                center.x + radius * 2.2f, center.y - radius * (1.2f + wingFlap),
                center.x + radius * 2.6f, center.y + radius * 0.5f,
                center.x + radius * 0.5f, center.y + radius * 0.6f
            )
            close()
        }
        drawPath(path = wingPathLeft, color = color.copy(alpha = 0.55f))
        drawPath(path = wingPathLeft, color = Color.White, style = Stroke(width = 2f))
        drawPath(path = wingPathRight, color = color.copy(alpha = 0.55f))
        drawPath(path = wingPathRight, color = Color.White, style = Stroke(width = 2f))
    }

    private fun DrawScope.drawVisor(center: Offset, radius: Float, color: Color) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - radius * 0.7f, center.y - radius * 0.35f),
            size = Size(radius * 1.4f, radius * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        drawLine(
            color = Color.White,
            start = Offset(center.x - radius * 0.5f, center.y - radius * 0.1f),
            end = Offset(center.x + radius * 0.5f, center.y - radius * 0.1f),
            strokeWidth = 2.5f
        )
    }

    private fun DrawScope.drawCrown(center: Offset, radius: Float, color: Color, animTime: Float) {
        val crownBob = sin(animTime * 4f) * 3f
        val crownPath = Path().apply {
            val topY = center.y - radius * 1.4f + crownBob
            val baseLeft = center.x - radius * 0.65f
            val baseRight = center.x + radius * 0.65f
            val baseY = center.y - radius * 0.7f + crownBob

            moveTo(baseLeft, baseY)
            lineTo(baseLeft - 4f, topY + 4f)
            lineTo(center.x - radius * 0.25f, baseY - 6f)
            lineTo(center.x, topY - 4f)
            lineTo(center.x + radius * 0.25f, baseY - 6f)
            lineTo(baseRight + 4f, topY + 4f)
            lineTo(baseRight, baseY)
            close()
        }
        drawPath(path = crownPath, color = color)
        drawPath(path = crownPath, color = Color.White, style = Stroke(width = 2f))
    }

    private fun DrawScope.drawOrbitingOrbs(center: Offset, radius: Float, color: Color, animTime: Float) {
        val count = 3
        val orbitRadius = radius * 1.7f
        for (i in 0 until count) {
            val ang = animTime * 4f + (i * 2f * PI / count).toFloat()
            val ox = center.x + cos(ang) * orbitRadius
            val oy = center.y + sin(ang) * orbitRadius
            drawCircle(color = color, radius = 6f, center = Offset(ox, oy))
            drawCircle(color = Color.White, radius = 3f, center = Offset(ox, oy))
        }
    }

    private fun DrawScope.drawHorns(center: Offset, radius: Float, color: Color) {
        val leftHorn = Path().apply {
            moveTo(center.x - radius * 0.6f, center.y - radius * 0.4f)
            quadraticTo(
                center.x - radius * 1.2f, center.y - radius * 1.4f,
                center.x - radius * 0.8f, center.y - radius * 1.6f
            )
            quadraticTo(
                center.x - radius * 0.4f, center.y - radius * 1.1f,
                center.x - radius * 0.3f, center.y - radius * 0.7f
            )
            close()
        }
        val rightHorn = Path().apply {
            moveTo(center.x + radius * 0.6f, center.y - radius * 0.4f)
            quadraticTo(
                center.x + radius * 1.2f, center.y - radius * 1.4f,
                center.x + radius * 0.8f, center.y - radius * 1.6f
            )
            quadraticTo(
                center.x + radius * 0.4f, center.y - radius * 1.1f,
                center.x + radius * 0.3f, center.y - radius * 0.7f
            )
            close()
        }
        drawPath(leftHorn, color = color)
        drawPath(rightHorn, color = color)
    }

    private fun DrawScope.drawBandana(center: Offset, radius: Float, color: Color, animTime: Float) {
        val wave = sin(animTime * 6f) * 6f
        val ribbonPath = Path().apply {
            moveTo(center.x - radius * 0.7f, center.y - radius * 0.2f)
            lineTo(center.x - radius * 1.6f, center.y - radius * 0.1f + wave)
            lineTo(center.x - radius * 1.8f, center.y + radius * 0.4f + wave)
            lineTo(center.x - radius * 0.6f, center.y + radius * 0.2f)
            close()
        }
        drawPath(ribbonPath, color = color)
    }

    private fun DrawScope.drawWeaponGlow(
        center: Offset,
        radius: Float,
        weapon: WeaponFx,
        color: Color,
        animTime: Float,
        isDashing: Boolean
    ) {
        val bladeDist = radius * 1.35f
        val rot = if (isDashing) animTime * 720f else animTime * 90f

        rotate(degrees = rot, pivot = center) {
            when (weapon) {
                WeaponFx.BLADE_SLASH -> {
                    drawLine(
                        color = color,
                        start = Offset(center.x - bladeDist, center.y),
                        end = Offset(center.x + bladeDist, center.y),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color.White, radius = 4f, center = Offset(center.x + bladeDist, center.y))
                }
                WeaponFx.DUAL_SABERS -> {
                    drawLine(
                        color = color,
                        start = Offset(center.x, center.y - bladeDist),
                        end = Offset(center.x, center.y + bladeDist),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x - bladeDist, center.y),
                        end = Offset(center.x + bladeDist, center.y),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }
                WeaponFx.STAR_SHURIKEN -> {
                    drawCircle(
                        color = color.copy(alpha = 0.6f),
                        radius = bladeDist,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }
                WeaponFx.PLASMA_PULSE -> {
                    drawCircle(
                        color = color.copy(alpha = 0.35f),
                        radius = bladeDist * 1.2f,
                        center = center,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
