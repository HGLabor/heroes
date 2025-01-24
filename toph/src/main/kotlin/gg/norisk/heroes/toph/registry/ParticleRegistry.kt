package gg.norisk.heroes.toph.registry

import gg.norisk.heroes.toph.TophManager.toId
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.ParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ParticleRegistry {
    val EARTH_DUST = register("earth_dust")

    fun init() {
    }

    private fun register(
        name: String
    ): ParticleEffect {
        return Registry.register(Registries.PARTICLE_TYPE, name.toId(), FabricParticleTypes.simple())
    }
}
