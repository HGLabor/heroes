package gg.norisk.heroes.common.database.inventory

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.utils.PlayStyle
import gg.norisk.heroes.common.utils.createIfNotExists
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.util.*

class JsonInventoryProvider : AbstractInventoryProvider(PlayStyle.current) {
    private val file = HeroesManager.baseDirectory.resolve("player-inventory-database.json").createIfNotExists()

    private fun loadDatabase(): MutableSet<InventorySorting> {
        var database = mutableSetOf<InventorySorting>()
        runCatching {
            if (file.exists()) {
                database = JSON.decodeFromString(file.readText())
            }
        }.onFailure {
            it.printStackTrace()
        }
        return database
    }
    private suspend fun find(uuid: UUID): InventorySorting? {
        val database = loadDatabase()
        return database.find { it.userId == uuid && it.playStyle == playStyle }
    }

    override suspend fun get(uuid: UUID): InventorySorting? {
        val inventory = getCached(uuid) ?: find(uuid)
        cache[uuid] = inventory
        return inventory
    }

    override suspend fun save(data: InventorySorting?) {
        if (data == null) {
            logger.info("Cant save Inventory `null`")
            return
        }

        if (data.main.isEmpty() && data.armor.isEmpty() && data.offhand.isEmpty()) {
            logger.info("${data.userId}'s inventory is empty. not saving")
            return
        }
        val database = loadDatabase()
        database.removeIf { it.userId == data.userId }
        database.add(data)
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        file.writeText(JSON.encodeToString(database))
        logger.info("Saved InventorySorting for ${data.userId} to $file")
    }
}
