package gg.norisk.heroes.common.cooldown

import kotlinx.serialization.Serializable

@Serializable
data class CooldownInfo(
    val entityId: Int,
    val duration: Long,
    val startTime: Long?,
    val currentTime: Long,
    val multipleUsesInfo: MultipleUsesInfo?,
    val heroKey: String,
    val abilityKey: String,
    val endTime: Long?,
    var durationString: String? = null,
) {
    val hasEnded get() = endTime?.let { System.nanoTime() > it } ?: true
    val remaining get() = endTime?.let { it - System.nanoTime() } ?: 0
}

@Serializable
data class MultipleUsesInfo(
    val currentUse: Int,
    val maxUses: Int
)