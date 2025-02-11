package gg.norisk.heroes.common.database.player

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.database.AbstractProvider
import gg.norisk.heroes.common.player.FFAPlayer
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.server.database.inventory.InventoryProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

abstract class AbstractPlayerProvider : AbstractProvider<UUID, FFAPlayer>() {

    override fun getCachedClient(uuid: UUID): FFAPlayer? {
        val ffaPlayer = MinecraftClient.getInstance().world?.getPlayerByUuid(uuid)?.ffaPlayer
        return ffaPlayer
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        val ffaPlayer = get(player.uuid)
        cache[player.uuid] = ffaPlayer
        player.ffaPlayer = ffaPlayer
        logger.info("Loaded Database Player ${player.gameProfile.name}")

        InventoryProvider.onPlayerJoin(player)
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        if (cache.containsKey(player.uuid)) {
            save(cache.computeIfAbsent(player.uuid) { FFAPlayer(player.uuid) })
            cache.remove(player.uuid)
            logger.info("Saving Database Player ${player.gameProfile.name}")
        } else {
            logger.warn("Cache didn't contain any data about ${player.gameProfile.name} (${player.gameProfile.id}), not saving any data")
        }

        InventoryProvider.onPlayerLeave(player)
    }
}
