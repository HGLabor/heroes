package gg.norisk.heroes.common.ability.operation

import kotlinx.serialization.Serializable

@Serializable
sealed class Operation {
    abstract fun getOperatedValue(baseValue: Double, level: Int): Double
}