package gg.norisk.heroes.server.database.player

import de.hglabor.utils.database.extensions.findOne
import de.hglabor.utils.database.extensions.getOrCreateCollection
import de.hglabor.utils.database.extensions.save
import de.hglabor.utils.database.kmongo.eq
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.player.DatabasePlayer
import gg.norisk.heroes.common.database.player.IPlayerProvider
import gg.norisk.heroes.common.player.dbPlayer
import gg.norisk.heroes.server.database.MongoManager
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class MongoPlayerProvider : IPlayerProvider {
    private val cache = HashMap<UUID, DatabasePlayer>()
    private val collection = MongoManager.database.getOrCreateCollection<DatabasePlayer>("players")

    override fun init(): IPlayerProvider {
        return this
    }

    override suspend fun findPlayer(uuid: UUID): DatabasePlayer? {
        return collection.findOne(DatabasePlayer::uuid eq uuid)
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
        cache[player.uuid] = player
        collection.save(player)
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
