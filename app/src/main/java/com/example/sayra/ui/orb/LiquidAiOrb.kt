package com.example.sayra.ui.orb

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sayra.character.CharacterPerformanceMode
import com.example.sayra.data.model.OrbState
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.DeepIndigo
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantMagenta
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftPink
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidAiOrb(
    orbState: OrbState,
    audioAmplitude: Float, // 0f to 1f (mic or voice)
    intensityMultiplier: Float = 1.0f,
    performanceMode: CharacterPerformanceMode = CharacterPerformanceMode.HIGH,
    size: Dp = 270.dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbMotion")

    // Continuous smooth time phase
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LiquidPhase"
    )

    // Secondary harmonic phase
    val fastPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FastPhase"
    )

    // Breathing scale for Idle
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleBreathing"
    )

    // Target scale and ripple animatables
    val stateScale = remember { Animatable(1f) }
    val successRipple = remember { Animatable(0f) }
    val errorShake = remember { Animatable(0f) }

    LaunchedEffect(orbState) {
        when (orbState) {
            OrbState.IDLE -> {
                stateScale.animateTo(1.0f, tween(500))
            }
            OrbState.LISTENING -> {
                stateScale.animateTo(1.18f, tween(400))
            }
            OrbState.THINKING -> {
                stateScale.animateTo(1.06f, tween(400))
            }
            OrbState.SPEAKING -> {
                stateScale.animateTo(1.12f, tween(300))
            }
            OrbState.ACTION_SUCCESS -> {
                stateScale.animateTo(1.22f, tween(200))
                successRipple.snapTo(0f)
                successRipple.animateTo(1f, tween(600))
                stateScale.animateTo(1.0f, tween(300))
            }
            OrbState.ERROR -> {
                stateScale.animateTo(0.96f, tween(200))
                errorShake.snapTo(1f)
                errorShake.animateTo(0f, tween(450))
                stateScale.animateTo(1.0f, tween(200))
            }
            OrbState.VISION -> {
                stateScale.animateTo(1.08f, tween(400))
            }
            OrbState.AGENT -> {
                stateScale.animateTo(1.14f, tween(400))
            }
            OrbState.IMAGE_GENERATION -> {
                stateScale.animateTo(1.15f, tween(450))
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("liquid_ai_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.65f * intensityMultiplier

            // Compute actual scale factoring in idle breathing and dynamic amplitudes
            val dynamicScale = when (orbState) {
                OrbState.IDLE -> stateScale.value * idleBreathing
                OrbState.LISTENING -> stateScale.value + (audioAmplitude * 0.15f)
                OrbState.SPEAKING -> stateScale.value + (audioAmplitude * 0.12f)
                else -> stateScale.value
            }

            val radius = baseRadius * dynamicScale

            // Error shake offset
            val shakeOffsetX = if (errorShake.value > 0f) {
                sin(fastPhase * 8f) * 14f * errorShake.value
            } else 0f

            val shiftedCenter = Offset(center.x + shakeOffsetX, center.y)

            // 1. Draw Outer Ambient Glowing Halos
            drawAmbientGlow(
                center = shiftedCenter,
                radius = radius,
                orbState = orbState,
                audioAmplitude = audioAmplitude
            )

            // 2. Draw Reactive Ripple Waves (Listening or Success)
            if (orbState == OrbState.LISTENING || successRipple.value > 0f) {
                drawReactiveRipples(
                    center = shiftedCenter,
                    radius = radius,
                    orbState = orbState,
                    audioAmp = audioAmplitude,
                    rippleProgress = successRipple.value,
                    phase = fastPhase
                )
            }

            // 3. Draw Orbiting Particles (Thinking, Listening, Agent, Vision, Image Gen)
            if (orbState == OrbState.THINKING || orbState == OrbState.LISTENING ||
                orbState == OrbState.AGENT || orbState == OrbState.VISION || orbState == OrbState.IMAGE_GENERATION) {
                drawOrbitingMotes(
                    center = shiftedCenter,
                    radius = radius,
                    phase = fastPhase,
                    orbState = orbState,
                    performanceMode = performanceMode
                )
            }

            // 4. Draw Core Liquid Glass Body with Multi-harmonic Deformation
            drawLiquidBody(
                center = shiftedCenter,
                radius = radius,
                phase = phase,
                fastPhase = fastPhase,
                orbState = orbState,
                audioAmp = audioAmplitude,
                performanceMode = performanceMode
            )

            // 5. Draw Glass Refraction Lens & Specular Arc Reflections
            drawGlassHighlights(
                center = shiftedCenter,
                radius = radius,
                phase = phase
            )
        }
    }
}

private fun DrawScope.drawAmbientGlow(
    center: Offset,
    radius: Float,
    orbState: OrbState,
    audioAmplitude: Float
) {
    val glowColor = when (orbState) {
        OrbState.IDLE -> ElectricViolet.copy(alpha = 0.28f)
        OrbState.LISTENING -> NeonCyan.copy(alpha = 0.45f + audioAmplitude * 0.3f)
        OrbState.THINKING -> ElectricPurple.copy(alpha = 0.45f)
        OrbState.SPEAKING -> RadiantMagenta.copy(alpha = 0.45f + audioAmplitude * 0.25f)
        OrbState.ACTION_SUCCESS -> EmeraldSuccess.copy(alpha = 0.55f)
        OrbState.ERROR -> CoralWarning.copy(alpha = 0.45f)
        OrbState.VISION -> NeonCyan.copy(alpha = 0.48f)
        OrbState.AGENT -> ElectricViolet.copy(alpha = 0.48f)
        OrbState.IMAGE_GENERATION -> RadiantMagenta.copy(alpha = 0.52f)
    }

    val glowRadius = radius * 1.55f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, glowColor.copy(alpha = 0.08f), Color.Transparent),
            center = center,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = center
    )
}

private fun DrawScope.drawReactiveRipples(
    center: Offset,
    radius: Float,
    orbState: OrbState,
    audioAmp: Float,
    rippleProgress: Float,
    phase: Float
) {
    if (rippleProgress > 0f) {
        // Golden/Emerald success shockwave
        val shockRadius = radius * (1f + rippleProgress * 0.7f)
        val shockAlpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.6f
        drawCircle(
            color = EmeraldSuccess.copy(alpha = shockAlpha),
            radius = shockRadius,
            center = center,
            style = Stroke(width = 4f * (1f - rippleProgress))
        )
    }

    if (orbState == OrbState.LISTENING && audioAmp > 0.05f) {
        val waveRadius1 = radius + (audioAmp * 28f * sin(phase))
        val waveRadius2 = radius + (audioAmp * 48f * cos(phase + 1f))

        drawCircle(
            color = NeonCyan.copy(alpha = 0.25f * audioAmp),
            radius = waveRadius1,
            center = center,
            style = Stroke(width = 2.5f)
        )
        drawCircle(
            color = ElectricViolet.copy(alpha = 0.18f * audioAmp),
            radius = waveRadius2,
            center = center,
            style = Stroke(width = 1.8f)
        )
    }
}

private fun DrawScope.drawOrbitingMotes(
    center: Offset,
    radius: Float,
    phase: Float,
    orbState: OrbState,
    performanceMode: CharacterPerformanceMode
) {
    val baseMotes = if (orbState == OrbState.THINKING || orbState == OrbState.AGENT) 8 else 5
    val moteCount = when (performanceMode) {
        CharacterPerformanceMode.HIGH -> baseMotes + 3
        CharacterPerformanceMode.MEDIUM -> baseMotes
        CharacterPerformanceMode.LOW -> 3
    }
    val speedFactor = if (orbState == OrbState.THINKING || orbState == OrbState.AGENT) 1.8f else 1.0f

    for (i in 0 until moteCount) {
        val angle = (phase * speedFactor) + (i * (2f * PI.toFloat() / moteCount))
        val orbitDistX = radius * (1.15f + 0.18f * sin(angle * 1.3f))
        val orbitDistY = radius * (0.68f + 0.15f * cos(angle * 1.5f))

        val x = center.x + orbitDistX * cos(angle)
        val y = center.y + orbitDistY * sin(angle)

        val particleColor = when (orbState) {
            OrbState.VISION -> NeonCyan.copy(alpha = 0.85f)
            OrbState.IMAGE_GENERATION -> RadiantMagenta.copy(alpha = 0.85f)
            else -> if (i % 2 == 0) NeonCyan.copy(alpha = 0.8f) else SoftPink.copy(alpha = 0.8f)
        }
        drawCircle(
            color = particleColor,
            radius = 3.5f + (sin(angle * 3f) * 1.5f),
            center = Offset(x, y)
        )
    }

    // Vision mode scanning laser sweep
    if (orbState == OrbState.VISION) {
        val scanY = center.y + sin(phase * 2f) * radius * 0.75f
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    NeonCyan.copy(alpha = 0.85f),
                    EmeraldSuccess.copy(alpha = 0.85f),
                    Color.Transparent
                )
            ),
            start = Offset(center.x - radius * 0.85f, scanY),
            end = Offset(center.x + radius * 0.85f, scanY),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawLiquidBody(
    center: Offset,
    radius: Float,
    phase: Float,
    fastPhase: Float,
    orbState: OrbState,
    audioAmp: Float,
    performanceMode: CharacterPerformanceMode
) {
    // Generate smooth undulating liquid perimeter path using trigonometric harmonic synthesis
    val path = Path()
    val steps = when (performanceMode) {
        CharacterPerformanceMode.HIGH -> 72
        CharacterPerformanceMode.MEDIUM -> 48
        CharacterPerformanceMode.LOW -> 28
    }
    val angleStep = (2f * PI.toFloat()) / steps

    val deformIntensity = when (orbState) {
        OrbState.IDLE -> 0.045f
        OrbState.LISTENING -> 0.08f + (audioAmp * 0.12f)
        OrbState.THINKING -> 0.075f
        OrbState.SPEAKING -> 0.065f + (audioAmp * 0.09f)
        OrbState.ACTION_SUCCESS -> 0.05f
        OrbState.ERROR -> 0.04f
        OrbState.VISION -> 0.06f
        OrbState.AGENT -> 0.07f
        OrbState.IMAGE_GENERATION -> 0.075f
    }

    for (i in 0..steps) {
        val theta = i * angleStep

        // 3-octave harmonic fluid noise
        val wave1 = sin(theta * 3f + phase) * 0.5f
        val wave2 = if (performanceMode != CharacterPerformanceMode.LOW) cos(theta * 5f - fastPhase * 1.2f) * 0.3f else 0f
        val wave3 = if (performanceMode == CharacterPerformanceMode.HIGH) sin(theta * 7f + phase * 2f) * 0.2f else 0f
        val totalDeform = (wave1 + wave2 + wave3) * deformIntensity

        val r = radius * (1f + totalDeform)
        val x = center.x + r * cos(theta)
        val y = center.y + r * sin(theta)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    // Liquid Core Gradient Colors based on Orb State
    val coreColors = when (orbState) {
        OrbState.IDLE -> listOf(
            ElectricPurple.copy(alpha = 0.95f),
            ElectricViolet.copy(alpha = 0.85f),
            Color(0xFF2E1065),
            Color(0xFF0F0728)
        )
        OrbState.LISTENING -> listOf(
            NeonCyan.copy(alpha = 0.95f),
            ElectricViolet.copy(alpha = 0.9f),
            DeepIndigo,
            Color(0xFF0C1938)
        )
        OrbState.THINKING -> listOf(
            SoftLavender.copy(alpha = 0.95f),
            ElectricPurple.copy(alpha = 0.9f),
            RadiantMagenta.copy(alpha = 0.75f),
            Color(0xFF2B0945)
        )
        OrbState.SPEAKING -> listOf(
            SoftPink.copy(alpha = 0.95f),
            RadiantMagenta.copy(alpha = 0.9f),
            ElectricViolet.copy(alpha = 0.85f),
            Color(0xFF38072B)
        )
        OrbState.ACTION_SUCCESS -> listOf(
            EmeraldSuccess.copy(alpha = 0.95f),
            NeonCyan.copy(alpha = 0.85f),
            Color(0xFF064E3B),
            Color(0xFF032219)
        )
        OrbState.ERROR -> listOf(
            CoralWarning.copy(alpha = 0.95f),
            RadiantMagenta.copy(alpha = 0.85f),
            Color(0xFF4C0519),
            Color(0xFF23030D)
        )
        OrbState.VISION -> listOf(
            NeonCyan.copy(alpha = 0.95f),
            EmeraldSuccess.copy(alpha = 0.85f),
            Color(0xFF064E3B),
            Color(0xFF031D16)
        )
        OrbState.AGENT -> listOf(
            ElectricPurple.copy(alpha = 0.95f),
            NeonCyan.copy(alpha = 0.85f),
            ElectricViolet.copy(alpha = 0.75f),
            Color(0xFF160B30)
        )
        OrbState.IMAGE_GENERATION -> listOf(
            RadiantMagenta.copy(alpha = 0.95f),
            SoftPink.copy(alpha = 0.90f),
            ElectricPurple.copy(alpha = 0.80f),
            Color(0xFF2C0A33)
        )
    }

    // Dynamic focal point rotating with phase for liquid depth illusion
    val focalOffset = Offset(
        center.x + (radius * 0.28f * cos(phase)),
        center.y + (radius * 0.28f * sin(phase))
    )

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = coreColors,
            center = focalOffset,
            radius = radius * 1.15f
        )
    )

    // Inner Glass Rim
    val rimColor = when (orbState) {
        OrbState.LISTENING -> NeonCyan.copy(alpha = 0.6f)
        OrbState.ACTION_SUCCESS -> EmeraldSuccess.copy(alpha = 0.7f)
        OrbState.ERROR -> CoralWarning.copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.45f)
    }

    drawPath(
        path = path,
        brush = Brush.sweepGradient(
            colors = listOf(
                rimColor,
                rimColor.copy(alpha = 0.15f),
                Color.Transparent,
                rimColor.copy(alpha = 0.35f),
                rimColor
            ),
            center = center
        ),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawGlassHighlights(
    center: Offset,
    radius: Float,
    phase: Float
) {
    // 1. Top-left Specular Crescent Reflection (Glass sheen)
    val specularPath = Path()
    val arcRect = Size(radius * 1.55f, radius * 1.35f)
    val arcTopLeft = Offset(center.x - radius * 0.82f, center.y - radius * 0.88f)

    specularPath.addArc(
        oval = androidx.compose.ui.geometry.Rect(arcTopLeft, arcRect),
        startAngleDegrees = 205f,
        sweepAngleDegrees = 85f
    )

    drawPath(
        path = specularPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.25f),
                Color.Transparent
            ),
            start = Offset(center.x - radius * 0.6f, center.y - radius * 0.8f),
            end = Offset(center.x, center.y - radius * 0.2f)
        ),
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )

    // 2. Secondary soft bottom reflection rim (Refracted back light)
    val backLightPath = Path()
    val backRect = Size(radius * 1.4f, radius * 1.2f)
    val backTopLeft = Offset(center.x - radius * 0.68f, center.y - radius * 0.35f)

    backLightPath.addArc(
        oval = androidx.compose.ui.geometry.Rect(backTopLeft, backRect),
        startAngleDegrees = 35f,
        sweepAngleDegrees = 75f
    )

    drawPath(
        path = backLightPath,
        color = NeonCyan.copy(alpha = 0.35f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}
