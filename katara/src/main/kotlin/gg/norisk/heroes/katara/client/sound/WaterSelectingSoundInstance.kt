package gg.norisk.heroes.katara.client.sound

import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class WaterSelectingSoundInstance(private val entity: Entity, val condition: (Entity) -> Boolean) :
    MovingSoundInstance(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {
    var fadeTime = 20
    var isFading = false

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.01f
    }

    override fun tick() {
        if (isFading) {
            --fadeTime
            this.volume *= 0.9f
            if (fadeTime < 0) {
                this.setDone()
                return
            }
        }

        this.x = entity.x.toFloat().toDouble()
        this.y = entity.y.toFloat().toDouble()
        this.z = entity.z.toFloat().toDouble()

        if (!entity.isRemoved && condition.invoke(entity)) {
            this.volume = 0.7f
            this.pitch = 0.5f
        } else {
            isFading = true
        }
    }
}