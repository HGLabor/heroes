package gg.norisk.heroes.common.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Box
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

val LivingEntity.pos3i: Vec3i get() = Vec3i(x.toInt(), y.toInt(), z.toInt())

fun Entity.sound(soundEvent: SoundEvent, volume: Number = 1f, pitch: Number = 1f) {
    world.playSoundFromEntity(null, this, soundEvent, this.soundCategory, volume.toFloat(), pitch.toFloat())
}


fun PlayerEntity.sound(soundEvent: SoundEvent) {
    world.playSoundFromEntity(null, this, soundEvent, SoundCategory.PLAYERS, 1f, 1f)
}
