package gg.norisk.heroes.common.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.silkmc.silk.core.entity.directionVector
import kotlin.math.cos
import kotlin.math.sin

fun LivingEntity.raycastEntities(range: Int = 30, expand: Double = 1.0): Set<Entity> {
    val eyePos = eyePos
    val eyeDirection = directionVector.normalize()

    val entities = mutableSetOf<Entity>()

    repeat(range) {
        val newPos = eyePos.add(eyeDirection.multiply(it.toDouble()))
        entities += world.getOtherEntities(this, Box.from(newPos).expand(expand))
    }

    return entities
}

fun Entity.oldTeleport(
    world: ServerWorld,
    x: Double,
    y: Double,
    z: Double,
    movementFlags: Set<PositionFlag>,
    yaw: Float,
    pitch: Float,
    resetCamera: Boolean = true
) {
    val d = if (movementFlags.contains(PositionFlag.X)) x - this.x else x
    val e = if (movementFlags.contains(PositionFlag.Y)) y - this.y else y
    val f = if (movementFlags.contains(PositionFlag.Z)) z - this.z else z
    val g = if (movementFlags.contains(PositionFlag.Y_ROT)) yaw - this.yaw else yaw
    val h = if (movementFlags.contains(PositionFlag.X_ROT)) pitch - this.pitch else pitch
    val i = MathHelper.wrapDegrees(g)
    val j = MathHelper.wrapDegrees(h)
    if (this.teleport(world, d, e, f, movementFlags, i, j, resetCamera)) {
    }
}

val LivingEntity.pos3i: Vec3i get() = Vec3i(x.toInt(), y.toInt(), z.toInt())

fun Entity.sound(soundEvent: SoundEvent, volume: Number = 1f, pitch: Number = 1f) {
    world.playSoundFromEntity(null, this, soundEvent, this.soundCategory, volume.toFloat(), pitch.toFloat())
}


fun PlayerEntity.sound(soundEvent: SoundEvent) {
    world.playSoundFromEntity(null, this, soundEvent, SoundCategory.PLAYERS, 1f, 1f)
}
