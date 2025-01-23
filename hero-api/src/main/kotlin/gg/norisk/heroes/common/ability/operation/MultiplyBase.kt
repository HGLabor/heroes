package gg.norisk.heroes.common.ability.operation

import kotlinx.serialization.Serializable

@Serializable
class MultiplyBase(var steps: List<Double>) : Operation() {
    constructor(vararg steps: Double) : this(steps.toList())
    override fun getOperatedValue(baseValue: Double, level: Int): Double {
        //wir doublen alles wegen... jo
        val increment = steps.getOrNull(level) ?: error("$steps doesn't have an index for level $level")

        return baseValue * increment
    }
}