package gg.norisk.heroes.aang.client.sound

import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility.isSpiritualLevitating
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class AirBendingLevitationSoundInstance(private val entity: PlayerEntity) :
    MovingSoundInstance(SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {
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

        if (!entity.isRemoved && entity.isSpiritualLevitating) {
            this.volume = 0.75f
            this.pitch = 0.5f
        } else {
            isFading = true
        }
    }
}
