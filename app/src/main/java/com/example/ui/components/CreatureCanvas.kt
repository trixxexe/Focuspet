package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.CreatureState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Data class representing a floating sparkle particle for THRIVING state
data class SparkleParticle(
    val id: Int,
    var x: Float,
    var y: Float,
    val speedY: Float,
    val speedX: Float,
    val maxRadius: Float,
    var alpha: Float,
    val fadeSpeed: Float
)

@Composable
fun CreatureCanvas(
    creatureState: CreatureState,
    modifier: Modifier = Modifier
) {
    // Choose animation speed and amplitude based on creature state
    val animDuration = when (creatureState) {
        CreatureState.WEAKENING -> 3000
        CreatureState.RESTING -> 2000
        CreatureState.ENERGIZED -> 1500
        CreatureState.THRIVING -> 1000
    }

    val bobRange = when (creatureState) {
        CreatureState.WEAKENING -> 4.dp
        CreatureState.RESTING -> 8.dp
        CreatureState.ENERGIZED -> 10.dp
        CreatureState.THRIVING -> 12.dp
    }

    val scaleRange = when (creatureState) {
        CreatureState.WEAKENING -> 0.01f
        CreatureState.RESTING -> 0.02f
        CreatureState.ENERGIZED -> 0.03f
        CreatureState.THRIVING -> 0.04f
    }

    val opacity = if (creatureState == CreatureState.WEAKENING) 0.7f else 1.0f

    // Setup infinite transition for bobbing and scaling
    val infiniteTransition = rememberInfiniteTransition(label = "creature_idle")
    
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animDuration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_offset"
    )

    val scaleOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animDuration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_offset"
    )

    // Glowing heartbeat pulse for ACTIVE state or high vitality states
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Maintain a list of floating sparkles for the THRIVING state
    var sparkles by remember { mutableStateOf(emptyList<SparkleParticle>()) }
    var nextParticleId by remember { mutableStateOf(0) }

    // Update sparkles in a side effect loop
    LaunchedEffect(creatureState) {
        if (creatureState == CreatureState.THRIVING) {
            while (isActive) {
                // Periodically spawn new sparkle
                if (sparkles.size < 15 && Random.nextFloat() < 0.3f) {
                    sparkles = sparkles + SparkleParticle(
                        id = nextParticleId++,
                        x = Random.nextFloat() * 200f - 100f, // relative to center
                        y = 80f, // spawn at feet level
                        speedY = Random.nextFloat() * 2f + 1f,
                        speedX = (Random.nextFloat() * 2f - 1f) * 0.5f,
                        maxRadius = Random.nextFloat() * 4f + 2f,
                        alpha = 1f,
                        fadeSpeed = Random.nextFloat() * 0.02f + 0.01f
                    )
                }

                // Update existing sparkles
                sparkles = sparkles.mapNotNull { particle ->
                    particle.y -= particle.speedY
                    particle.x += particle.speedX
                    particle.alpha -= particle.fadeSpeed
                    if (particle.alpha <= 0) null else particle
                }

                delay(30)
            }
        } else {
            sparkles = emptyList()
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Adjust base size and Y stretching based on state
            val baseRadius = size.minDimension / 4f
            val scaleX = 1f + (scaleOffset * scaleRange)
            // WEAKENED/WEAKENING state scales Y downwards slightly (droop)
            val baseScaleY = if (creatureState == CreatureState.WEAKENING) 0.85f else 1.0f
            val scaleY = baseScaleY + (scaleOffset * scaleRange)

            val currentBob = bobOffset * bobRange.toPx()

            // Main creature center position
            val creatureX = centerX
            val creatureY = centerY + currentBob

            // Draw thriving ambient glow behind
            if (creatureState == CreatureState.THRIVING) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x336C63FF),
                            Color(0x116C63FF),
                            Color.Transparent
                        ),
                        center = Offset(creatureX, creatureY),
                        radius = baseRadius * 2.2f
                    ),
                    center = Offset(creatureX, creatureY),
                    radius = baseRadius * 2.2f
                )
            }

            // Draw layered glowing rings (hardware accelerated, highly reliable)
            val glowColor = when (creatureState) {
                CreatureState.WEAKENING -> Color(0x224A4A5A)
                CreatureState.RESTING -> Color(0x336C63FF)
                CreatureState.ENERGIZED -> Color(0x556C63FF)
                CreatureState.THRIVING -> Color(0x886C63FF)
            }

            val finalGlowRadius = baseRadius * (1f + (glowPulse * 0.12f))

            // Draw sparkles for thriving state
            if (creatureState == CreatureState.THRIVING) {
                sparkles.forEach { sparkle ->
                    drawCircle(
                        color = Color(0xFFE2E0FF).copy(alpha = sparkle.alpha),
                        radius = sparkle.maxRadius * sparkle.alpha,
                        center = Offset(creatureX + sparkle.x, creatureY - sparkle.y)
                    )
                }
            }

            // Create Path for organic blob body with feet
            val bodyPath = Path().apply {
                val rx = baseRadius * scaleX
                val ry = baseRadius * scaleY

                // Draw blob using cubic curves
                // Control points offset relative to radii to make a cute rounded shape
                val ctrlX = rx * 0.55f
                val ctrlY = ry * 0.55f

                moveTo(creatureX, creatureY - ry) // top center
                
                // Top-Right quadrant
                cubicTo(
                    creatureX + ctrlX, creatureY - ry,
                    creatureX + rx, creatureY - ctrlY,
                    creatureX + rx, creatureY
                )

                // Bottom-Right quadrant to Right Foot
                val footOffset = rx * 0.3f
                cubicTo(
                    creatureX + rx, creatureY + ctrlY,
                    creatureX + rx - footOffset, creatureY + ry - footOffset,
                    creatureX + rx - footOffset, creatureY + ry // Foot peak
                )

                // Middle feet arch
                val innerFootX = rx * 0.2f
                cubicTo(
                    creatureX + rx - footOffset, creatureY + ry,
                    creatureX + innerFootX, creatureY + ry - footOffset,
                    creatureX, creatureY + ry - (ry * 0.1f) // bottom-center recess
                )

                // Left Foot to Bottom-Left quadrant
                cubicTo(
                    creatureX - innerFootX, creatureY + ry - footOffset,
                    creatureX - rx + footOffset, creatureY + ry,
                    creatureX - rx + footOffset, creatureY + ry // Foot peak
                )

                // Left quadrant up
                cubicTo(
                    creatureX - rx + footOffset, creatureY + ry - footOffset,
                    creatureX - rx, creatureY + ctrlY,
                    creatureX - rx, creatureY
                )

                // Top-Left quadrant
                cubicTo(
                    creatureX - rx, creatureY - ctrlY,
                    creatureX - ctrlX, creatureY - ry,
                    creatureX, creatureY - ry
                )
                close()
            }

            // Draw glowing outline (using layered strokes for stunning glow effect)
            val outerStrokeWidths = listOf(20f, 12f, 6f)
            val outerStrokeOpacities = listOf(0.12f, 0.25f, 0.6f)
            outerStrokeWidths.forEachIndexed { idx, widthPx ->
                drawPath(
                    path = bodyPath,
                    color = glowColor.copy(alpha = outerStrokeOpacities[idx] * opacity),
                    style = Stroke(width = widthPx, cap = StrokeCap.Round)
                )
            }

            // Draw BlurMaskFilter glow via native Canvas as instructed!
            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.parseColor("#6C63FF")
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 4f
                    isAntiAlias = true
                    // Apply blur mask filter for a beautiful glow outline
                    maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
                }
                
                // Adjust paint opacity based on state
                paint.alpha = (outerStrokeOpacities.last() * 255 * opacity).toInt()
                
                canvas.nativeCanvas.drawPath(bodyPath.asAndroidPath(), paint)
            }

            // Base fill color #1A1A2E with requested opacity
            drawPath(
                path = bodyPath,
                color = Color(0xFF1A1A2E).copy(alpha = opacity),
                style = Fill
            )

            // Inner outline
            drawPath(
                path = bodyPath,
                color = Color(0xFF6C63FF).copy(alpha = 0.9f * opacity),
                style = Stroke(width = 3.5f)
            )

            // Draw One large circular eye in center
            val eyeRadius = baseRadius * 0.35f
            // Eye center slightly above body center
            val eyeX = creatureX
            val eyeY = creatureY - (baseRadius * 0.1f)

            // Eye state properties
            val irisScale = when (creatureState) {
                CreatureState.WEAKENING -> 0.4f  // Droopy, small pupil
                CreatureState.RESTING -> 0.6f    // Normal
                CreatureState.ENERGIZED -> 0.7f  // Brighter
                CreatureState.THRIVING -> 0.8f   // Wide alert
            }

            // Eye height scaling for droop/half-closed in WEAKENING
            val eyeHeightScale = if (creatureState == CreatureState.WEAKENING) 0.5f else 1.0f

            // Sclera (White background)
            val eyePath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        left = eyeX - eyeRadius,
                        top = eyeY - (eyeRadius * eyeHeightScale),
                        right = eyeX + eyeRadius,
                        bottom = eyeY + (eyeRadius * eyeHeightScale)
                    )
                )
            }

            drawPath(
                path = eyePath,
                color = Color.White.copy(alpha = opacity)
            )

            // Iris (glowing center pupil)
            val irisRadius = eyeRadius * irisScale
            val irisColor = when (creatureState) {
                CreatureState.WEAKENING -> Color(0xFF4A4A5A)
                CreatureState.RESTING -> Color(0xFF6C63FF)
                CreatureState.ENERGIZED -> Color(0xFF8C84FF)
                CreatureState.THRIVING -> Color(0xFFB4AFFF)
            }

            drawCircle(
                color = irisColor.copy(alpha = opacity),
                radius = irisRadius,
                center = Offset(eyeX, eyeY)
            )

            // Pupil Center (dark center dot)
            drawCircle(
                color = Color(0xFF0A0A0F).copy(alpha = opacity),
                radius = irisRadius * 0.4f,
                center = Offset(eyeX, eyeY)
            )

            // Specular Highlight dot (little reflective white spark in eye)
            drawCircle(
                color = Color.White.copy(alpha = opacity),
                radius = eyeRadius * 0.15f,
                center = Offset(eyeX - (eyeRadius * 0.3f), eyeY - (eyeRadius * 0.3f * eyeHeightScale))
            )
        }
    }
}
