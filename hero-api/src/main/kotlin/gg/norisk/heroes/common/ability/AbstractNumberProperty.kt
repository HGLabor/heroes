package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.ability.operation.MultiplyBase
import gg.norisk.heroes.common.ability.operation.Operation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*

@Serializable
sealed class AbstractNumberProperty : PlayerProperty<Double>() {
    abstract var modifier: Operation

    override fun getValue(uuid: UUID): Double {
        return getValue(getLevelInfo(uuid).currentLevel)
    }

    override fun getValue(int: Int): Double {
        return when (modifier) {
            is AddValueTotal, is MultiplyBase -> {
                modifier.getOperatedValue(baseValue, int)
            }
        }
    }

    override fun fromJson(text: String) {
        runCatching {
            val loaded = JSON.decodeFromString<NumberProperty>(text)
            baseValue = loaded.baseValue
            maxLevel = loaded.maxLevel
            name = loaded.name
            levelScale = loaded.levelScale
            modifier = loaded.modifier
        }.onFailure {
            HeroesManager.logger.error("Error Loading $name ${it.message}")
            it.printStackTrace()
        }
    }

    override fun toJson(): String {
        return JSON.encodeToString<PlayerProperty<Double>>(this)
    }

    override fun toJsonElement(): JsonElement {
        return JSON.encodeToJsonElement<PlayerProperty<Double>>(this)
    }
}