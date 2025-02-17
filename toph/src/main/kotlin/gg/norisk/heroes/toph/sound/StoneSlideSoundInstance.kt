package gg.norisk.heroes.toph.sound

import gg.norisk.heroes.toph.ability.isEarthSurfing
import gg.norisk.heroes.toph.registry.SoundRegistry
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory

class StoneSlideSoundInstance(private val player: PlayerEntity) :
    MovingSoundInstance(SoundRegistry.STONE_SLIDE, SoundCategory.PLAYERS, SoundInstance.createRandom()) {

    init {
        repeat = true
        repeatDelay = 0
        volume = 1f
    }

    override fun tick() {
        if (!player.isAlive || player.isRemoved) {
            setDone()
        }


        x = player.x.toFloat().toDouble()
        y = player.y.toFloat().toDouble()
        z = player.z.toFloat().toDouble()
        if (player.isEarthSurfing()) {
            volume = 0.7f
        } else {
            volume -= 0.1f
        }

        if (volume <= 0) {
            setDone()
        }
    }

    override fun shouldAlwaysPlay(): Boolean {
        return true
    }
}
