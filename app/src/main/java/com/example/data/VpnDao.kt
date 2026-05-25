package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnDao {
    @Query("SELECT * FROM vpn_servers ORDER BY isFavorite DESC, pingMs ASC")
    fun getAllServersFlow(): Flow<List<VpnServer>>

    @Query("SELECT * FROM vpn_servers ORDER BY isFavorite DESC, pingMs ASC")
    suspend fun getAllServers(): List<VpnServer>

    @Query("SELECT * FROM vpn_servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): VpnServer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<VpnServer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: VpnServer)

    @Query("UPDATE vpn_servers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE vpn_servers SET pingMs = :pingMs WHERE id = :id")
    suspend fun updatePing(id: String, pingMs: Int)

    @Query("DELETE FROM vpn_servers WHERE id = :id")
    suspend fun deleteServerById(id: String)

    @Query("DELETE FROM vpn_servers")
    suspend fun clearAll()
}
