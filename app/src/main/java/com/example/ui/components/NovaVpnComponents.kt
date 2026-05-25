package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun CyberBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .drawBehind {
                // High-fidelity background mesh/radar grid drawing
                val gridSpacing = 40.dp.toPx()
                val width = size.width
                val height = size.height

                // Base ambient bottom glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(width * 0.1f, height * 0.9f),
                        radius = width * 0.8f
                    ),
                    center = Offset(width * 0.1f, height * 0.9f),
                    radius = width * 0.8f
                )

                // Top right cyan ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(width * 0.9f, height * 0.1f),
                        radius = width * 0.8f
                    ),
                    center = Offset(width * 0.9f, height * 0.1f),
                    radius = width * 0.8f
                )

                // Subtle tactical horizontal/vertical grid lines
                var x = 0f
                while (x < width) {
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.015f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridSpacing
                }

                var y = 0f
                while (y < height) {
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.015f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridSpacing
                }
            }
    ) {
        content()
    }
}

@Composable
fun GlassMorphismCard(
    modifier: Modifier = Modifier,
    borderGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (borderGlow) NeonCyan.copy(alpha = 0.45f) else GlassBorder
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f),
                        borderColor
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp),
        content = content
    )
}

@Composable
fun PulseConnectionRing(
    modifier: Modifier = Modifier,
    isConnectingOrConnected: Boolean,
    isConnected: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition("PulseTransition")
    
    val pulseSize1 by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnectingOrConnected) 1.55f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseSize1"
    )

    val pulseSize2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnectingOrConnected) 2.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing, delayMillis = 600),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseSize2"
    )

    val pulseAlpha1 by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )

    val pulseAlpha2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing, delayMillis = 600),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha2"
    )

    val pulseColor = if (isConnected) CyberGreen else if (isConnectingOrConnected) NeonPink else NeonCyan

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isConnectingOrConnected) {
            // Pulsing Ring 2
            Box(
                modifier = Modifier
                    .fillMaxSize(pulseSize2)
                    .clip(RoundedCornerShape(100.dp))
                    .border(2.dp, pulseColor.copy(alpha = pulseAlpha2), RoundedCornerShape(100.dp))
            )
            // Pulsing Ring 1
            Box(
                modifier = Modifier
                    .fillMaxSize(pulseSize1)
                    .clip(RoundedCornerShape(100.dp))
                    .border(3.dp, pulseColor.copy(alpha = pulseAlpha1), RoundedCornerShape(100.dp))
            )
        }
        content()
    }
}

@Composable
fun FlagEmojiText(
    countryCode: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge
) {
    Text(
        text = getFlagEmoji(countryCode),
        modifier = modifier,
        style = style
    )
}

fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌐"
    return try {
        val firstChar = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    } catch (e: Exception) {
        "🌐"
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(glowColor, NeonPink)),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("neon_action_button"),
        colors = ButtonDefaults.buttonColors(
            containerColor = CyberDarkGray.copy(alpha = 0.85f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
