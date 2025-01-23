package gg.norisk.heroes.common.hero.ability

import net.minecraft.entity.player.PlayerEntity

class AbilityScope(val executingPlayer: PlayerEntity) {
    var applyCooldown = true
    var broadcastPacket = false

    fun cancelCooldown() {
        applyCooldown = false
    }

    fun applyCooldown() {
        applyCooldown = true
    }

    fun cancelBroadcasting() {
        broadcastPacket = false
    }

    fun broadcast() {
        broadcastPacket = true
    }
}
