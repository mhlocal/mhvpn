package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class VpnRepository(private val context: Context) {
    private val database = VpnDatabase.getDatabase(context)
    private val vpnDao = database.vpnDao()

    val allServersFlow: Flow<List<VpnServer>> = vpnDao.getAllServersFlow()

    // Retrofit service setup
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.novavpn.cyber/") // Example simulated backend endpoint
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: VpnApiService = retrofit.create(VpnApiService::class.java)

    suspend fun getServersDirectly(): List<VpnServer> {
        return vpnDao.getAllServers()
    }

    suspend fun initializePresetServers() {
        val currentServers = vpnDao.getAllServers()
        if (currentServers.isEmpty()) {
            Log.d("VpnRepository", "Initializing default premium servers...")
            val defaults = listOf(
                VpnServer(
                    id = "DE-Frankfurt-01",
                    name = "Frankfurt • CyberDome",
                    countryCode = "DE",
                    ipAddress = "185.190.140.42",
                    loadPercent = 42,
                    pingMs = 15,
                    isFavorite = true,
                    privateKey = "mOOCeXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo22E4=",
                    publicKey = "bOOCeXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo22E4=",
                    dns = "1.1.1.1",
                    allowedIps = "0.0.0.0/0",
                    clientIp = "10.0.0.2/24",
                    endpointPort = 51820
                ),
                VpnServer(
                    id = "US-Seattle-02",
                    name = "Seattle • Pacific Grid",
                    countryCode = "US",
                    ipAddress = "192.241.211.89",
                    loadPercent = 18,
                    pingMs = 38,
                    isFavorite = false,
                    privateKey = "aP9fXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo11D3=",
                    publicKey = "xO8eYhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo55E5=",
                    dns = "8.8.8.8",
                    allowedIps = "0.0.0.0/0",
                    clientIp = "10.0.0.3/24",
                    endpointPort = 51820
                ),
                VpnServer(
                    id = "JP-Tokyo-03",
                    name = "Tokyo • SolarShade",
                    countryCode = "JP",
                    ipAddress = "210.140.10.155",
                    loadPercent = 65,
                    pingMs = 54,
                    isFavorite = false,
                    privateKey = "oU7cxXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo33F4=",
                    publicKey = "kI9fYhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo44G3=",
                    dns = "1.1.1.1",
                    allowedIps = "0.0.0.0/0",
                    clientIp = "10.0.0.4/24",
                    endpointPort = 51820
                ),
                VpnServer(
                    id = "SG-Singapore-04",
                    name = "Singapore • Nexus-X",
                    countryCode = "SG",
                    ipAddress = "45.112.5.30",
                    loadPercent = 29,
                    pingMs = 24,
                    isFavorite = true,
                    privateKey = "qO8wXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo11Z8=",
                    publicKey = "mF5gYhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo99K9=",
                    dns = "1.1.1.1, 8.8.8.8",
                    allowedIps = "0.0.0.0/0",
                    clientIp = "10.0.0.5/24",
                    endpointPort = 443
                ),
                VpnServer(
                    id = "UK-London-05",
                    name = "London • Sovereign Gate",
                    countryCode = "GB",
                    ipAddress = "82.165.10.29",
                    loadPercent = 88,
                    pingMs = 19,
                    isFavorite = false,
                    privateKey = "uP4eXhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo77B2=",
                    publicKey = "wO3dYhY6uR1I6fPl2F7T+X6sZ8rUeMlw139Mlo88C1=",
                    dns = "9.9.9.9",
                    allowedIps = "0.0.0.0/0",
                    clientIp = "10.0.0.6/24",
                    endpointPort = 51820
                )
            )
            vpnDao.insertServers(defaults)
        }
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        vpnDao.updateFavorite(id, isFavorite)
    }

    suspend fun updatePing(id: String, pingMs: Int) {
        vpnDao.updatePing(id, pingMs)
    }

    suspend fun insertServer(server: VpnServer) {
        vpnDao.insertServer(server)
    }

    suspend fun deleteServer(id: String) {
        vpnDao.deleteServerById(id)
    }

    // Sync from Simulated Web API
    suspend fun syncFromRemote(): Result<Unit> {
        return try {
            // Note: This API call simulates a physical network connection.
            // Under normal simulation conditions we will catch connection errors and fall back gracefully.
            val responses = apiService.getActiveServers()
            val entities = responses.map {
                VpnServer(
                    id = it.id,
                    name = it.name,
                    countryCode = it.countryCode,
                    ipAddress = it.ipAddress,
                    loadPercent = it.loadPercent,
                    pingMs = it.pingMs,
                    privateKey = it.privateKey,
                    publicKey = it.publicKey,
                    dns = it.dns,
                    allowedIps = it.allowedIps,
                    clientIp = it.clientIp,
                    endpointPort = it.endpointPort
                )
            }
            vpnDao.insertServers(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("VpnRepository", "API Sync failed, using local Room persistence", e)
            Result.failure(e)
        }
    }
}
