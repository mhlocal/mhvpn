package com.example.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

data class VpnServerResponse(
    val id: String,
    val name: String,
    val countryCode: String,
    val ipAddress: String,
    val loadPercent: Int,
    val pingMs: Int,
    val privateKey: String,
    val publicKey: String,
    val dns: String,
    val allowedIps: String,
    val clientIp: String,
    val endpointPort: Int
)

interface VpnApiService {
    @GET("api/v1/servers")
    suspend fun getActiveServers(
        @Header("Authorization") token: String? = null
    ): List<VpnServerResponse>
}
