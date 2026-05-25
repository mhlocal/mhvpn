package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "vpn_servers")
data class VpnServer(
    @PrimaryKey val id: String,
    val name: String,
    val countryCode: String, // e.g. "US", "DE", "JP"
    val ipAddress: String,
    val loadPercent: Int,
    val pingMs: Int,
    val isFavorite: Boolean = false,
    val privateKey: String,
    val publicKey: String,
    val dns: String = "1.1.1.1",
    val allowedIps: String = "0.0.0.0/0",
    val clientIp: String = "10.0.0.2/24",
    val endpointPort: Int = 51820
) : Serializable
