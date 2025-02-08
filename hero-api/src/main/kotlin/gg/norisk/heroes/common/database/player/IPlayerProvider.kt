package gg.norisk.heroes.common.database.player

import gg.norisk.heroes.common.player.DatabasePlayer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

interface IPlayerProvider {
    fun init(): IPlayerProvider

    suspend fun save(player: DatabasePlayer)
    suspend fun save(uuid: UUID)
    suspend fun findPlayer(uuid: UUID): DatabasePlayer?
    fun getCachedPlayer(uuid: UUID): DatabasePlayer?
    fun getCachedPlayerOrDummy(uuid: UUID): DatabasePlayer
    suspend fun get(uuid: UUID): DatabasePlayer

    suspend fun onPlayerJoin(player: ServerPlayerEntity)
    suspend fun onPlayerLeave(player: ServerPlayerEntity)
}
