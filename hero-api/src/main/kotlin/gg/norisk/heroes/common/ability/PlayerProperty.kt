package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import gg.norisk.heroes.server.database.player.PlayerProvider
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.minecraft.item.Items
import java.util.*
import kotlin.math.cbrt
import kotlin.math.pow

@Serializable
sealed class PlayerProperty<T> {
    abstract var baseValue: T
    abstract var maxLevel: Int
    abstract var name: String
    abstract var levelScale: Int

    @Transient
    var icon: () -> Component = {
        Components.item(Items.CLOCK.defaultStack)
    }

    @Transient
    lateinit var hero: Hero

    @Transient
    lateinit var ability: AbstractAbility<*>

    companion object {
        val levelScale = 315

        val JSON = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            //explicitNulls = true
        }
    }

    abstract fun getValue(uuid: UUID): T
    abstract fun getValue(int: Int): T
    abstract fun fromJson(text: String)
    abstract fun toJson(): String
    abstract fun toJsonElement(): JsonElement
    open fun getMaxValue(): T {
        return getValue(maxLevel)
    }

    fun addExperience(uuid: UUID, experienceToAdd: Int): Int {
        val player = getOrLoadPlayer(uuid)
        val maxExperience = getXpForLevel(maxLevel)

        val buffer = player.experiencePoints + experienceToAdd
        return if (buffer > maxExperience) {
            val toAdd = maxExperience - player.experiencePoints // Nur bis zum Maximalwert hinzufügen
            player.experiencePoints = maxExperience // Korrigiere Erfahrungspunkte auf Maximum
            toAdd
        } else {
            player.experiencePoints += experienceToAdd
            experienceToAdd
        }
    }

    fun isMaxed(uuid: UUID): Boolean {
        return getLevelInfo(uuid).currentLevel >= maxLevel
    }

    fun getLevelInfo(uuid: UUID, level: Int? = null): LevelInformation {
        val player = getOrLoadPlayer(uuid)
        val currentLevel = Math.min(maxLevel, level ?: calculateLevel(player.experiencePoints))
        val nextLevel = Math.min(maxLevel, currentLevel + 1)

        val xpCurrentLevel = getXpForLevel(currentLevel)

        val xpNextLevel = if (currentLevel < maxLevel) {
            getXpForLevel(nextLevel)
        } else {
            xpCurrentLevel // Kein weiteres Level, bleibt gleich
        }

        val xpTillNextLevel = if (currentLevel < maxLevel) {
            xpNextLevel - player.experiencePoints
        } else {
            0 // Kein weiteres Level
        }

        val percentageTillNextLevel = if (currentLevel < maxLevel) {
            Math.max(
                0.0, Math.min(
                    100.0,
                    ((player.experiencePoints - xpCurrentLevel).toDouble() / (xpNextLevel - xpCurrentLevel).toDouble()) * 100.0
                )
            )
        } else {
            100.0 // Max-Level erreicht
        }

        if (currentLevel == maxLevel - 1 && percentageTillNextLevel >= 100f) {
            return LevelInformation(
                maxLevel,
                maxLevel,
                xpCurrentLevel,
                xpNextLevel,
                xpTillNextLevel,
                percentageTillNextLevel,
                player.experiencePoints,
                maxLevel
            )
        }

        return LevelInformation(
            currentLevel,
            nextLevel,
            xpCurrentLevel,
            xpNextLevel,
            xpTillNextLevel,
            percentageTillNextLevel,
            player.experiencePoints,
            maxLevel
        )
    }

    private fun calculateLevel(xp: Int): Int {
        return cbrt((xp / levelScale).toDouble()).toInt()
    }

    private fun getXpForLevel(level: Int): Int {
        return (levelScale * level.toDouble().pow(3)).toInt()
    }

    val internalKey get() = name.lowercase().replace(" ", "_")
    val translationKey get() = "heroes.property.${internalKey}"
    val descriptionKey get() = "heroes.property.${internalKey}.description"

    private fun getOrLoadPlayer(uuid: UUID): PropertyPlayer {
        val player = runBlocking { PlayerProvider.get(uuid) }
        val heroMap = player.heroes.computeIfAbsent(hero.internalKey) { mutableMapOf() }
        val abilityMap = heroMap.computeIfAbsent(ability.internalKey) { mutableMapOf() }
        val property = abilityMap.computeIfAbsent(internalKey) { PropertyPlayer() }
        return property
    }
}
