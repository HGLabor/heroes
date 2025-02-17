package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.ability.operation.Operation
import kotlinx.serialization.Serializable

@Serializable
open class CooldownProperty(
    override var baseValue: Double,
    override var maxLevel: Int,
    override var name: String,
    override var modifier: Operation,
    override var levelScale: Int = PlayerProperty.levelScale
) : AbstractNumberProperty() {

}