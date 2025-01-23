package gg.norisk.heroes.common.ability

import kotlinx.serialization.Serializable

@Serializable
data class PropertyPlayer(
    var experiencePoints: Int = 0
) {
}
