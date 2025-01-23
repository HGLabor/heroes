package gg.norisk.heroes.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.Registries

object BlockStateSerializer : KSerializer<BlockState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockState")

    override fun serialize(encoder: Encoder, value: BlockState) {
        val nbt = NbtHelper.fromBlockState(value)
        encoder.encodeString(nbt.asString())
    }

    override fun deserialize(decoder: Decoder): BlockState {
        val nbtString = decoder.decodeString()
        val nbt = StringNbtReader.parse(nbtString)
        val blockState = NbtHelper.toBlockState(Registries.BLOCK.tagCreatingWrapper, nbt)
        return blockState
    }
}
