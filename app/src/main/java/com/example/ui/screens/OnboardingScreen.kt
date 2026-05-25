package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberBackground
import com.example.ui.components.GlassMorphismCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }

    val slides = listOf(
        OnboardingPageData(
            title = "NOVA SHIELDED CORE",
            desc = "Military-grade WireGuard encryption routing. Your IP address and network traces are fully secured in an anonymous shadow shell.",
            icon = Icons.Default.Shield,
            color = NeonCyan
        ),
        OnboardingPageData(
            title = "HYPER-SONIC NODES",
            desc = "One-tap connection to blazing redundant channels. Dynamically evaluates global servers to select the absolute fastest peer routing.",
            icon = Icons.Default.FlashOn,
            color = NeonPink
        ),
        OnboardingPageData(
            title = "ZERO LOG STORAGE",
            desc = "Fully decoupled routing with secure local Room storage and custom DNS configurations. Your cyber footprint stays isolated.",
            icon = Icons.Default.Lock,
            color = NeonPurple
        )
    )

    CyberBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Name
            Text(
                text = "NOVA VPN",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                ),
                color = NeonCyan,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Central glass sliding card
            GlassMorphismCard(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 32.dp),
                borderGlow = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing circular icon panel
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(slides[currentPage].color.copy(alpha = 0.15f))
                            .border(1.5.dp, slides[currentPage].color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slides[currentPage].icon,
                            contentDescription = null,
                            tint = slides[currentPage].color,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = slides[currentPage].title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = slides[currentPage].desc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp
                        ),
                        color = CyberGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Bottom Section with Indicator bullets and actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pagination Indicator Bullets
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    slides.forEachIndexed { index, slide ->
                        val isSelected = index == currentPage
                        val widthByState = if (isSelected) 24.dp else 8.dp
                        val colorByState = if (isSelected) slide.color else CyberGray.copy(alpha = 0.4f)
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(widthByState)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorByState)
                        )
                    }
                }

                // Next Button
                if (currentPage < slides.lastIndex) {
                    NeonButton(
                        text = "CONTINUE SECURE SYSTEM",
                        onClick = { currentPage++ },
                        glowColor = slides[currentPage].color,
                        modifier = Modifier.testTag("onboarding_next_button")
                    )
                } else {
                    NeonButton(
                        text = "INITIALIZE CORE SHELL",
                        onClick = {
                            // Persist onboarding state completion
                            context.getSharedPreferences("nova_vpn_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean("onboarding_completed", true)
                                .apply()
                            onFinished()
                        },
                        glowColor = NeonCyan,
                        modifier = Modifier.testTag("onboarding_finish_button")
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color
)
