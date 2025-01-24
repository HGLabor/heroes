package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import net.minecraft.util.Identifier

object EmoteRegistry {
    val AIR_SCOOTER = "air_scooter_2".toEmote()
    val AIR_SCOOTER_SITTING = "air_scooter_sitting".toEmote()
    val SPIRITUAL_PROJECTION_START = "spiritual_projection_start".toEmote()
    val SPIRITUAL_PROJECTION_LOOP = "spiritual_projection_loop".toEmote()
    val LEVITATION = "levitation".toEmote()
    val AIR_BENDING = "air_bending".toEmote()

    fun init() {}

    fun String.toEmote(): Identifier {
        return "emotes/$this.animation.json".toId()
    }
}
