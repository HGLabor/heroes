package gg.norisk.heroes.server.database.player

import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.database.player.IPlayerProvider
import gg.norisk.heroes.common.database.player.JsonPlayerProvider
import gg.norisk.heroes.common.player.DatabasePlayer
import gg.norisk.heroes.server.database.MongoManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.task.mcCoroutineTask
import java.util.*

object PlayerProvider : IPlayerProvider {
    private var provider: IPlayerProvider = JsonPlayerProvider()

    override fun init() {
        (registeredTypes as MutableMap<Any, Any>).put(
            DatabasePlayer::class,
            DatabasePlayer.serializer()
        )

        ServerLifecycleEvents.SERVER_STARTING.register {
            runCatching {
                MongoManager.connect()
            }.onSuccess {
                provider = MongoPlayerProvider()
                logger.info("Initialized provider: ${provider::class.simpleName}")
            }.onFailure {
                it.printStackTrace()
                MongoManager.isConnected = false
                provider = JsonPlayerProvider()
            }
            logger.info("Initialized provider: ${provider::class.simpleName}")
        }

        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, sender, server ->
            mcCoroutineTask(sync = false, client = false) {
                onPlayerJoin(handler.player)
            }
        })

        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, server ->
            mcCoroutineTask(sync = false, client = false) {
                onPlayerLeave(handler.player)
            }
        })
    }

    override suspend fun save(player: DatabasePlayer) {
        provider.save(player)
    }

    override suspend fun save(uuid: UUID) {
        provider.save(uuid)
    }

    override suspend fun findPlayer(uuid: UUID): DatabasePlayer? {
        return provider.findPlayer(uuid)
    }

    override fun getCachedPlayer(uuid: UUID): DatabasePlayer? {
        return provider.getCachedPlayer(uuid)
    }

    override fun getCachedPlayerOrDummy(uuid: UUID): DatabasePlayer {
        return provider.getCachedPlayerOrDummy(uuid)
    }

    override suspend fun get(uuid: UUID): DatabasePlayer {
        return provider.get(uuid)
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        provider.onPlayerJoin(player)
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        provider.onPlayerLeave(player)
    }
}
