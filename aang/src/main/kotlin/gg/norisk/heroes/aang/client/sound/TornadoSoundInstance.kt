package gg.norisk.heroes.aang.client.sound

import gg.norisk.heroes.aang.entity.TornadoEntity
import gg.norisk.heroes.aang.registry.SoundRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory

class TornadoSoundInstance(private val entity: TornadoEntity) :
    MovingSoundInstance(SoundRegistry.FLYING, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.3f
    }

    override fun tick() {
        if (!entity.isRemoved) {

            val clientPlayer = MinecraftClient.getInstance().player ?: return
            if (entity.controllingPassenger?.id == clientPlayer.id) {
                this.x = clientPlayer.x.toFloat().toDouble() + 20
                this.y = clientPlayer.y.toFloat().toDouble() + 19
                this.z = clientPlayer.z.toFloat().toDouble()
            } else {
                this.x = entity.x.toFloat().toDouble()
                this.y = entity.y.toFloat().toDouble()
                this.z = entity.z.toFloat().toDouble()
            }


            val f: Float = this.entity.scale / 5f
            this.volume = f
            this.pitch = 1f + this.volume
        } else {
            this.setDone()
        }
    }
}
