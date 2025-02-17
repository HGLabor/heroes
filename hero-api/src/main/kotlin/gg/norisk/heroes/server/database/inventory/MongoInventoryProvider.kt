package gg.norisk.heroes.server.database.inventory

import com.mongodb.client.model.ReplaceOptions
import de.hglabor.utils.database.extensions.findOne
import de.hglabor.utils.database.extensions.getOrCreateCollection
import de.hglabor.utils.database.kmongo.and
import de.hglabor.utils.database.kmongo.eq
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.database.inventory.AbstractInventoryProvider
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.common.utils.PlayStyle
import gg.norisk.heroes.server.database.MongoManager
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class MongoInventoryProvider : AbstractInventoryProvider(PlayStyle.current) {
    private val collection = MongoManager.database.getOrCreateCollection<InventorySorting>("inventories")

    private suspend fun find(uuid: UUID): InventorySorting? {
        return collection.findOne(and(InventorySorting::userId eq uuid, InventorySorting::playStyle eq playStyle))
    }

    override suspend fun get(uuid: UUID): InventorySorting? {
        val player = getCached(uuid) ?: find(uuid)
        cache[uuid] = player
        return player
    }

    override suspend fun save(data: InventorySorting?) {
        if (data == null) {
            logger.info("Cant save Inventory `null`")
            return
        }
        cache[data.userId] = data
        collection.replaceOne(
            and(InventorySorting::userId eq data.userId, InventorySorting::playStyle eq playStyle),
            data,
            ReplaceOptions().upsert(true)
        )
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        val inventory = get(player.uuid)
        cache[player.uuid] = inventory
        player.ffaPlayer.inventorySorting = inventory
        logger.info("Loaded inventory of ${player.gameProfile.name}")
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        if (cache.containsKey(player.uuid)) {
            save(cache[player.uuid])
            cache.remove(player.uuid)
            logger.info("Saving inventory of ${player.gameProfile.name}")
        } else {
            logger.warn("Cache didn't contain any data about ${player.gameProfile.name} (${player.gameProfile.id}), not saving any data")
        }
    }
}
