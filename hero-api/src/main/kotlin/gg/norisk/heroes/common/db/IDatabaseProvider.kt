package gg.norisk.heroes.common.db

import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

interface IDatabaseProvider {
    fun init()
    suspend fun onPlayerJoin(player: ServerPlayerEntity)
    suspend fun onPlayerLeave(player: ServerPlayerEntity)
    suspend fun save(player: DatabasePlayer)
    suspend fun save(uuid: UUID)
    suspend fun getPlayer(uuid: UUID): DatabasePlayer?
    fun getCachedPlayer(uuid: UUID): DatabasePlayer
}