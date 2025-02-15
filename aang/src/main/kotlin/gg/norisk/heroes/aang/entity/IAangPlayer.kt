package gg.norisk.heroes.aang.entity

import gg.norisk.heroes.aang.utils.EntitySpinTracker
import gg.norisk.heroes.aang.utils.PlayerRotationTracker
import kotlinx.coroutines.Job
import net.minecraft.entity.player.PlayerEntity

interface IAangPlayer {
    var rotationTracker: PlayerRotationTracker?
    val aang_airScooterTasks: MutableList<Job>
    val aang_spiritualProjectionsTasks: MutableList<Job>
    val aang_tornadoTasks: MutableList<Job>
    var aang_tornadoEntity: TornadoEntity?
    var aang_airBallSpinTracker: EntitySpinTracker
}

val PlayerEntity.aang get() = this as IAangPlayer
