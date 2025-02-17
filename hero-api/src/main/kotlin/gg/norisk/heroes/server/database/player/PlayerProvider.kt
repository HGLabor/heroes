package gg.norisk.heroes.server.database.player

import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.database.player.AbstractPlayerProvider
import gg.norisk.heroes.common.database.player.JsonPlayerProvider
import gg.norisk.heroes.common.player.FFAPlayer
import gg.norisk.heroes.server.database.MongoManager
import gg.norisk.heroes.server.database.inventory.InventoryProvider
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.silkmc.silk.core.task.mcCoroutineTask
import java.util.*

object PlayerProvider : AbstractPlayerProvider() {
    private var provider: AbstractPlayerProvider = JsonPlayerProvider()

    fun init() {
        (registeredTypes as MutableMap<Any, Any>).put(
            FFAPlayer::class,
            FFAPlayer.serializer()
        )

        ServerLifecycleEvents.SERVER_STARTING.register {
            runCatching {
                MongoManager.connect()
            }.onSuccess {
                provider = MongoPlayerProvider()
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
                InventoryProvider.onPlayerLeave(handler.player)
            }
        })
    }

    override suspend fun save(player: FFAPlayer) {
        provider.save(player)
        InventoryProvider.save(player.inventorySorting)
    }

    override suspend fun get(uuid: UUID): FFAPlayer {
        val player = provider.get(uuid)
        player.inventorySorting = InventoryProvider.get(uuid)
        return player
    }
}
