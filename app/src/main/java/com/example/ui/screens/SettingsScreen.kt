package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NovaVpnViewModel

@Composable
fun SettingsScreen(
    viewModel: NovaVpnViewModel
) {
    val context = LocalContext.current
    
    val isAutoConnect by viewModel.isAutoConnect.collectAsStateWithLifecycle()
    val isKillSwitch by viewModel.isKillSwitch.collectAsStateWithLifecycle()
    val dnsMode by viewModel.dnsMode.collectAsStateWithLifecycle()
    val customDns by viewModel.customDns.collectAsStateWithLifecycle()
    val splitAppsCount by viewModel.splitTunnelingAppsCount.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremiumUser.collectAsStateWithLifecycle()
    val debugLogs by viewModel.debugLogs.collectAsStateWithLifecycle()

    var showDnsDialog by remember { mutableStateOf(false) }
    var tempCustomDns by remember { mutableStateOf(customDns) }

    CyberBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            // Screen Title
            Text(
                text = "NOVA SECURE PROPERTIES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Property Section 1: Tunnel Rules
                item {
                    Text(
                        text = "CORE PARAMS & ROUTING TUNNEL",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassMorphismCard(modifier = Modifier.fillMaxWidth()) {
                        // Option 1: Auto Connect
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Automated Handshake", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                Text(text = "Auto-launch tunnel on system startup", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Switch(
                                checked = isAutoConnect,
                                onCheckedChange = { viewModel.toggleAutoConnect(it) },
                                modifier = Modifier.testTag("auto_connect_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonCyan,
                                    checkedTrackColor = CyberNavy
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Option 2: Kill Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "System Kill Switch", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                Text(text = "Block physical traffic if VPN drops", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Switch(
                                checked = isKillSwitch,
                                onCheckedChange = { viewModel.toggleKillSwitch(it) },
                                modifier = Modifier.testTag("kill_switch_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonPink,
                                    checkedTrackColor = CyberNavy
                                )
                            )
                        }
                    }
                }

                // Property Section 2: Split Tunneling & DNS Profiles
                item {
                    Text(
                        text = "DNS PROPERTIES & ISOLATED TUNNELLING",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = NeonPink,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassMorphismCard(modifier = Modifier.fillMaxWidth()) {
                        // Option 3: DNS Protocol Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDnsDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "DNS Resolution Host", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                Text(text = "Mode: $dnsMode ($customDns)", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = NeonPink
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Option 4: Split Tunneling Configuration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val countList = listOf(5, 12, 28)
                                    val newCount = countList.random()
                                    viewModel.updateSplitTunnelingCount(newCount)
                                    Toast.makeText(context, "Configured split tunneling rule for $newCount system packages", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Split Tunnelling Packages", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                Text(text = "Excluding $splitAppsCount packages from secure proxy routes", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                            }
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = NeonCyan
                            )
                        }
                    }
                }

                // Property Section 3: Log telemetry
                item {
                    Text(
                        text = "WIREGUARD DIAGNOSTIC telemetry",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = NeonPurple,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberDarkGray)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE LOGS FEED",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = NeonPurple
                            )
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(debugLogs.reversed()) { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = CyberWhite.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Premium Reset Action Target
                item {
                    if (isPremium) {
                        Button(
                            onClick = {
                                viewModel.resetPremium()
                                Toast.makeText(context, "Premium billing simulated reset.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "SIMULATE CANCEL SUBSCRIPTION", color = CyberRed)
                        }
                    }
                }

                // Application version tag
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Nova VPN Secure client", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                        Text(text = "v1.2.0-stable (WireGuard Core v1.0.2023)", style = MaterialTheme.typography.bodySmall, color = CyberGray)
                    }
                }
            }
        }
    }

    // DNS Custom Map Editor Dialog Overlay
    if (showDnsDialog) {
        AlertDialog(
            onDismissRequest = { showDnsDialog = false },
            containerColor = CyberDarkGray,
            title = { Text(text = "SELECT DNS HOST PROFILE", color = Color.White) },
            text = {
                Column {
                    listOf("Automatic", "AdGuard Safe DNS", "Google Public DNS", "Cloudflare Secure", "Custom").forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateDnsMode(mode)
                                    val fallbackVal = when (mode) {
                                        "Google Public DNS" -> "8.8.8.8"
                                        "Cloudflare Secure" -> "1.1.1.1"
                                        "AdGuard Safe DNS" -> "94.140.14.14"
                                        else -> "1.1.1.1"
                                    }
                                    if (mode != "Custom") {
                                        viewModel.updateCustomDns(fallbackVal)
                                        showDnsDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = dnsMode == mode,
                                onClick = {
                                    viewModel.updateDnsMode(mode)
                                    if (mode != "Custom") {
                                        val fallbackVal = when (mode) {
                                            "Google Public DNS" -> "8.8.8.8"
                                            "Cloudflare Secure" -> "1.1.1.1"
                                            "AdGuard Safe DNS" -> "94.140.14.14"
                                            else -> "1.1.1.1"
                                        }
                                        viewModel.updateCustomDns(fallbackVal)
                                        showDnsDialog = false
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = mode, color = Color.White)
                        }
                    }

                    if (dnsMode == "Custom") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tempCustomDns,
                            onValueChange = { tempCustomDns = it },
                            label = { Text(text = "Custom IPv4 IP Mapping", color = CyberGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberGray
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (dnsMode == "Custom") {
                    TextButton(onClick = {
                        viewModel.updateCustomDns(tempCustomDns)
                        showDnsDialog = false
                    }) {
                        Text(text = "APPLY PROFILE", color = NeonCyan)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDnsDialog = false }) {
                    Text(text = "CANCEL", color = NeonPink)
                }
            }
        )
    }
}
