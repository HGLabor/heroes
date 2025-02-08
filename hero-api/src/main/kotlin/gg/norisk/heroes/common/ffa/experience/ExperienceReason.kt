package gg.norisk.heroes.common.ffa.experience

import kotlinx.serialization.Serializable

@Serializable
data class ExperienceReason(val key: String, var value: Int)
