package gg.norisk.heroes.aang.client.sound

import gg.norisk.heroes.aang.registry.SoundRegistry
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory

class VelocityBasedFlyingSoundInstance(private val entity: Entity, val condition: (Entity) -> Boolean) :
    MovingSoundInstance(SoundRegistry.FLYING, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.3f
    }

    override fun tick() {
        if (!entity.isRemoved && condition.invoke(entity)) {
            this.x = entity.x.toFloat().toDouble()
            this.y = entity.y.toFloat().toDouble()
            this.z = entity.z.toFloat().toDouble()
            val f: Float = Math.min(0.3f, Math.max(0.01f, this.entity.velocity.lengthSquared().toFloat()))
            this.volume = f
            this.pitch = 1f + this.volume
        } else {
            this.setDone()
        }
    }
}
