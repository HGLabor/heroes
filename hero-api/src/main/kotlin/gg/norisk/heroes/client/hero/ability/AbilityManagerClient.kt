package gg.norisk.heroes.client.hero.ability

import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.ability.*
import gg.norisk.heroes.common.hero.ability.implementation.Ability
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.hero.getHero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.Networking.s2cAbilityPacket
import gg.norisk.heroes.common.networking.Networking.s2cCooldownPacket
import gg.norisk.utils.DevUtils.uniqueId
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.core.task.mcCoroutineTask
import java.util.*

object AbilityManagerClient : IAbilityManager {
    private val abilitiesInUse = hashMapOf<UUID, AbstractAbility<*>>()

    override fun init() {
        //cleanup
        syncedValueChangeEvent.listen { event ->
            if (event.key != HeroManager.HERO_KEY) return@listen
            val player = event.entity as? AbstractClientPlayerEntity? ?: return@listen
            if (player == MinecraftClient.getInstance().player) {
                (event.oldValue as? Hero?)?.abilities?.forEach { (name, ability) -> ability.onDisable(player) }
                abilitiesInUse.remove(player.uniqueId)
                HeroManager.registeredHeroes.values.forEach {
                    it.abilities.values.forEach { ability ->
                        ability.removeCooldown(player)
                    }
                }
                player.getHero()?.abilities?.forEach { (name, ability) -> ability.onEnable(player) }
            }
        }


        s2cCooldownPacket.receiveOnClient { packet, context ->
            mcCoroutineTask(sync = true, client = true) {
                val player =
                    context.client.world?.getEntityById(packet.entityId) as? PlayerEntity? ?: return@mcCoroutineTask
                val hero = HeroManager.getHero(packet.heroKey) ?: return@mcCoroutineTask
                val ability = hero.abilities[packet.abilityKey] ?: return@mcCoroutineTask
                ability.setCooldown(packet, player)
            }
        }

        s2cAbilityPacket.receiveOnClient { packet, context ->
            runCatching {
                val player = context.client.player ?: return@receiveOnClient
                val heroPlayer = context.client.world?.players?.firstOrNull { it.uuid == packet.playerUuid }
                    ?: return@receiveOnClient
                val ability = getAbilityFromAbilityPacket(packet) ?: return@receiveOnClient
                val description = packet.description
                val isOwnPacket = heroPlayer.uuid == player.uuid
                val abilityScope = AbilityScope(heroPlayer)
                when (ability) {
                    is Ability,
                    is PressAbility -> {
                        ability.onStart(player, abilityScope)
                        /*val callbacks = ability.internalCallbacks as AbstractAbility.ReceiveCallbacks
                        callbacks.handleAllClients?.invoke(heroPlayer, player, description)
                        if (isOwnPacket) {
                            callbacks.handleOwnClient?.invoke(player, description)
                        } else {
                            callbacks.handleOtherClients?.invoke(heroPlayer, player, description)
                        }*/
                    }

                    is ToggleAbility -> {
                        val callbacks = when (description) {
                            is AbilityPacketDescription.Start -> {
                                abilitiesInUse[packet.playerUuid] = ability
                                logger.info("Start")
                                ability.onStart(player, abilityScope)
                                //ability.internalCallbacks.START
                            }

                            is AbilityPacketDescription.Use -> {
                                ability.onUse(player)
                                //ability.internalCallbacks.USE
                            }

                            is AbilityPacketDescription.End -> {
                                abilitiesInUse.remove(packet.playerUuid)
                                ability.onEnd(player, ToggleAbility.AbilityEndInformation())
                                //(ability).internalCallbacks.END
                            }
                        }
                        //callbacks.handleAllClients?.invoke(heroPlayer, player, description)

                        if (isOwnPacket) {
                            //callbacks.handleOwnClient?.invoke(player, description)
                        } else {
                            //callbacks.handleOtherClients?.invoke(heroPlayer, player, description)
                        }
                    }

                    else -> error("Received an unknown Ability?")
                }
            }
        }
    }

    override fun isUsingAbility(player: PlayerEntity, ability: AbstractAbility<*>): Boolean {
        return abilitiesInUse[player.uuid]?.internalKey == ability.internalKey
    }

    override fun registerAbility(ability: AbstractAbility<*>) {
        // REMOVED AbilityKeyBindManager.initializeKeyBind(ability)
    }

    override fun useAbility(
        player: PlayerEntity,
        hero: Hero,
        ability: AbstractAbility<*>,
        description: AbilityPacketDescription.Use
    ): Boolean {
        if (ability.hasCooldown(player)) {
            return false
        } else {
            val packet = AbilityPacket(player.uuid, hero.internalKey, ability.internalKey, description)
            Networking.c2sAbilityPacket.send(packet)
            return true
        }
    }

    override fun useAbility(
        player: PlayerEntity,
        ability: AbstractAbility<*>,
        description: AbilityPacketDescription.Use
    ) {
        val hero = player.getHero() ?: return
        //player.sendDebugMessage("Sending Start Use $ability".literal)
        val packet = AbilityPacket(player.uuid, hero.internalKey, ability.internalKey, description)
        Networking.c2sAbilityPacket.send(packet)
    }

    fun startAbility(player: PlayerEntity, hero: Hero, ability: ToggleAbility): Boolean {
        if (ability.hasCooldown(player)) {
            return false
        } else {
            //player.sendDebugMessage("Sending Start Ability $ability".literal)
            val packet =
                AbilityPacket(player.uuid, hero.internalKey, ability.internalKey, AbilityPacketDescription.Start)
            Networking.c2sAbilityPacket.send(packet)
            return true
        }
    }

    fun endAbility(player: PlayerEntity, hero: Hero, ability: ToggleAbility): Boolean {
        //player.sendDebugMessage("Sending End Ability $ability".literal)
        val packet = AbilityPacket(player.uuid, hero.internalKey, ability.internalKey, AbilityPacketDescription.End)
        Networking.c2sAbilityPacket.send(packet)
        return true
    }

    private fun getAbilityFromAbilityPacket(packet: AbilityPacket<out AbilityPacketDescription>): AbstractAbility<*>? {
        val hero = HeroManager.getHero(packet.heroKey) ?: return null
        val ability = hero.abilities[packet.abilityKey]
        return ability
    }
}
