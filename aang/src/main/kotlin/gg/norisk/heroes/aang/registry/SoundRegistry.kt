package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var FLYING = "flying".register()

    fun init() {
    }

    private fun String.register() = Registry.register(Registries.SOUND_EVENT, this.toId(), SoundEvent.of(this.toId()))
}
