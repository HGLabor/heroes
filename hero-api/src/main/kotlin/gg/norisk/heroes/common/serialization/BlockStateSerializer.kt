package gg.norisk.heroes.common.serialization

import gg.norisk.heroes.common.HeroesManager.isServer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.silkmc.silk.core.Silk.serverOrThrow

object BlockStateSerializer : KSerializer<BlockState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockState")

    override fun serialize(encoder: Encoder, value: BlockState) {
        val nbt = NbtHelper.fromBlockState(value)
        encoder.encodeString(nbt.asString())
    }

    override fun deserialize(decoder: Decoder): BlockState {
        val nbtString = decoder.decodeString()
        val nbt = StringNbtReader.parse(nbtString)
        val world = if (isServer) {
            serverOrThrow.overworld
        } else {
            MinecraftClient.getInstance().world!!
        }

        val blockState = NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt)
        return blockState
    }
}
