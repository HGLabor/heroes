package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.ParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ParticleRegistry {
    val AIR_SCOOTER_DUST = register("air_scooter_dust")
    val BENDING_AIR = register("bending_air")

    fun init() {
    }

    private fun register(
        name: String
    ): ParticleEffect {
        return Registry.register(Registries.PARTICLE_TYPE, name.toId(), FabricParticleTypes.simple())
    }
}
