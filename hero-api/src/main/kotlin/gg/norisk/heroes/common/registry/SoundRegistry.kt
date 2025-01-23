package gg.norisk.heroes.common.registry

import gg.norisk.heroes.common.HeroesManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var FLYING = "flying".register()

    fun init() {
    }

    private fun String.register() = Registry.register(Registries.SOUND_EVENT, this.toId(), SoundEvent.of(this.toId()))
}
