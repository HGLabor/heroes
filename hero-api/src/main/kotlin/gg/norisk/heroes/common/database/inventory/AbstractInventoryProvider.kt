package gg.norisk.heroes.common.database.inventory

import gg.norisk.heroes.common.database.AbstractProvider
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.common.utils.PlayStyle
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

abstract class AbstractInventoryProvider(val playStyle: PlayStyle) : AbstractProvider<UUID, InventorySorting?>() {

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {}
    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {}

    override fun getCachedClient(uuid: UUID): InventorySorting? {
        val ffaPlayer = MinecraftClient.getInstance().world?.getPlayerByUuid(uuid)?.ffaPlayer
        return ffaPlayer?.inventory
    }
}
