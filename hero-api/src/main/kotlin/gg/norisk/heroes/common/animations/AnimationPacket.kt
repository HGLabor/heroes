package gg.norisk.heroes.common.animations

import gg.norisk.heroes.common.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier
import net.silkmc.silk.core.serialization.serializers.ResourceLocationSerializer
import java.util.*

@Serializable
data class AnimationPacket(
    @Serializable(with = UUIDSerializer::class)
    val playerUuid: UUID,
    @Serializable(with = ResourceLocationSerializer::class)
    val animation: Identifier
)