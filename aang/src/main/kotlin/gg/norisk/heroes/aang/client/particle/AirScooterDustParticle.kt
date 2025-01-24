package gg.norisk.heroes.aang.client.particle

import gg.norisk.utils.Easing
import gg.norisk.utils.OldAnimation
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.ParticleEffect
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Environment(EnvType.CLIENT)
class AirScooterDustParticle internal constructor(
    clientWorld: ClientWorld,
    d: Double,
    e: Double,
    f: Double,
    g: Double,
    h: Double,
    i: Double,
    bl: Boolean
) : SpriteBillboardParticle(clientWorld, d, e, f) {
    val scaleAnimation = OldAnimation(0f, 3.5f, 1.seconds.toJavaDuration(), Easing.LINEAR)

    init {
        this.setBoundingBoxSpacing(0.25f, 0.25f)
        this.maxAge = random.nextInt(50)
        this.gravityStrength = 3.0E-6f
        this.velocityX = g
        this.velocityY = h
        this.velocityZ = i
    }

    override fun tick() {
        this.prevPosX = this.x
        this.prevPosY = this.y
        this.prevPosZ = this.z
        this.scale = scaleAnimation.get()
        if (age++ < this.maxAge && !(this.alpha <= 0.0f)) {
            this.velocityX += (random.nextFloat() / 5000.0f * (if (random.nextBoolean()) 1 else -1).toFloat()).toDouble()
            this.velocityZ += (random.nextFloat() / 5000.0f * (if (random.nextBoolean()) 1 else -1).toFloat()).toDouble()
            this.velocityY -= gravityStrength.toDouble()
            this.move(this.velocityX, this.velocityY, this.velocityZ)
            if (this.age >= this.maxAge - 60 && this.alpha > 0.01f) {
                this.alpha -= 0.015f
            }
        } else {
            this.markDead()
        }
    }

    override fun getType(): ParticleTextureSheet {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
    }

    @Environment(EnvType.CLIENT)
    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<ParticleEffect> {
        override fun createParticle(
            defaultParticleType: ParticleEffect,
            clientWorld: ClientWorld,
            d: Double,
            e: Double,
            f: Double,
            g: Double,
            h: Double,
            i: Double
        ): Particle {
            val airScooterParticle = AirScooterDustParticle(clientWorld, d, e, f, g, h, i, false)
            airScooterParticle.setAlpha(0.5f)
            airScooterParticle.setSprite(this.spriteProvider)
            return airScooterParticle
        }
    }
}
