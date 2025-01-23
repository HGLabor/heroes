package gg.norisk.heroes.common.hero.ability

import gg.norisk.heroes.common.hero.Hero
import net.minecraft.entity.player.PlayerEntity

interface IAbilityManager {
    fun init()

    fun useAbility(
        player: PlayerEntity,
        hero: Hero<*>,
        ability: AbstractAbility<*>,
        description: AbilityPacketDescription.Use
    ): Boolean

    fun useAbility(player: PlayerEntity, ability: AbstractAbility<*>, description: AbilityPacketDescription.Use)

    fun registerAbility(ability: AbstractAbility<*>)

    fun isUsingAbility(player: PlayerEntity, ability: AbstractAbility<*>): Boolean
}
