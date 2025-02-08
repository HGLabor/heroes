package gg.norisk.heroes.common.database.player

import de.hglabor.utils.core.extensions.createIfNotExists
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.player.DatabasePlayer
import gg.norisk.heroes.common.player.dbPlayer
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class JsonPlayerProvider : IPlayerProvider {
    private val cache = HashMap<UUID, DatabasePlayer>()
    private val file = HeroesManager.baseDirectory.resolve("player-database.json").createIfNotExists()

    private fun loadDatabase(): MutableSet<DatabasePlayer> {
        var database = mutableSetOf<DatabasePlayer>()
        runCatching {
            if (file.exists()) {
                database = JSON.decodeFromString(file.readText())
            }
        }.onFailure {
            it.printStackTrace()
        }
        return database
    }

    override fun init(): IPlayerProvider {
        return this
    }

    override suspend fun findPlayer(uuid: UUID): DatabasePlayer? {
        val database = loadDatabase()
        return database.find { it.uuid == uuid }
    }

    override fun getCachedPlayer(uuid: UUID): DatabasePlayer? {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && !FabricLoader.getInstance().isDevelopmentEnvironment) {
            val dbPlayer = MinecraftClient.getInstance().world?.getPlayerByUuid(uuid)?.dbPlayer
            if (dbPlayer != null) {
                return dbPlayer
            }
        }
        return cache[uuid]
    }

    override fun getCachedPlayerOrDummy(uuid: UUID): DatabasePlayer {
        return getCachedPlayer(uuid) ?: DatabasePlayer(uuid)
    }

    override suspend fun get(uuid: UUID): DatabasePlayer {
        val player = getCachedPlayer(uuid) ?: findPlayer(uuid) ?: DatabasePlayer(uuid)
        cache[uuid] = player
        return player
    }

    override suspend fun save(player: DatabasePlayer) {
        val database = loadDatabase()
        database.removeIf { it.uuid == player.uuid }
        database.add(player)
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        file.writeText(JSON.encodeToString(database))
        logger.info("Saved Database Player for ${player.uuid} to $file")
    }

    override suspend fun save(uuid: UUID) {
        save(get(uuid))
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        val dbPlayer = get(player.uuid)
        cache[player.uuid] = dbPlayer
        player.dbPlayer = dbPlayer
        logger.info("Loaded Database Player ${player.gameProfile.name}")
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        if (cache.containsKey(player.uuid)) {
            save(cache.computeIfAbsent(player.uuid) { DatabasePlayer(player.uuid) })
            cache.remove(player.uuid)
            logger.info("Saving Database Player ${player.gameProfile.name}")
        } else {
            logger.warn("Cache didn't contain any data about ${player.gameProfile.name} (${player.gameProfile.id}), not saving any data")
        }
    }
}
