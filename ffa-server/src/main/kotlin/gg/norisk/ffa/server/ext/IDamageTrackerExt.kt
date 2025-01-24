package gg.norisk.ffa.server.ext

import net.minecraft.entity.player.PlayerEntity

interface IDamageTrackerExt {
    var ffa_lastPlayer: PlayerEntity?
}