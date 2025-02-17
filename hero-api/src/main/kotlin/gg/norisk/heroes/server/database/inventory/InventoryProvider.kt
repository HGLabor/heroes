package gg.norisk.heroes.server.database.inventory

import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.database.inventory.AbstractInventoryProvider
import gg.norisk.heroes.common.database.inventory.JsonInventoryProvider
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.utils.PlayStyle
import gg.norisk.heroes.server.database.MongoManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object InventoryProvider : AbstractInventoryProvider(PlayStyle.current) {
    private var provider: AbstractInventoryProvider = JsonInventoryProvider()

    fun init() {
        (registeredTypes as MutableMap<Any, Any>).put(
            InventorySorting::class,
            InventorySorting.serializer()
        )

        ServerLifecycleEvents.SERVER_STARTING.register {
            runCatching {
                MongoManager.connect()
            }.onSuccess {
                provider = MongoInventoryProvider()
                logger.info("Initialized provider: ${provider::class.simpleName}")
            }.onFailure {
                it.printStackTrace()
                MongoManager.isConnected = false
                provider = JsonInventoryProvider()
            }
            logger.info("Initialized provider: ${provider::class.simpleName}")
        }
    }

    override suspend fun save(data: InventorySorting?) {
        provider.save(data)
    }

    override suspend fun get(uuid: UUID): InventorySorting? {
        return provider.get(uuid)
    }

    override suspend fun onPlayerJoin(player: ServerPlayerEntity) {
        provider.onPlayerJoin(player)
    }

    override suspend fun onPlayerLeave(player: ServerPlayerEntity) {
        provider.onPlayerLeave(player)
    }
}
