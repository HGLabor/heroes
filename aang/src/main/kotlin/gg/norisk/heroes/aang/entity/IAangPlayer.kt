package gg.norisk.heroes.aang.entity

import gg.norisk.heroes.aang.utils.CircleDetector3D
import gg.norisk.heroes.aang.utils.PlayerRotationTracker
import net.minecraft.entity.player.PlayerEntity

interface IAangPlayer {
    var circleDetector: CircleDetector3D?
    var rotationTracker: PlayerRotationTracker?
}

val PlayerEntity.aang get() = this as IAangPlayer
