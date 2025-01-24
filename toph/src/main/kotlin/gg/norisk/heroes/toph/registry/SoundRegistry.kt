package gg.norisk.heroes.toph.registry

import gg.norisk.heroes.toph.TophManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var EARTH_ARMOR = "earth_armor".register()
    var SEISMIC_SENSE_START = "seismic_sense_start".register()
    var SEISMIC_SENSE_WAVE = "seismic_sense_wave".register()
    var EARTH_COLUMN_1 = "earth_column_1".register()
    var ARM_WHOOSH = "arm_whoosh".register()
    var STONE_SMASH = "stone_smash".register()
    var STONE_SLIDE = "stone_slide".register()

    fun init() {
    }

    private fun String.register() = Registry.register(Registries.SOUND_EVENT, this.toId(), SoundEvent.of(this.toId()))
}
