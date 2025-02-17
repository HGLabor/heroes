package gg.norisk.ffa.server.mechanics

import gg.norisk.ffa.server.event.FFAEvents
import gg.norisk.ffa.server.ext.IDamageTrackerExt
import gg.norisk.ffa.server.mechanics.CombatTag.isInCombat
import gg.norisk.ffa.server.mixin.accessor.LivingEntityAccessor
import gg.norisk.ffa.server.selector.SelectorServerManager.setSelectorReady
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.ffa.experience.ExperienceReason
import gg.norisk.heroes.common.ffa.experience.ExperienceRegistry
import gg.norisk.heroes.common.ffa.experience.addXp
import gg.norisk.heroes.common.player.FFAPlayer
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.server.database.player.PlayerProvider
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.passive.ChickenEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literalText
import java.awt.Color
import kotlin.math.min

object KillManager {
    fun init() {
        killCommand()

        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, source, _ ->
            val player = entity as? ChickenEntity ?: return@register true

            val attacker =
                source.attacker as? ServerPlayerEntity? ?: (player.damageTracker as IDamageTrackerExt).ffa_lastPlayer
            if (attacker != null) {
                FFAEvents.entityKilledOtherEntityEvent.invoke(
                    FFAEvents.EntityKilledOtherEntityEvent(
                        attacker,
                        player,
                        source
                    )
                )
            }

            return@register true
        }

        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, source, _ ->
            val player = entity as? ServerPlayerEntity ?: return@register true
            (player as LivingEntityAccessor).lastAttackTime = -10000
            (player as LivingEntityAccessor).attacking = null
            player.damageTracker.hasDamage = false

            val attacker =
                source.attacker as? ServerPlayerEntity? ?: (player.damageTracker as IDamageTrackerExt).ffa_lastPlayer
            if (attacker != null && attacker != player) {
                FFAEvents.entityKilledOtherEntityEvent.invoke(
                    FFAEvents.EntityKilledOtherEntityEvent(
                        attacker,
                        player,
                        source
                    )
                )
            }

            (player.damageTracker as IDamageTrackerExt).ffa_lastPlayer = null
            player.setSelectorReady()

            return@register false
        }

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            val item = entity as? ItemEntity? ?: return@register
            item.itemAge = 4800
        }

        FFAEvents.entityKilledOtherEntityEvent.listen { event ->
            val wasCombatLog = event.source.isOf(DamageTypes.GENERIC_KILL)
            val killer = event.killer as? ServerPlayerEntity?
            val killed = event.killed as? ServerPlayerEntity?

            killed?.inventory?.dropAll()

            Silk.server?.broadcastText(literalText {
                text(event.killer.name)
                text(" hat ") {
                    color = Color.YELLOW.rgb
                }
                text(event.killed.name)
                text(" getötet") {
                    color = Color.YELLOW.rgb
                }
                if (wasCombatLog) {
                    text(" ")
                    text("[Combat Log]") {
                        color = Color.RED.rgb
                    }
                }
            })

            mcCoroutineTask(false, false) {
                if (killer != null) {
                    increaseKillsForPlayer(killer)
                }
                if (killed != null) {
                    increaseDeathForPlayer(killed, event.source)
                }

                if (killer != killed && killer != null && killed != null) {
                    Bounty.receiveBounty(killer, killed)
                }
            }
        }
    }

    private fun provideExtraXpForKillStreak(player: ServerPlayerEntity, ffaPlayer: FFAPlayer) {
        val currentKillStreak = ffaPlayer.currentKillStreak
        val killStreakXp = min(3000, ExperienceRegistry.KILLED_PLAYER.value * currentKillStreak * 10)
        player.addXp(ExperienceReason("kill_streak", killStreakXp))
    }

    private fun provideExtraBountyForKillStreak(player: ServerPlayerEntity, ffaPlayer: FFAPlayer) {
        val currentKillStreak = ffaPlayer.currentKillStreak
        val bountyXp = when (currentKillStreak) {
            10 -> 1000
            20 -> 2000
            else -> return
        }

        ffaPlayer.bounty += bountyXp
    }

    private suspend fun increaseKillsForPlayer(attacker: ServerPlayerEntity) {
        val cachedAttacker = PlayerProvider.get(attacker.uuid)
        cachedAttacker.kills++
        cachedAttacker.currentKillStreak++
        attacker.ffaPlayer = cachedAttacker
        if (cachedAttacker.currentKillStreak.mod(10) == 0 || cachedAttacker.currentKillStreak == 5) {
            attacker.server.broadcastText {
                text(attacker.name)
                text(" hat eine Killstreak von ") {
                    color = Color.YELLOW.rgb
                }
                text(cachedAttacker.currentKillStreak.toString()) {
                    color = Color.RED.rgb
                }
            }
            provideExtraXpForKillStreak(attacker, cachedAttacker)
            provideExtraBountyForKillStreak(attacker, cachedAttacker)
        }
        PlayerProvider.save(cachedAttacker)
        attacker.addXp(ExperienceRegistry.KILLED_PLAYER, true)
    }

    private suspend fun increaseDeathForPlayer(player: ServerPlayerEntity, source: DamageSource) {
        val heroDeathEvent = HeroEvents.HeroDeathEvent(player, true)
        player.sendMessage(Text.translatable("ffa.died"))
        HeroEvents.heroDeathEvent.invoke(heroDeathEvent)
        val cachedEntity = PlayerProvider.get(player.uuid)
        if (heroDeathEvent.isValidDeath) {
            cachedEntity.deaths++
            if (cachedEntity.currentKillStreak > cachedEntity.highestKillStreak) {
                cachedEntity.highestKillStreak = cachedEntity.currentKillStreak
            }
            if (cachedEntity.currentKillStreak >= 5) {
                player.sendMessage(Text.translatable("ffa.mechanic.killstreak.lost", player.name, cachedEntity.currentKillStreak))
            }
            cachedEntity.currentKillStreak = 0

            player.ffaPlayer = cachedEntity
            mcCoroutineTask(sync = false, client = false) {
                PlayerProvider.save(cachedEntity)
            }
        }

        if (!source.isOf(DamageTypes.GENERIC_KILL)) {
            player.addXp(ExperienceRegistry.PLAYER_DEATH, true)
        }
    }

    private fun killCommand() {
        command("kill") {
            alias("spawn")
            requiresPermissionLevel(PermissionLevel.NONE)
            runs {
                val player = this.source.playerOrThrow
                if (player.isInCombat()) {
                    player.kill()
                } else {
                    player.setSelectorReady()
                }
            }
        }
    }
}
