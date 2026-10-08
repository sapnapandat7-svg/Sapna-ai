package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.AssistantState
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonMint

@Composable
fun MicrophoneOrbButton(
    assistantState: AssistantState,
    soundLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_orb_pulse")

    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_ring"
    )

    val spinAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_anim"
    )

    val dynamicScale = remember { Animatable(1f) }
    LaunchedEffect(soundLevel, assistantState) {
        if (assistantState == AssistantState.LISTENING) {
            val target = 1f + (soundLevel * 0.25f)
            dynamicScale.animateTo(target, tween(80))
        } else {
            dynamicScale.animateTo(1f, tween(150))
        }
    }

    val glowColor = when (assistantState) {
        AssistantState.LISTENING -> NeonMint
        AssistantState.THINKING -> ElectricBlue
        AssistantState.SPEAKING -> JarvisCyanBright
        AssistantState.ERROR -> NeonCoral
        AssistantState.IDLE -> JarvisCyan
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .testTag("microphone_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring when listening or speaking
        if (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseRing)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.25f))
            )
        }

        // Second subtle ambient glow
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        // Main core button
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(dynamicScale.value)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(glowColor, glowColor.copy(alpha = 0.85f))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = CyberBlack),
                    onClick = onClick
                )
                .testTag("microphone_button"),
            contentAlignment = Alignment.Center
        ) {
            when (assistantState) {
                AssistantState.THINKING -> {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Thinking",
                        tint = CyberBlack,
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(spinAnim)
                    )
                }
                AssistantState.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speaking",
                        tint = CyberBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = CyberBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
