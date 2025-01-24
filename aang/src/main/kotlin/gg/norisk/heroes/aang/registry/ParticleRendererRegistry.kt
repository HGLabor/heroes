package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.client.particle.AirScooterDustParticle
import gg.norisk.heroes.aang.client.particle.BendingAirParticle
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType

object ParticleRendererRegistry {
    fun init() {
        ParticleFactoryRegistry.getInstance().register(ParticleRegistry.AIR_SCOOTER_DUST as ParticleType<ParticleEffect>, AirScooterDustParticle::Factory)
        ParticleFactoryRegistry.getInstance().register(ParticleRegistry.BENDING_AIR as ParticleType<ParticleEffect>, BendingAirParticle::Factory)
    }
}
