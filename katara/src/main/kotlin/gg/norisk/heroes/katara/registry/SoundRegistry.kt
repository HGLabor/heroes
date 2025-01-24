package gg.norisk.heroes.katara.registry

import gg.norisk.heroes.katara.KataraManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var ICE_PLACE = "ice_place".register()
    var WATER_CIRCLE_ADD = "water_circle_add".register()

    fun init() {
    }

    private fun String.register() = Registry.register(Registries.SOUND_EVENT, this.toId(), SoundEvent.of(this.toId()))
}
