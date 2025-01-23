package gg.norisk.heroes.common.ability

import gg.norisk.heroes.common.db.DatabaseManager
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.ability.AbstractAbility
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.math.sqrt

@Serializable
sealed class PlayerProperty<T> {
    abstract var baseValue: T
    abstract var maxLevel: Int
    abstract var name: String
    abstract var levelScale: Int

    @Transient
    lateinit var hero: Hero<*>

    @Transient
    lateinit var ability: AbstractAbility<*>

    companion object {
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

    fun getLevelInfo(uuid: UUID): LevelInformation {
        val player = getOrLoadPlayer(uuid)
        val currentLevel = Math.min(maxLevel, calculateLevel(player.experiencePoints))
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
            ((player.experiencePoints - xpCurrentLevel).toDouble() / (xpNextLevel - xpCurrentLevel).toDouble()) * 100.0
        } else {
            100.0 // Max-Level erreicht
        }

        if (currentLevel == maxLevel -1 && percentageTillNextLevel >= 100f ) {
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
        return when {
            xp < 352 -> {
                // Level 0-16
                sqrt((xp + 9).toDouble()).toInt() - 3
            }

            xp < 1507 -> {
                // Level 17-31
                ((40.5 + sqrt(1640.25 - 10 * (360 - xp))).toInt() / 5)
            }

            else -> {
                // Level 32 und höher
                ((162.5 + sqrt(26404.25 - 18 * (2220 - xp))).toInt() / 9)
            }
        } / levelScale
    }

    private fun getXpForLevel(level: Int): Int {
        return when (val scaledLevel = level * levelScale) {
            in 0..16 -> scaledLevel * scaledLevel + 6 * scaledLevel
            in 17..31 -> (2.5 * scaledLevel * scaledLevel - 40.5 * scaledLevel + 360).toInt()
            else -> (4.5 * scaledLevel * scaledLevel - 162.5 * scaledLevel + 2220).toInt()
        }
    }

    val internalKey get() = name.lowercase().replace(" ", "_")
    val translationKey get() = "heroes.property.${internalKey}"

    fun getOrLoadPlayer(uuid: UUID): PropertyPlayer {
        val player = DatabaseManager.provider.getCachedPlayer(uuid)
        val heroMap = player.heroes.computeIfAbsent(hero.internalKey) { mutableMapOf() }
        val abilityMap = heroMap.computeIfAbsent(ability.internalKey) { mutableMapOf() }
        val property = abilityMap.computeIfAbsent(internalKey) { PropertyPlayer() }
        return property
    }
}