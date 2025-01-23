package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.ability.operation.Operation
import kotlinx.serialization.Serializable

@Serializable
class CooldownProperty(
    override var baseValue: Double,
    override var maxLevel: Int,
    override var name: String,
    override var modifier: Operation,
    override var levelScale: Int = 10
) : AbstractNumberProperty() {

}