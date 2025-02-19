package gg.norisk.heroes.common.localization.minimessage

import kotlin.reflect.full.declaredMemberProperties

object LaborColors {
    val White = 0xFFFFFF
    val Gray = 0xAAAAAA
    val DarkGray = 0x555555
    val Pink = 0xEF6F82
    val LightPink = 0xFFC0CB
    val DarkPink = 0x966f76
    val DarkPurple = 0x8C5ABB
    val Purple = 0xD9C4EC
    val Green = 0xBFFFB7
    val DarkGreen = 0x0E7C00
    val Red = 0xFF9997
    val DarkRed = 0xFF4C49
    val Blue = 0x3399ff
    val LightBlue = 0xA6EDFF
    val Yellow = 0xF9E795
    val Orange = 0xFFB797
    val CornSilk = 0xFFF8DC
    val DarkerCornSilk = 0x858276

    fun getAllColorsWithValue(): Map<String, Int> {
        return this::class.declaredMemberProperties.associate {
            val name = it.name.lowercase()
            val value = it.getter.call(this) as Int
            name to value
        }
    }
}
