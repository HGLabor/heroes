package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.ability.operation.Operation
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
class MultiUseProperty(
    override var baseValue: Double,
    override var maxLevel: Int,
    override var name: String,
    override var modifier: Operation,
    override var levelScale: Int = 10
) : AbstractUsageProperty() {
    @Transient
    val uses = mutableMapOf<UUID, Int>()
}