package gg.norisk.heroes.common.db

import gg.norisk.datatracker.entity.EntityWrapper
import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.registeredTypes
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.isServer
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.hero.getHero
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.WorldSavePath
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.s2cPacket

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
        ServerLivingEntityEvents.AFTER_DEATH.register(ServerLivingEntityEvents.AfterDeath { entity, damageSource ->
            handleAfterDeath(damageSource, entity)
        })
    }

    fun handleAfterDeath(damageSource: DamageSource, entity: Entity) {
        val attacker = damageSource.attacker as? ServerPlayerEntity?
        if (attacker?.getHero() != null) {
            val cachedAttacker = provider.getCachedPlayer(attacker.uuid)
            cachedAttacker.kills++
            cachedAttacker.currentKillStreak++
            attacker.dbPlayer = cachedAttacker
            if (cachedAttacker.currentKillStreak.mod(5) == 0) {
                if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
                    attacker.server.broadcastText(
                        Text.translatable(
                            "heroes.announce_kill_streak",
                            attacker.gameProfile.name,
                            cachedAttacker.currentKillStreak
                        )
                    )
                }
            }
            mcCoroutineTask(sync = false, client = false) {
                provider.save(cachedAttacker)
            }

            ExperienceManager.addXp(attacker, ExperienceManager.KILLED_PLAYER, true)
        }
        //das als config?

        if (entity is ServerPlayerEntity && entity.getHero() != null) {
            var heroDeathEvent = HeroEvents.HeroDeathEvent(entity, true)
            if (isServer) {
                logger.info("$entity ist gestorben")
                logger.info("$entity ist gestorben")
                logger.info("$entity ist gestorben")
                logger.info("$entity ist gestorben")
                entity.sendMessage("Du bist gestorben".literal)
            }
            HeroEvents.heroDeathEvent.invoke(heroDeathEvent)
            val cachedEntity = provider.getCachedPlayer(entity.uuid)
            if (heroDeathEvent.isValidDeath) {
                cachedEntity.deaths++
                if (cachedEntity.currentKillStreak > cachedEntity.highestKillStreak) {
                    cachedEntity.highestKillStreak = cachedEntity.currentKillStreak
                }
                if (cachedEntity.currentKillStreak > 5) {
                    if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
                        entity.server.broadcastText(
                            Text.translatable(
                                "heroes.end_kill_streak",
                                entity.gameProfile.name,
                                cachedEntity.currentKillStreak
                            )
                        )
                    }
                }
                cachedEntity.currentKillStreak = 0

                entity.dbPlayer = cachedEntity
                mcCoroutineTask(sync = false, client = false) {
                    provider.save(cachedEntity)
                }
            }

            if (!damageSource.isOf(DamageTypes.GENERIC_KILL)) {
                ExperienceManager.addXp(entity, ExperienceManager.PLAYER_DEATH, true)
            }
        }
    }

    private const val DATABASE_PLAYER = "HeroApi:DataBasePlayer"

    var PlayerEntity.dbPlayer: DatabasePlayer
        get() = this.getSyncedData<DatabasePlayer>(DATABASE_PLAYER) ?: DatabasePlayer(this.uuid)
        set(value) = this.setSyncedData(DATABASE_PLAYER, value)
}