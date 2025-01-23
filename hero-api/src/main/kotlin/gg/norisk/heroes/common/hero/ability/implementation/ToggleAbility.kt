package gg.norisk.heroes.common.hero.ability.implementation

import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.PlayerProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.ability.operation.Operation
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import net.minecraft.entity.player.PlayerEntity

open class ToggleAbility(name: String) : AbstractAbility<Any>(name) {
    override fun onStart(player: PlayerEntity) {

    }

    open fun onUse(player: PlayerEntity) {

    }

    open fun onEnd(player: PlayerEntity) {

    }

    var maxDurationProperty = buildMaxDuration(10.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

    protected fun buildMaxDuration(baseValue: Double, maxLevel: Int, operation: Operation): CooldownProperty {
        return CooldownProperty(
            baseValue, maxLevel,
            "Max Duration",
            operation
        )
    }

    override val extraProperties: List<PlayerProperty<*>>
        get() = listOf(maxDurationProperty)
}