package gg.norisk.heroes.common.db

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.WorldSavePath
import java.io.File
import java.nio.file.Path
import java.util.*

object JsonProvider : IDatabaseProvider {
    private val cache = mutableMapOf<UUID, DatabasePlayer>()

    val baseFolder
        get() = File(
            System.getProperty(
                "hero_folder_path",
                defaultPath.toFile().absolutePath
            ),
        ).apply {
            mkdirs()
        }

    var serverPath: Path? = null

    val defaultPath
        get() = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            FabricLoader.getInstance().configDir
        } else {
            serverPath ?: FabricLoader.getInstance().configDir
        }

    private val folder
        get() = File(baseFolder, "heroes").apply {
            mkdirs()
        }

    private val file
        get() = File(folder, "database.json")

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

    override fun init() {
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        val dbPlayer = getPlayer(player.uuid) ?: DatabasePlayer(player.uuid)
        cache[player.uuid] = dbPlayer
        player.dbPlayer = dbPlayer
        logger.info("Loaded Database Player ${player.gameProfile.name}")
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        save(cache.computeIfAbsent(player.uuid) { DatabasePlayer(player.uuid) })
        cache.remove(player.uuid)
        logger.info("Saving Database Player ${player.gameProfile.name}")
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
        save(getCachedPlayer(uuid))
    }

    override suspend fun getPlayer(uuid: UUID): DatabasePlayer? {
        val database = loadDatabase()
        return database.find { it.uuid == uuid }
    }

    override fun getCachedPlayer(uuid: UUID): DatabasePlayer {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT && !FabricLoader.getInstance().isDevelopmentEnvironment) {
            val dbPlayer = MinecraftClient.getInstance().world?.getPlayerByUuid(uuid)?.dbPlayer
            if (dbPlayer != null) {
                return dbPlayer
            }
        }
        return cache.computeIfAbsent(uuid) { DatabasePlayer(uuid) }
    }
}