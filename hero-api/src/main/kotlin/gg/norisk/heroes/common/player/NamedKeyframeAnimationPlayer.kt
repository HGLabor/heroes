package gg.norisk.heroes.common.player

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer
import dev.kosmx.playerAnim.core.data.KeyframeAnimation

class NamedKeyframeAnimationPlayer(
    val name: String,
    animation: KeyframeAnimation,
    t: Int = 0,
    mutable: Boolean = false
) : KeyframeAnimationPlayer(animation, t, mutable)