package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VpnServer
import com.example.data.VpnRepository
import com.example.vpn.NovaVpnService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class NovaVpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application)

    // Room Database Server List observer
    val servers: StateFlow<List<VpnServer>> = repository.allServersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer.asStateFlow()

    // Observe background VPN service reactive states
    val connectionState = NovaVpnService.connectionState
    val activeServer = NovaVpnService.activeServer
    val downloadSpeed = NovaVpnService.downloadSpeed
    val uploadSpeed = NovaVpnService.uploadSpeed
    val durationSeconds = NovaVpnService.durationSeconds
    val debugLogs = NovaVpnService.debugLogs

    // User settings states
    private val prefs = application.getSharedPreferences("nova_vpn_prefs", Context.MODE_PRIVATE)

    private val _isAutoConnect = MutableStateFlow(prefs.getBoolean("auto_connect", false))
    val isAutoConnect = _isAutoConnect.asStateFlow()

    private val _isKillSwitch = MutableStateFlow(prefs.getBoolean("kill_switch", false))
    val isKillSwitch = _isKillSwitch.asStateFlow()

    private val _dnsMode = MutableStateFlow(prefs.getString("dns_mode", "Automatic") ?: "Automatic")
    val dnsMode = _dnsMode.asStateFlow()

    private val _customDns = MutableStateFlow(prefs.getString("custom_dns", "1.1.1.1") ?: "1.1.1.1")
    val customDns = _customDns.asStateFlow()

    private val _splitTunnelingAppsCount = MutableStateFlow(prefs.getInt("split_tunnel_count", 0))
    val splitTunnelingAppsCount = _splitTunnelingAppsCount.asStateFlow()

    private val _isPremiumUser = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremiumUser = _isPremiumUser.asStateFlow()

    init {
        viewModelScope.launch {
            // First boot initialize preset servers
            repository.initializePresetServers()
            
            // Set initial selected server to first or favorite if any
            servers.collectFirst { list ->
                if (list.isNotEmpty()) {
                    val favorite = list.find { it.isFavorite }
                    _selectedServer.value = favorite ?: list.first()
                }
            }
        }
    }

    private suspend fun <T> StateFlow<T>.collectFirst(action: suspend (T) -> Unit) {
        this.filter { 
            if (it is List<*>) it.isNotEmpty() else it != null 
        }.first().let { action(it) }
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        NovaVpnService.logDebug("Selected server updated: ${server.name} (${server.ipAddress})")
    }

    fun toggleFavorite(server: VpnServer) {
        viewModelScope.launch {
            repository.toggleFavorite(server.id, !server.isFavorite)
            NovaVpnService.logDebug("Toggled favorite for server: ${server.name}")
        }
    }

    fun toggleAutoConnect(enabled: Boolean) {
        _isAutoConnect.value = enabled
        prefs.edit().putBoolean("auto_connect", enabled).apply()
        NovaVpnService.logDebug("Settings: Auto-Connect set to $enabled")
    }

    fun toggleKillSwitch(enabled: Boolean) {
        _isKillSwitch.value = enabled
        prefs.edit().putBoolean("kill_switch", enabled).apply()
        NovaVpnService.logDebug("Settings: Advanced Kill Switch set to $enabled")
    }

    fun updateDnsMode(mode: String) {
        _dnsMode.value = mode
        prefs.edit().putString("dns_mode", mode).apply()
        NovaVpnService.logDebug("Settings: DNS resolution mode updated to $mode")
    }

    fun updateCustomDns(dns: String) {
        _customDns.value = dns
        prefs.edit().putString("custom_dns", dns).apply()
        NovaVpnService.logDebug("Settings: Custom private DNS set to $dns")
    }

    fun updateSplitTunnelingCount(count: Int) {
        _splitTunnelingAppsCount.value = count
        prefs.edit().putInt("split_tunnel_count", count).apply()
        NovaVpnService.logDebug("Settings: Split Tunneling rules applied to $count apps")
    }

    fun activatePremium() {
        _isPremiumUser.value = true
        prefs.edit().putBoolean("is_premium", true).apply()
        NovaVpnService.logDebug("Premium Mode activated! Nova Hyper-Speed enabled.")
    }

    fun resetPremium() {
        _isPremiumUser.value = false
        prefs.edit().putBoolean("is_premium", false).apply()
        NovaVpnService.logDebug("Premium Mode reset to standard tiers.")
    }

    // Connect/Disconnect controls
    fun toggleVpnConnection() {
        val server = selectedServer.value ?: return
        val state = connectionState.value
        
        if (state == NovaVpnService.VpnState.CONNECTED || state == NovaVpnService.VpnState.CONNECTING) {
            triggerDisconnect()
        } else {
            triggerConnect(server)
        }
    }

    fun triggerConnect(server: VpnServer) {
        // Enforce customized parameters based on profile details
        val configuredServer = if (dnsMode.value != "Automatic") {
            val finalDns = if (dnsMode.value == "Custom") customDns.value else if (dnsMode.value == "Google") "8.8.8.8" else "1.1.1.1"
            server.copy(dns = finalDns)
        } else {
            server
        }

        val intent = Intent(getApplication(), NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_CONNECT
            putExtra(NovaVpnService.EXTRA_SERVER, configuredServer)
        }
        getApplication<Application>().startService(intent)
    }

    fun triggerDisconnect() {
        val intent = Intent(getApplication(), NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_DISCONNECT
        }
        getApplication<Application>().startService(intent)
    }

    // Dynamic Fastest Smart Routing selection
    fun selectFastestServer() {
        val list = servers.value
        if (list.isNotEmpty()) {
            val fastest = list.minByOrNull { it.pingMs }
            if (fastest != null) {
                selectServer(fastest)
                NovaVpnService.logDebug("Dynamic latency evaluation: Lowest ping selected - ${fastest.name} (${fastest.pingMs}ms)")
            }
        }
    }

    // Import WireGuard Configuration file (.conf format parsing)
    fun importWireGuardConfig(fileName: String, content: String): Boolean {
        try {
            NovaVpnService.logDebug("Parsing configuration: $fileName")
            
            var privateKey = "mOOCeXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo22E4=" // Fallback defaults
            var ipAddress = "10.0.0.10"
            var clientIp = "10.0.0.10/24"
            var dns = "1.1.1.1"
            
            var publicKey = "bOOCeXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo22E4="
            var endpointHost = "127.0.0.1"
            var endpointPort = 51820
            var allowedIps = "0.0.0.0/0"
            
            val lines = content.lines()
            var currentSection = ""
            
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    currentSection = trimmed.lowercase(Locale.ROOT)
                    continue
                }
                if (trimmed.startsWith("#") || !trimmed.contains("=")) continue
                
                val key = trimmed.substringBefore("=").trim().lowercase(Locale.ROOT)
                val value = trimmed.substringAfter("=").trim()
                
                if (currentSection == "[interface]") {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "address" -> {
                            clientIp = value
                            ipAddress = value.substringBefore("/")
                        }
                        "dns" -> dns = value
                    }
                } else if (currentSection == "[peer]") {
                    when (key) {
                        "publickey" -> publicKey = value
                        "allowedips" -> allowedIps = value
                        "endpoint" -> {
                            endpointHost = value.substringBefore(":")
                            endpointPort = value.substringAfter(":", "51820").toIntOrNull() ?: 51820
                        }
                    }
                }
            }
            
            val cleanName = fileName.replace(".conf", "")
                .replace(Regex("[_-]"), " ")
                .replaceFirstChar { it.titlecase() }

            val newServer = VpnServer(
                id = "Imported-${System.currentTimeMillis()}",
                name = "Imported • $cleanName",
                countryCode = "UN", // Unified / Custom config flag
                ipAddress = endpointHost,
                loadPercent = 0,
                pingMs = 50 + (10..150).random(), // Set an initial fake ping value
                isFavorite = false,
                privateKey = privateKey,
                publicKey = publicKey,
                dns = dns,
                allowedIps = allowedIps,
                clientIp = clientIp,
                endpointPort = endpointPort
            )

            viewModelScope.launch {
                repository.insertServer(newServer)
                selectServer(newServer)
            }
            NovaVpnService.logDebug("Imported WireGuard profile: '$cleanName' parsed successfully and inserted into secure Room storage.")
            return true
        } catch (e: Exception) {
            NovaVpnService.logDebug("Failed to parse WireGuard config: ${e.message}")
            return false
        }
    }

    fun deleteDownloadedServer(server: VpnServer) {
        viewModelScope.launch {
            repository.deleteServer(server.id)
            NovaVpnService.logDebug("Deleted server profile: ${server.name}")
            // Reselect if matching
            if (_selectedServer.value?.id == server.id) {
                val currentList = servers.value.filter { it.id != server.id }
                if (currentList.isNotEmpty()) {
                    selectServer(currentList.first())
                } else {
                    _selectedServer.value = null
                }
            }
        }
    }

    fun syncServers() {
        viewModelScope.launch {
            NovaVpnService.logDebug("Fetching updated VPN directory from Cloud Endpoint...")
            val result = repository.syncFromRemote()
            if (result.isSuccess) {
                NovaVpnService.logDebug("Server directory synchronised successfully.")
            } else {
                NovaVpnService.logDebug("Direct cloud sync offline. Keeping local room configurations cached.")
            }
        }
    }

    fun addLiveDebugLog(msg: String) {
        NovaVpnService.logDebug(msg)
    }
}
