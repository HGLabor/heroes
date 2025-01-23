package gg.norisk.heroes.common.ability.operation

import kotlinx.serialization.Serializable

@Serializable
class AddValueTotal(var steps: List<Double>) : Operation() {

    constructor(vararg steps: Double) : this(steps.toList())

    override fun getOperatedValue(baseValue: Double, level: Int): Double {
        // wir doublen alles wegen... jo
        var valueToReturn = baseValue
        repeat(level) {
            val increment = steps.getOrNull(it) ?: error("$steps doesn't have an index for level $it")
            valueToReturn += increment
        }

        return valueToReturn
    }
}
