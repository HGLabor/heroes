package gg.norisk.heroes.common.animations

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

interface IAnimationManager {
    fun init()

    fun playAnimation(player: PlayerEntity, animation: Identifier)
    fun resetAnimation(player: PlayerEntity)
}
