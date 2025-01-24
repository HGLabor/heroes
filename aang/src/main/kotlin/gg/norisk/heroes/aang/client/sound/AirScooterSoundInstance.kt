package gg.norisk.heroes.aang.client.sound

import gg.norisk.heroes.aang.ability.AirScooterAbility.isAirScooting
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.registry.SoundRegistry
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory

class AirScooterSoundInstance(private val entity: Entity) :
    MovingSoundInstance(SoundRegistry.FLYING, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.3f
    }

    override fun tick() {
        val flag = when {
            entity is AirScooterEntity -> true
            entity is PlayerEntity && entity.isAirScooting -> true
            else -> false
        }
        if (!entity.isRemoved && flag) {
            this.x = entity.x.toFloat().toDouble()
            this.y = entity.y.toFloat().toDouble()
            this.z = entity.z.toFloat().toDouble()

            val f: Float = Math.min(0.3f, Math.max(0.1f, this.entity.velocity.lengthSquared().toFloat()))
            this.volume = f
            this.pitch = 1f + this.volume
        } else {
            this.setDone()
        }
    }
}
