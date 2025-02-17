package gg.norisk.heroes.common.database.player

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.player.FFAPlayer
import gg.norisk.heroes.common.utils.createIfNotExists
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.util.*

class JsonPlayerProvider : AbstractPlayerProvider() {
    private val file get() = HeroesManager.baseDirectory.resolve("player-database.json")

    private suspend fun loadDatabase(): MutableSet<FFAPlayer> {
        var database = mutableSetOf<FFAPlayer>()
        runCatching {
            if (file.exists()) {
                database = JSON.decodeFromString(file.readText())
            }
        }.onFailure {
            if (file.readText().isBlank()) {
                file.writeText("[]")
            }
            logger.error("Error Reading ${file.absolutePath}")
            it.printStackTrace()
        }
        return database
    }

    private suspend fun findPlayer(uuid: UUID): FFAPlayer? {
        val database = loadDatabase()
        return database.find { it.uuid == uuid }
    }

    override suspend fun get(uuid: UUID): FFAPlayer {
        val player = getCached(uuid) ?: findPlayer(uuid) ?: FFAPlayer(uuid)
        cache[uuid] = player
        return player
    }

    override suspend fun save(data: FFAPlayer) {
        val database = loadDatabase()
        database.removeIf { it.uuid == data.uuid }
        database.add(data.copy(inventory = null))
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        file.writeText(JSON.encodeToString(database.map { it.copy(inventory = null) }))
        logger.info("Saved Database Player for ${data.uuid} to $file")
    }
}
