package com.example.sayra.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RadiantMagenta
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftPink
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SayraCharacterRenderer(
    characterState: CharacterState,
    expression: CharacterExpression,
    audioAmplitude: Float,
    performanceMode: CharacterPerformanceMode,
    attentionTarget: CharacterAttentionTarget,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    showSignatureEnergy: Boolean = true
) {
    // 1. Subtle natural breathing animation (slow, graceful sine wave)
    val infiniteTransition = rememberInfiniteTransition(label = "CharacterBreathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.990f,
        targetValue = 1.010f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )
    val breathingOffsetY by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingOffsetY"
    )

    // Liquid energy continuous flow
    val energyRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (characterState) {
                    CharacterState.THINKING -> 5000
                    CharacterState.SPEAKING -> 8000
                    CharacterState.LISTENING -> 9000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "energyRotation"
    )

    // 2. Smooth audio amplitude reaction for lip sync and energy glow
    val animatedAmplitude by animateFloatAsState(
        targetValue = audioAmplitude.coerceIn(0f, 1f),
        animationSpec = tween(70, easing = LinearEasing),
        label = "lipSyncAmp"
    )

    // 3. Eye Blinking System (realistic intervals with occasional double blink)
    var blinkAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            val waitTime = Random.nextLong(2800, 5200)
            delay(waitTime)

            // First blink
            val blinkDuration = 120L
            blinkAlpha = 1f
            delay(blinkDuration)
            blinkAlpha = 0f

            // 25% chance of realistic double-blink
            if (Random.nextFloat() < 0.25f) {
                delay(100L)
                blinkAlpha = 1f
                delay(90L)
                blinkAlpha = 0f
            }
        }
    }

    // Gaze direction offset based on AttentionTarget
    val (gazeTargetX, gazeTargetY) = when (attentionTarget) {
        CharacterAttentionTarget.USER -> 0f to 0f
        CharacterAttentionTarget.ORB -> 0f to 4f
        CharacterAttentionTarget.TRANSCRIPT -> 0f to 6f
        CharacterAttentionTarget.THINKING_UP -> 2f to -4f
        CharacterAttentionTarget.SCANNING -> (sin(energyRotation * 0.08f) * 6f) to 0f
    }
    val gazeX by animateFloatAsState(gazeTargetX, tween(300), label = "gazeX")
    val gazeY by animateFloatAsState(gazeTargetY, tween(300), label = "gazeY")

    // Micro head tilt and gesture matching expressions
    val headTiltTarget = when (expression) {
        CharacterExpression.CURIOUS -> 2.6f
        CharacterExpression.CUTE_SMILE -> 1.4f
        CharacterExpression.THINKING -> -2.0f
        CharacterExpression.HAPPY -> 1.0f
        CharacterExpression.CONFIDENT -> -0.6f
        CharacterExpression.CONCERNED -> -1.4f
        CharacterExpression.SURPRISED -> -1.0f
        CharacterExpression.SERIOUS -> 0f
        CharacterExpression.FOCUSED -> 0f
        else -> 0f
    }
    val headTilt by animateFloatAsState(headTiltTarget, tween(450, easing = FastOutSlowInEasing), label = "headTilt")

    // State-dependent glow colors
    val (primaryGlow, secondaryGlow) = when (characterState) {
        CharacterState.IDLE -> ElectricViolet to NeonCyan
        CharacterState.LISTENING -> NeonCyan to ElectricPurple
        CharacterState.THINKING -> SoftPink to RadiantMagenta
        CharacterState.SPEAKING -> RadiantMagenta to NeonCyan
        CharacterState.ACTION_SUCCESS -> EmeraldSuccess to NeonCyan
        CharacterState.ERROR -> Color(0xFFF43F5E) to ElectricPurple
        CharacterState.VISION -> NeonCyan to EmeraldSuccess
        CharacterState.AGENT -> ElectricViolet to SoftLavender
        CharacterState.IMAGE_GENERATION -> RadiantMagenta to SoftPink
    }

    // Number of particles based on performance mode
    val particleCount = when (performanceMode) {
        CharacterPerformanceMode.HIGH -> 32
        CharacterPerformanceMode.MEDIUM -> 18
        CharacterPerformanceMode.LOW -> 8
    }

    val particles = remember(particleCount) {
        List(particleCount) { index ->
            CharacterParticle(
                id = index,
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.008f + 0.004f,
                baseAlpha = Random.nextFloat() * 0.5f + 0.3f,
                colorIndex = index % 4,
                phase = Random.nextFloat() * 6.28f
            )
        }
    }

    val particleColors = listOf(
        ElectricViolet,
        NeonCyan,
        RadiantMagenta,
        SoftLavender
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("sayra_character_view")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Ambient Volumetric Glow & Holographic Aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            // Dynamic outer aura
            val auraIntensity = when (characterState) {
                CharacterState.SPEAKING -> 0.45f + animatedAmplitude * 0.25f
                CharacterState.LISTENING -> 0.40f + animatedAmplitude * 0.20f
                CharacterState.THINKING -> 0.35f
                else -> 0.25f
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = auraIntensity),
                        secondaryGlow.copy(alpha = auraIntensity * 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.15f
                ),
                radius = radius * 1.15f,
                center = center
            )
        }

        // LAYER 2: Character Portrait with Breathing & Liquid Glass Frame
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .offset(y = breathingOffsetY.dp)
                .rotate(headTilt)
                .scale(breathingScale)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            primaryGlow.copy(alpha = 0.8f),
                            secondaryGlow.copy(alpha = 0.3f),
                            SoftLavender.copy(alpha = 0.7f),
                            primaryGlow.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(ObsidianBlack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_sayra_character),
                contentDescription = "SAYRA AI Character Presence",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        // Subtle holographic scanlines / soft glass depth gradient
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    ObsidianBlack.copy(alpha = 0.55f),
                                    ObsidianBlack.copy(alpha = 0.85f)
                                )
                            )
                        )
                    }
            )

            // LAYER 3 & 4: Interactive Canvas Overlay (Eyes, Lip-Sync, Liquid Energy, Particles)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = this.size.width
                val canvasH = this.size.height

                // --- EYES ANIMATION & DEEP VIOLET AI GLOW ---
                // Eyes coordinates in portrait (approx centered around X=42% and 58%, Y=41%)
                val leftEyeCenter = Offset(canvasW * 0.42f + gazeX, canvasH * 0.41f + gazeY)
                val rightEyeCenter = Offset(canvasW * 0.58f + gazeX, canvasH * 0.41f + gazeY)
                val eyeRadius = canvasW * 0.024f

                // Deep violet AI iris glow
                val irisGlowColor = primaryGlow.copy(alpha = 0.5f + animatedAmplitude * 0.3f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(irisGlowColor, Color.Transparent),
                        center = leftEyeCenter,
                        radius = eyeRadius * 2.2f
                    ),
                    radius = eyeRadius * 2.2f,
                    center = leftEyeCenter
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(irisGlowColor, Color.Transparent),
                        center = rightEyeCenter,
                        radius = eyeRadius * 2.2f
                    ),
                    radius = eyeRadius * 2.2f,
                    center = rightEyeCenter
                )

                // Specular glint
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = eyeRadius * 0.35f,
                    center = Offset(leftEyeCenter.x - 1.5f, leftEyeCenter.y - 1.5f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = eyeRadius * 0.35f,
                    center = Offset(rightEyeCenter.x - 1.5f, rightEyeCenter.y - 1.5f)
                )

                // Realistic Eyelid Blink Overlay
                if (blinkAlpha > 0f) {
                    val blinkHeight = eyeRadius * 2.8f * blinkAlpha
                    drawOval(
                        color = Color(0xFF1E172E).copy(alpha = 0.95f * blinkAlpha),
                        topLeft = Offset(leftEyeCenter.x - eyeRadius * 1.6f, leftEyeCenter.y - blinkHeight / 2f),
                        size = Size(eyeRadius * 3.2f, blinkHeight)
                    )
                    drawOval(
                        color = Color(0xFF1E172E).copy(alpha = 0.95f * blinkAlpha),
                        topLeft = Offset(rightEyeCenter.x - eyeRadius * 1.6f, rightEyeCenter.y - blinkHeight / 2f),
                        size = Size(eyeRadius * 3.2f, blinkHeight)
                    )
                }

                // --- VOICE-REACTIVE LIP SYNC ---
                // Mouth coordinate around X=50%, Y=59%
                val mouthCenter = Offset(canvasW * 0.50f, canvasH * 0.59f)
                val baseMouthWidth = canvasW * 0.08f
                val mouthOpening = when (characterState) {
                    CharacterState.SPEAKING -> (animatedAmplitude * canvasH * 0.024f).coerceIn(0f, canvasH * 0.026f)
                    else -> 0f
                }

                if (mouthOpening > 1f) {
                    // Subtle interior lip aperture
                    drawOval(
                        color = Color(0xFF1B0B24).copy(alpha = 0.7f),
                        topLeft = Offset(mouthCenter.x - baseMouthWidth / 2f, mouthCenter.y - mouthOpening / 2f),
                        size = Size(baseMouthWidth, mouthOpening)
                    )
                    // Lip sheen highlight
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(
                                SoftLavender.copy(alpha = 0.2f),
                                SoftPink.copy(alpha = 0.6f),
                                SoftLavender.copy(alpha = 0.2f)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(mouthCenter.x - baseMouthWidth / 2f, mouthCenter.y - mouthOpening / 2f),
                        size = Size(baseMouthWidth, mouthOpening),
                        style = Stroke(width = 1.5f)
                    )
                } else if (expression == CharacterExpression.CUTE_SMILE || expression == CharacterExpression.HAPPY) {
                    // Subtle, gentle smile curvature
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(
                                SoftLavender.copy(alpha = 0.15f),
                                SoftPink.copy(alpha = 0.55f),
                                SoftLavender.copy(alpha = 0.15f)
                            )
                        ),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(mouthCenter.x - baseMouthWidth * 0.45f, mouthCenter.y - 2f),
                        size = Size(baseMouthWidth * 0.9f, 6f),
                        style = Stroke(width = 1.6f, cap = StrokeCap.Round)
                    )
                }

                // --- SIGNATURE LIQUID AI ENERGY COLLAR & SHOULDERS ---
                if (showSignatureEnergy) {
                    val energyCollarY = canvasH * 0.78f
                    val energyPath = Path().apply {
                        moveTo(canvasW * 0.20f, energyCollarY + sin(energyRotation * 0.05f) * 4f)
                        quadraticTo(
                            canvasW * 0.50f,
                            energyCollarY + 12f + animatedAmplitude * 10f,
                            canvasW * 0.80f,
                            energyCollarY + cos(energyRotation * 0.05f) * 4f
                        )
                    }

                    // Pulsating glow stream
                    drawPath(
                        path = energyPath,
                        brush = Brush.horizontalGradient(
                            listOf(
                                primaryGlow.copy(alpha = 0.1f),
                                secondaryGlow.copy(alpha = 0.75f + animatedAmplitude * 0.25f),
                                primaryGlow.copy(alpha = 0.9f + animatedAmplitude * 0.1f),
                                secondaryGlow.copy(alpha = 0.75f + animatedAmplitude * 0.25f),
                                primaryGlow.copy(alpha = 0.1f)
                            )
                        ),
                        style = Stroke(
                            width = 3.5f + animatedAmplitude * 3f,
                            cap = StrokeCap.Round
                        )
                    )

                    // Secondary flowing wave
                    val energyPath2 = Path().apply {
                        moveTo(canvasW * 0.28f, energyCollarY + 8f)
                        quadraticTo(
                            canvasW * 0.50f,
                            energyCollarY + 20f + animatedAmplitude * 8f,
                            canvasW * 0.72f,
                            energyCollarY + 8f
                        )
                    }
                    drawPath(
                        path = energyPath2,
                        brush = Brush.horizontalGradient(
                            listOf(
                                RadiantMagenta.copy(alpha = 0.2f),
                                SoftLavender.copy(alpha = 0.7f),
                                RadiantMagenta.copy(alpha = 0.2f)
                            )
                        ),
                        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                    )
                }

                // --- FLOATING DIGITAL PARTICLES ---
                particles.forEach { p ->
                    // Update particle phase
                    p.phase += p.speed * when (characterState) {
                        CharacterState.SPEAKING -> 2.5f
                        CharacterState.THINKING -> 2.0f
                        CharacterState.LISTENING -> 1.5f
                        else -> 1.0f
                    }

                    val px = (p.x * canvasW + sin(p.phase) * 12f)
                    val py = (p.y * canvasH - (p.phase * 20f) % canvasH + canvasH) % canvasH
                    val pColor = particleColors[p.colorIndex]
                    val pAlpha = (p.baseAlpha * (0.6f + 0.4f * sin(p.phase))).coerceIn(0f, 1f)

                    drawCircle(
                        color = pColor.copy(alpha = pAlpha),
                        radius = p.size * (0.8f + animatedAmplitude * 0.6f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
