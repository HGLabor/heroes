package gg.norisk.ffa.server.mechanics

import gg.norisk.heroes.common.events.HeroEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask

object CombatTag {
    interface ICombatPlayer {
        var ffa_combatTicks: Int
    }

    var ticks = 15 * 20

    fun init() {
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, server ->
            mcCoroutineTask(sync = true, client = false) {
                val player = handler.player
                player.kill()
            }
        })
        HeroEvents.heroDeathEvent.listen { event ->
            /*if (event.player.isInCombat()) {
                println("LAST DAMAGE: " + event.player.damageTracker.recentDamage)
                event.isValidDeath = true
                event.player.sendMessage("Du bekommst einen Tod dazugeschrieben weil du in combat warst!".literal)
            } else {
                event.isValidDeath = false
                event.player.sendMessage("Du bekommst keinen Tod dazugeschrieben weil du nicht in combat warst!".literal)
            }*/
        }
    }

    fun getCombatTimeAsString(value: Int): String {
        val builder = StringBuilder()
        value.ticks.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) builder.append(days).append("d ")
            if (hours > 0) builder.append(hours).append("h ")
            if (minutes > 0) builder.append(minutes).append("m ")
            builder.append(seconds).append("s")
        }
        return builder.toString()
    }

    fun PlayerEntity.isInCombat(): Boolean {
        val damageTracker = damageTracker
        val lastAttackTime = ticks - (age - lastAttackTime)
        return damageTracker.hasDamage || lastAttackTime > 0
    }
}