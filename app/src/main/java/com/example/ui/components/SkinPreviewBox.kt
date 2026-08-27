package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.models.SkinEntity
import com.example.game.renderer.CharacterRenderer
import java.io.File

@Composable
fun SkinPreviewBox(
    skin: SkinEntity?,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    radiusRatio: Float = 0.32f
) {
    val context = LocalContext.current
    var customBitmap by remember(skin?.id, skin?.galleryFilePath, skin?.builtInDrawableResName) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(skin?.id, skin?.galleryFilePath, skin?.builtInDrawableResName) {
        if (skin?.galleryFilePath != null) {
            val file = File(skin.galleryFilePath)
            if (file.exists()) {
                customBitmap = BitmapFactory.decodeFile(skin.galleryFilePath)
                return@LaunchedEffect
            }
        }
        if (skin?.builtInDrawableResName != null) {
            val resId = context.resources.getIdentifier(
                skin.builtInDrawableResName,
                "drawable",
                context.packageName
            )
            if (resId != 0) {
                customBitmap = BitmapFactory.decodeResource(context.resources, resId)
                return@LaunchedEffect
            }
        }
        customBitmap = null
    }

    val transition = rememberInfiniteTransition(label = "skinPreview")
    val animTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    val auraColor = Color(skin?.auraColorHex ?: 0xFF6750A4)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF3EDF7))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(auraColor.copy(alpha = 0.8f), Color(0xFFCAC4D0))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val radius = this.size.width * radiusRatio

            CharacterRenderer.drawCharacter(
                drawScope = this,
                x = cx,
                y = cy,
                radius = radius,
                skin = skin,
                customBitmap = customBitmap,
                animTime = animTime,
                isDashing = false,
                invulnerableTime = 0f
            )
        }
    }
}
