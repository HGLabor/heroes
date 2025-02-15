package gg.norisk.heroes.aang.client.sound

import gg.norisk.heroes.aang.ability.AirBallAbility.isAirBending
import gg.norisk.heroes.aang.entity.aang
import gg.norisk.heroes.aang.registry.SoundRegistry
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory

class AirBendingCircleSoundInstance(private val entity: PlayerEntity) :
    MovingSoundInstance(SoundRegistry.FLYING, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {
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

        val progress = entity.aang.aang_airBallSpinTracker.getSpinProgress()
        if (!entity.isAirBending) {
            isFading = true
            return
        }

        if (!entity.isRemoved) {
            val percentage = (progress / 100.0) * 0.2
            val f: Float = Math.min(0.5f, Math.max(0.1f, percentage.toFloat()))
            this.volume = f
            this.pitch = 1f + this.volume
        } else {
            isFading = true
        }
    }
}
