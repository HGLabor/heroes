package gg.norisk.heroes.common.player

import dev.kosmx.playerAnim.api.layered.IAnimation
import dev.kosmx.playerAnim.api.layered.ModifierLayer

interface IAnimatedPlayer {
    fun hero_getModAnimation(): ModifierLayer<IAnimation>
}
