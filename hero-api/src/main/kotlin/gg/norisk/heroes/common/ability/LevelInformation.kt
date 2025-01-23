package gg.norisk.heroes.common.ability

data class LevelInformation(
    val currentLevel: Int,
    val nextLevel: Int,
    val xpCurrentLevel: Int,
    val xpNextLevel: Int,
    val xpTillNextLevel: Int,
    val percentageTillNextLevel: Double,
    val experiencePoints: Int,
    val maxLevel: Int,
)
