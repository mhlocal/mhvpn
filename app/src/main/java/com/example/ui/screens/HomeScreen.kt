package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NovaVpnViewModel
import com.example.vpn.NovaVpnService
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: NovaVpnViewModel,
    onNavigateToServers: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val activeServer by viewModel.activeServer.collectAsStateWithLifecycle()
    
    val downloadSpeed by viewModel.downloadSpeed.collectAsStateWithLifecycle()
    val uploadSpeed by viewModel.uploadSpeed.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.durationSeconds.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUser.collectAsStateWithLifecycle()

    val isConnected = connectionState == NovaVpnService.VpnState.CONNECTED
    val isConnectingOrConnected = connectionState == NovaVpnService.VpnState.CONNECTED || 
                                 connectionState == NovaVpnService.VpnState.CONNECTING

    val statusMessage = when (connectionState) {
        NovaVpnService.VpnState.CONNECTED -> "SECURE COLD SHELL INTENT STABLE"
        NovaVpnService.VpnState.CONNECTING -> "NEGOTIATING HANDSHAKE..."
        NovaVpnService.VpnState.RECONNECTING -> "DETECTING CARRIER DROPOUT... RECONNECTING"
        NovaVpnService.VpnState.DISCONNECTING -> "TEARING SECURE CHANNELS DOWN..."
        NovaVpnService.VpnState.DISCONNECTED -> "SHIELD DISARMED • INSECURE IPS"
    }

    val statusColor = when (connectionState) {
        NovaVpnService.VpnState.CONNECTED -> CyberGreen
        NovaVpnService.VpnState.CONNECTING -> NeonPink
        NovaVpnService.VpnState.RECONNECTING -> NeonPurple
        else -> NeonCyan
    }

    CyberBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOVA VPN",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "v1.2.0-secure",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberGray
                    )
                }

                // Premium badge display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPremium) Brush.horizontalGradient(listOf(NeonCyan, NeonPink))
                            else Brush.horizontalGradient(listOf(CyberNavy, CyberNavy))
                        )
                        .clickable { if (!isPremium) viewModel.activatePremium() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPremium) "HYPER PREMIUM" else "BASIC SHIELD",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isPremium) CyberBlack else CyberGray
                    )
                }
            }

            // Connection Central Core Button
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PulseConnectionRing(
                    modifier = Modifier.size(200.dp),
                    isConnectingOrConnected = isConnectingOrConnected,
                    isConnected = isConnected
                ) {
                    val circleBg = if (isConnected) CyberGreen.copy(alpha = 0.1f) 
                                   else if (isConnectingOrConnected) NeonPink.copy(alpha = 0.1f) 
                                   else CyberDarkGray

                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.82f)
                            .clip(CircleShape)
                            .background(circleBg)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(statusColor, statusColor.copy(alpha = 0.3f), statusColor)
                                ),
                                shape = CircleShape
                            )
                            .clickable { viewModel.toggleVpnConnection() }
                            .testTag("vpn_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = if (isConnected) "Shield Active" else "Shield Offline",
                                tint = statusColor,
                                modifier = Modifier.size(52.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = if (isConnected) "SHIELD ACTIVE" else "SHIELD DOWN",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                ),
                                color = statusColor
                            )
                        }
                    }
                }
            }

            // Status message
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = statusColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Diagnostic Stats Table (Glow design glass module)
                GlassMorphismCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stat 1: Download
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "DOWNLINK", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = downloadSpeed,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Vertical split line
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(GlassWhite)
                        )

                        // Stat 2: Upload
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonPink)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "UPLINK", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uploadSpeed,
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Vertical split line
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(GlassWhite)
                        )

                        // Stat 3: Speed latency list & timer info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "DURATION", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDuration(durationSeconds),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connected server metadata card
            val targetServer = if (isConnected) activeServer else selectedServer
            
            if (targetServer != null) {
                GlassMorphismCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToServers() }
                        .testTag("home_server_picker_trigger")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // National flag representation
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberDarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                FlagEmojiText(countryCode = targetServer.countryCode)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = targetServer.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${targetServer.ipAddress} • MTU 1420",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyberGray
                                    )
                                }
                            }
                        }

                        // Node Latency Indicator
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${targetServer.pingMs} ms",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (targetServer.pingMs < 30) CyberGreen else if (targetServer.pingMs < 80) NeonCyan else NeonPink
                            )
                            Text(
                                text = "LATENCY",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberGray
                            )
                        }
                    }
                }
            } else {
                // Empty server select card
                Button(
                    onClick = onNavigateToServers,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNavy)
                ) {
                    Text(text = "SELECT ROUTING CELL")
                }
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
}
