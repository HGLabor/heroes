package gg.norisk.ffa.server.mechanics

import gg.norisk.ffa.server.event.FFAEvents
import gg.norisk.ffa.server.ext.IDamageTrackerExt
import gg.norisk.ffa.server.mechanics.CombatTag.isInCombat
import gg.norisk.ffa.server.mixin.accessor.LivingEntityAccessor
import gg.norisk.ffa.server.selector.SelectorServerManager.setSelectorReady
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.common.db.DatabaseManager.provider
import gg.norisk.heroes.common.db.DatabasePlayer
import gg.norisk.heroes.common.db.ExperienceManager
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.hero.getHero
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.awt.Color

object KillManager {
    fun init() {
        killCommand()

        ServerLivingEntityEvents.ALLOW_DEATH.register { entity, source, _ ->
            val player = entity as? ServerPlayerEntity ?: return@register true
            (player as LivingEntityAccessor).lastAttackTime = -10000
            (player as LivingEntityAccessor).attacking = null
            player.damageTracker.hasDamage = false

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

            (player.damageTracker as IDamageTrackerExt).ffa_lastPlayer = null
            player.setSelectorReady()

            return@register false
        }

        FFAEvents.entityKilledOtherEntityEvent.listen { event ->
            val wasCombatLog = event.source.isOf(DamageTypes.GENERIC_KILL)
            val killer = event.killer as? ServerPlayerEntity?
            val killed = event.killed as? ServerPlayerEntity?

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

    private fun provideExtraXpForKillStreak(player: ServerPlayerEntity, dbPlayer: DatabasePlayer) {
        val currentKillStreak = dbPlayer.currentKillStreak
        val killStreakXp = Math.min(3000, ExperienceManager.KILLED_PLAYER.value * currentKillStreak * 10)
        ExperienceManager.addXp(player, ExperienceManager.Reason("kill_streak", killStreakXp))
    }

    private fun provideExtraBountyForKillStreak(player: ServerPlayerEntity, dbPlayer: DatabasePlayer) {
        val currentKillStreak = dbPlayer.currentKillStreak
        val bountyXp = when (currentKillStreak) {
            10 -> 1000
            20 -> 2000
            else -> return
        }

        dbPlayer.bounty += bountyXp
    }

    private fun increaseKillsForPlayer(attacker: ServerPlayerEntity) {
        if (attacker.getHero() != null) {
            val cachedAttacker = provider.getCachedPlayer(attacker.uuid)
            cachedAttacker.kills++
            cachedAttacker.currentKillStreak++
            attacker.dbPlayer = cachedAttacker
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
            mcCoroutineTask(sync = false, client = false) {
                provider.save(cachedAttacker)
            }

            ExperienceManager.addXp(attacker, ExperienceManager.KILLED_PLAYER, true)
        }
    }

    private fun increaseDeathForPlayer(player: ServerPlayerEntity, source: DamageSource) {
        if (player.getHero() != null) {
            val heroDeathEvent = HeroEvents.HeroDeathEvent(player, true)
            player.sendMessage(literalText {
                text("Du bist gestorben".literal)
            })
            HeroEvents.heroDeathEvent.invoke(heroDeathEvent)
            val cachedEntity = provider.getCachedPlayer(player.uuid)
            if (heroDeathEvent.isValidDeath) {
                cachedEntity.deaths++
                if (cachedEntity.currentKillStreak > cachedEntity.highestKillStreak) {
                    cachedEntity.highestKillStreak = cachedEntity.currentKillStreak
                }
                if (cachedEntity.currentKillStreak >= 5) {
                    player.sendMessage(literalText {
                        text(player.name)
                        text("hat seine Killstreak von ") {
                            color = Color.YELLOW.rgb
                        }
                        text(cachedEntity.currentKillStreak.toString()) {
                            color = Color.RED.rgb
                        }
                        text(" verloren") {
                            color = Color.YELLOW.rgb
                        }
                    })
                }
                cachedEntity.currentKillStreak = 0

                player.dbPlayer = cachedEntity
                mcCoroutineTask(sync = false, client = false) {
                    provider.save(cachedEntity)
                }
            }

            if (!source.isOf(DamageTypes.GENERIC_KILL)) {
                ExperienceManager.addXp(player, ExperienceManager.PLAYER_DEATH, true)
            }
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