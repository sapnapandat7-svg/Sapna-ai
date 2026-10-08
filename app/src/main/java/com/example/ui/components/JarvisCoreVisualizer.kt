package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.AssistantState
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonMint
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisCoreVisualizer(
    assistantState: AssistantState,
    soundLevel: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_rotation")

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_slow"
    )

    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_fast"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val reactiveRms = remember { Animatable(0f) }
    LaunchedEffect(soundLevel) {
        reactiveRms.animateTo(
            targetValue = soundLevel.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
        )
    }

    val primaryAccent = when (assistantState) {
        AssistantState.IDLE -> JarvisCyan
        AssistantState.LISTENING -> NeonMint
        AssistantState.THINKING -> ElectricBlue
        AssistantState.SPEAKING -> JarvisCyanBright
        AssistantState.ERROR -> NeonCoral
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.minDimension / 2f) - 8.dp.toPx()
            val rmsBoost = reactiveRms.value * 20.dp.toPx()

            // 1. Outer Tech Ring (Dashed Arc)
            rotate(rotationSlow, pivot = center) {
                drawCircle(
                    color = primaryAccent.copy(alpha = 0.25f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f), 0f)
                    )
                )
            }

            // 2. Middle Ring with Counter-Rotation
            val midRadius = (maxRadius * 0.78f) + (if (assistantState == AssistantState.LISTENING) rmsBoost * 0.5f else 0f)
            rotate(rotationFast, pivot = center) {
                drawCircle(
                    color = primaryAccent.copy(alpha = 0.45f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 25f), 0f)
                    )
                )

                // 4 Orbiting Tech Nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90).toDouble())
                    val nodeX = center.x + (midRadius * cos(angle)).toFloat()
                    val nodeY = center.y + (midRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = primaryAccent,
                        radius = 3.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 3. Audio Reactive Sound Waves (Listening or Speaking mode)
            if (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING) {
                val waveCount = 24
                val waveBaseRadius = maxRadius * 0.55f
                val waveMaxDelta = 16.dp.toPx() + rmsBoost

                for (i in 0 until waveCount) {
                    val angle = Math.toRadians((i * (360.0 / waveCount)).toDouble())
                    val barHeight = waveMaxDelta * (0.3f + 0.7f * ((i % 3 + 1) / 3f) * (if (assistantState == AssistantState.LISTENING) (reactiveRms.value + 0.2f) else pulseScale))
                    val startR = waveBaseRadius
                    val endR = waveBaseRadius + barHeight

                    val startX = center.x + (startR * cos(angle)).toFloat()
                    val startY = center.y + (startR * sin(angle)).toFloat()
                    val endX = center.x + (endR * cos(angle)).toFloat()
                    val endY = center.y + (endR * sin(angle)).toFloat()

                    drawLine(
                        color = primaryAccent.copy(alpha = 0.8f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 4. Inner Glowing Orb Core
            val coreRadius = (maxRadius * 0.38f * pulseScale) + (rmsBoost * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryAccent.copy(alpha = 0.9f),
                        primaryAccent.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.5f
                ),
                radius = coreRadius * 1.5f,
                center = center
            )

            // 5. Solid Core Ring
            drawCircle(
                color = primaryAccent,
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Inner Core Center Pip
            drawCircle(
                color = primaryAccent,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
