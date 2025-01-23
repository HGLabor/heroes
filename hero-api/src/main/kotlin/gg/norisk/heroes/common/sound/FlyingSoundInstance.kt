package gg.norisk.heroes.common.sound

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

class FlyingSoundInstance(
    private val player: ClientPlayerEntity,
    private val condition: Supplier<Boolean>
) :
    MovingSoundInstance(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.1f
    }

    override fun tick() {
        ++this.tickCount
        this.x = player.x.toFloat().toDouble()
        this.y = player.y.toFloat().toDouble()
        this.z = player.z.toFloat().toDouble()
        val f = player.velocity.lengthSquared().toFloat() * 2
        if (f.toDouble() >= 1.0E-7) {
            this.volume = MathHelper.clamp(f / 4.0f, 0.0f, 1.0f)
        } else {
            this.volume = 0.0f
        }

        if (!player.isRemoved && (condition.get())) {
            if (this.volume > 0.8f) {
                this.pitch = 1.0f + (this.volume - 0.8f)
            } else {
                this.pitch = 1.0f
            }
        } else {
            if (player.isOnGround) {
                this.setDone()
            }
        }
    }
}
