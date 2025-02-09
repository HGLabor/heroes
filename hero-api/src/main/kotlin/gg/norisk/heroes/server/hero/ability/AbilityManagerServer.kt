package gg.norisk.heroes.server.hero.ability

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.ffa.experience.ExperienceRegistry
import gg.norisk.heroes.common.ffa.experience.addXp
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.ability.*
import gg.norisk.heroes.common.hero.ability.implementation.Ability
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.hero.getHero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.Networking.c2sAbilityPacket
import gg.norisk.heroes.common.networking.Networking.c2sSkillProperty
import gg.norisk.heroes.common.networking.Networking.s2cAbilityPacket
import gg.norisk.heroes.common.player.dbPlayer
import gg.norisk.heroes.server.database.player.PlayerProvider
import kotlinx.coroutines.*
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.silkmc.silk.core.server.players
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.task.mcSyncLaunch
import java.util.*
import kotlin.time.Duration.Companion.seconds

object AbilityManagerServer : IAbilityManager {
    private val abilitiesInUse = hashMapOf<UUID, AbstractAbility<*>>()
    private val abilityJobs: HashMap<UUID, HashMap<AbstractAbility<*>, Job>> = hashMapOf()

    override fun init() {
        c2sAbilityPacket.receiveOnServer { packet, context ->
            mcCoroutineTask(sync = true, client = false) {
                handleIncomingAbility(packet, context.player)
            }
        }

        c2sSkillProperty.receiveOnServer { packet, context ->
            mcCoroutineTask(sync = true, client = false) {
                skillProperty(packet, context.player)
            }
        }

        initCooldownManager()
    }

    private fun initCooldownManager() {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick {
                sendCooldownToPlayers(it)
            })
        } else {
            ServerLifecycleEvents.SERVER_STARTED.register {
                infiniteMcCoroutineTask(sync = false, client = false) {
                    sendCooldownToPlayers(it)
                }
            }
        }
    }

    private fun sendCooldownToPlayers(server: MinecraftServer) {
        for (player in server.players) {
            val hero = player.getHero() ?: continue
            for (ability in hero.abilities.values) {
                val cooldown = ability.getCooldown(player) ?: continue
                cooldown.durationString = ability.getCooldownText(cooldown)
                Networking.s2cCooldownPacket.send(cooldown, player)
            }
        }
    }

    private suspend fun skillProperty(packet: SkillPropertyPacket, player: ServerPlayerEntity) {
        val hero = HeroManager.getHero(packet.heroKey) ?: return
        val ability = hero.abilities[packet.abilityKey] ?: return
        val property = ability.getAllProperties().find { it.internalKey == packet.propertyKey } ?: return
        val cachedPlayer = PlayerProvider.get(player.uuid)

        val oldLevel = property.getLevelInfo(player.uuid)

        val experienceToSpend = Math.min(500, cachedPlayer.xp)
        val spentExperience = property.addExperience(player.uuid, experienceToSpend)
        cachedPlayer.xp -= spentExperience

        val newLevel = property.getLevelInfo(player.uuid)

        logger.info("OldLevel $oldLevel CurrentLevel: $newLevel")
        if (oldLevel.currentLevel != newLevel.currentLevel) {
            //upgrade
            if (newLevel.currentLevel == newLevel.maxLevel) {
                player.playSoundToPlayer(SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.MASTER, 1f, 1f)
            } else {
                player.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 0.5f, 1f)
            }
        }

        logger.info("Spent Experience $spentExperience $experienceToSpend ${cachedPlayer.xp}")
        player.dbPlayer = cachedPlayer
        PlayerProvider.save(cachedPlayer)
    }

    private fun handleIncomingAbility(packet: AbilityPacket<*>, player: ServerPlayerEntity) {
        runCatching {
            var ignoreCooldown = false
            if (packet.playerUuid != player.uuid) return@runCatching
            val ability = getAbilityFromAbilityUsePacket(packet) ?: return@runCatching
            val description = packet.description
            val abilityScope = AbilityScope(player)
            if (!ability.hasUnlocked(player)) {
                player.sendMessage(Text.translatable("heroes.ability.locked").withColor(Colors.RED))
                return@runCatching
            }
            val condition = ability.condition
            if (condition != null && ability !is ToggleAbility) {
                if (!condition.invoke(player)) {
                    player.sendMessage(Text.translatable("heroes.ability.condition.long.${ability.internalKey}"))
                    return@runCatching
                }
            }
            when (ability) {
                is PressAbility,
                is Ability -> {
                    if (ability.handleCooldown(player)) return@runCatching
                    player.addXp(ExperienceRegistry.SMALL_ABILITY_USE, true)
                    ability.onStart(player, abilityScope)
                }

                is ToggleAbility -> {
                    val callbacks = when (description) {
                        is AbilityPacketDescription.Start -> {
                            if (condition != null) {
                                if (!condition.invoke(player)) {
                                    player.sendMessage(Text.translatable("heroes.ability.condition.long.${(ability).internalKey}"))
                                    return@runCatching
                                }
                            }
                            if (ability.handleCooldown(player)) return@runCatching
                            startAbilityAndForceEndAfterMaxDuration(player, abilityScope, ability)
                            ignoreCooldown = true
                            player.addXp(ExperienceRegistry.SMALL_ABILITY_USE, true)
                            ability.onStart(player, abilityScope)
                            //ability.internalCallbacks.START
                        }

                        is AbilityPacketDescription.Use -> {
                            ignoreCooldown = true
                            ability.onUse(player)
                            //ability.internalCallbacks.USE
                        }

                        is AbilityPacketDescription.End -> {
                            if (abilityJobs[player.uuid]?.containsKey(ability) == false) return@runCatching
                            forceEndAbility(player, ability)
                            return
                        }
                    }
                    //callbacks?.handleServer?.invoke(abilityScope, player, packet.description)
                }
            }
            if (abilityScope.broadcastPacket) {
                s2cAbilityPacket.sendToAll(packet)
            } else {
                s2cAbilityPacket.send(packet, player)
            }
            if (!ignoreCooldown && abilityScope.applyCooldown) {
                ability.addCooldown(player)
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun startAbilityAndForceEndAfterMaxDuration(
        player: ServerPlayerEntity,
        abilityScope: AbilityScope,
        ability: ToggleAbility
    ) {
        val playerJobs = abilityJobs.computeIfAbsent(player.uuid) { hashMapOf() }
        abilitiesInUse[player.uuid] = ability
        playerJobs[ability] = mcCoroutineTask(sync = false, client = false) {
            delay(ability.maxDurationProperty.getValue(player.uuid).seconds)
            yield()
            mcCoroutineTask(sync = true, client = false) {
                forceEndAbility(player, ability, abilityScope)
            }
        }
    }

    fun clear(player: PlayerEntity) {
        abilityJobs[player.uuid]?.forEach { (ability, job) ->
            job.cancel()
        }
        abilitiesInUse.remove(player.uuid)
    }

    fun forceEndAbility(
        player: ServerPlayerEntity,
        ability: ToggleAbility,
        abilityScope: AbilityScope = AbilityScope(player)
    ) {
        val job = abilityJobs[player.uuid]?.get(ability) ?: return
        job.cancel()
        abilitiesInUse.remove(player.uuid)
        abilityJobs[player.uuid]?.remove(ability)
        val description = AbilityPacketDescription.End
        val packet = AbilityPacket(player.uuid, ability.hero.internalKey, ability.internalKey, description)
        s2cAbilityPacket.sendToAll(packet)
        val abilityEndInformation = ToggleAbility.AbilityEndInformation(true)
        ability.onEnd(player, abilityEndInformation)
        if (abilityEndInformation.applyCooldown) {
            ability.addCooldown(player)
        }
    }

    private fun getAbilityFromAbilityUsePacket(abilityPacket: AbilityPacket<out AbilityPacketDescription>): AbstractAbility<*>? {
        val hero = HeroManager.getHero(abilityPacket.heroKey) ?: return null
        return hero.abilities[abilityPacket.abilityKey]
    }

    override fun useAbility(
        player: PlayerEntity,
        hero: Hero,
        ability: AbstractAbility<*>,
        description: AbilityPacketDescription.Use
    ): Boolean {
        throw IllegalCallerException("AbilityManager.useAbility(AbstractAbility<*>, AbilityPacketDescription.Use) must not be used on the server side")
    }

    override fun useAbility(
        player: PlayerEntity,
        ability: AbstractAbility<*>,
        description: AbilityPacketDescription.Use
    ) {
        val hero = player.getHero() ?: return
        if (hero != ability.hero) return
        val packet = AbilityPacket(player.uuid, hero.internalKey, ability.internalKey, description)
        mcCoroutineTask(sync = true, client = false) {
            handleIncomingAbility(packet, player as? ServerPlayerEntity ?: return@mcCoroutineTask)
        }
    }

    override fun registerAbility(ability: AbstractAbility<*>) {}

    override fun isUsingAbility(player: PlayerEntity, ability: AbstractAbility<*>): Boolean {
        return abilitiesInUse[player.uuid]?.internalKey == ability.internalKey
    }
}
