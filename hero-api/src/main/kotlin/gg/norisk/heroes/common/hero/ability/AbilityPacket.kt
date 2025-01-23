package gg.norisk.heroes.common.hero.ability

import gg.norisk.heroes.common.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AbilityPacket<C : AbilityPacketDescription>(
    @Serializable(with = UUIDSerializer::class)
    val playerUuid: UUID,
    val heroKey: String,
    val abilityKey: String,
    val description: C
)

@Serializable
data class SkillPropertyPacket(
    val heroKey: String,
    val abilityKey: String,
    val propertyKey: String
)

@Serializable
sealed class AbilityPacketDescription {
    @Serializable
    object Start : AbilityPacketDescription()

    @Serializable
    open class Use : AbilityPacketDescription()

    @Serializable
    object End : AbilityPacketDescription()
}
