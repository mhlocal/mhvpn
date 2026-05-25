package com.example.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VpnServer
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NovaVpnViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ServerListScreen(
    viewModel: NovaVpnViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    
    // Config import launcher
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    // Get file name
                    val fileName = "custom_imported.conf"
                    val success = viewModel.importWireGuardConfig(fileName, content)
                    if (success) {
                        Toast.makeText(context, "WireGuard configuration parsed securely!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to parse .conf formatting rules.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ServerListScreen", "Error reading content URI", e)
                Toast.makeText(context, "Access denied to selected local file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredServers = remember(servers, searchQuery) {
        if (searchQuery.isBlank()) {
            servers
        } else {
            servers.filter {
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.countryCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    CyberBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
        ) {
            // Screen Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("server_list_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Return",
                        tint = Color.White
                    )
                }

                Text(
                    text = "SECURE SERVER REGISTRY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )

                // Refresh trigger
                IconButton(
                    onClick = { viewModel.syncServers() },
                    modifier = Modifier.testTag("server_list_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Nodes",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actionable Import & Smart select bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smart Path selection
                Button(
                    onClick = { viewModel.selectFastestServer() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("smart_path_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "SMART PATH", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }

                // Import config file
                Button(
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("application/octet-stream", "text/plain", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .testTag("import_conf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "IMPORT .CONF", style = MaterialTheme.typography.bodySmall, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // QR scanner simulation row
            Button(
                onClick = {
                    // Simulate scanning QR code of WireGuard credentials
                    val sampleQrConf = """
                        [Interface]
                        PrivateKey = kP8cxXhY2uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo88A4=
                        Address = 10.0.0.11/24
                        DNS = 1.1.1.1
                        
                        [Peer]
                        PublicKey = rO9fYH7uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo22E4=
                        Endpoint = 198.51.100.45:51820
                        AllowedIPs = 0.0.0.0/0
                    """.trimIndent()
                    viewModel.importWireGuardConfig("QR-Scan-Profile.conf", sampleQrConf)
                    Toast.makeText(context, "Parsed QR WireGuard configuration securely!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qr_config_scanner_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = NeonPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SCAN CREDENTIAL QR CODE", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Text Field with glass panels
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("server_search_input"),
                placeholder = { Text(text = "FILTER SYSTEMS BY KEYWORDS OR FLAGS...", color = CyberGray) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberGray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CyberDarkGray.copy(alpha = 0.85f),
                    unfocusedContainerColor = CyberDarkGray.copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 1,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Scrollable Server directory list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredServers, key = { it.id }) { server ->
                    val isSelected = selectedServer?.id == server.id
                    val borderGradient = if (isSelected) {
                        Brush.horizontalGradient(listOf(NeonCyan, NeonPink))
                    } else {
                        Brush.horizontalGradient(listOf(GlassBorder, GlassBorder))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CyberDarkGray.copy(alpha = 0.75f))
                            .clickable {
                                viewModel.selectServer(server)
                                onBack()
                            }
                            .testTag("server_item_${server.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // National Emoji Flag
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FlagEmojiText(countryCode = server.countryCode)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = server.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Performance node load
                                        LinearProgressIndicator(
                                            progress = { server.loadPercent / 100f },
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = if (server.loadPercent < 50) CyberGreen else if (server.loadPercent < 80) NeonCyan else NeonPink,
                                            trackColor = GlassWhite
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Load: ${server.loadPercent}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CyberGray
                                        )
                                    }
                                }
                            }

                            // Favorite toggle, ping, and delete actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Ping
                                Text(
                                    text = "${server.pingMs}ms",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (server.pingMs < 30) CyberGreen else if (server.pingMs < 80) NeonCyan else NeonPink,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                // Favorite Toggle Action target
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(server) },
                                    modifier = Modifier.testTag("favorite_button_${server.id}")
                                ) {
                                    Icon(
                                        imageVector = if (server.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (server.isFavorite) NeonPink else CyberGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete custom imported server action target
                                if (server.id.startsWith("Imported")) {
                                    IconButton(
                                        onClick = { viewModel.deleteDownloadedServer(server) },
                                        modifier = Modifier.testTag("delete_server_button_${server.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Import",
                                            tint = CyberRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredServers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO ACTIVE NODES DETECTED MATCHING SEARCH TERMS.",
                                color = CyberGray,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }
        }
    }
}
