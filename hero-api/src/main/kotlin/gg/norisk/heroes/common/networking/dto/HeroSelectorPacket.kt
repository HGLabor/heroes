package gg.norisk.heroes.common.networking.dto

import kotlinx.serialization.Serializable

@Serializable
data class HeroSelectorPacket(
    val heroes: List<String>,
    val isActive: Boolean,
    var isKitEditorEnabled: Boolean
)