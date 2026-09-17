package com.offx.sayra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SayraOrb(state: SayraViewModel.State, onTap: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "orb")
    val pulse by infinite.animateFloat(0.88f, 1.08f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    Canvas(Modifier.size(250.dp).pointerInput(Unit) { detectTapGestures { onTap() } }) {
        val r = size.minDimension * .36f * if (state == SayraViewModel.State.LISTENING) pulse else 1f
        drawCircle(Brush.radialGradient(listOf(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color(0xFF8B8BFF), androidx.compose.ui.graphics.Color.Transparent)), radius = r, center = Offset(size.width / 2, size.height / 2))
        drawCircle(androidx.compose.ui.graphics.Color.White.copy(alpha = .08f), radius = r * 1.45f, center = Offset(size.width / 2, size.height / 2))
    }
}
