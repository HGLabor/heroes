package gg.norisk.heroes.common.db

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.logger
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.WorldSavePath
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask

object DatabaseManager {
    var provider: IDatabaseProvider = JsonProvider

    fun init() {
        (registeredTypes as MutableMap<Any, Any>).put(
            DatabasePlayer::class,
            DatabasePlayer.serializer()
        )

        ServerLifecycleEvents.SERVER_STARTING.register {
            JsonProvider.serverPath = it.getSavePath(WorldSavePath("heroes"))
            logger.info("Found Server Path: ${JsonProvider.serverPath}")
        }
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, sender, server ->
            mcCoroutineTask(sync = false, client = false) {
                provider.onPlayerJoin(handler.player)
                mcCoroutineTask(sync = false, client = false, delay = 5.ticks) {
                    handler.player.dbPlayer = provider.getCachedPlayer(handler.player.uuid)
                }
            }
        })
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, server ->
            mcCoroutineTask(sync = false, client = false) {
                provider.onPlayerLeave(handler.player)
            }
        })
    }

    private const val DATABASE_PLAYER = "HeroApi:DataBasePlayer"
    private const val FFA_BOUNTY = "HeroApi:Bounty"
    var PlayerEntity.dbPlayer: DatabasePlayer
        get() = this.getSyncedData<DatabasePlayer>(DATABASE_PLAYER) ?: DatabasePlayer(this.uuid)
        set(value) = this.setSyncedData(DATABASE_PLAYER, value, (this as? ServerPlayerEntity?))

    var PlayerEntity.ffaBounty: Int
        get() = this.getSyncedData<Int>(FFA_BOUNTY) ?: 0
        set(value) = this.setSyncedData(FFA_BOUNTY, value)
}