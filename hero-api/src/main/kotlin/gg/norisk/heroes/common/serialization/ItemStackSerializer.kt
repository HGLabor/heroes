package gg.norisk.heroes.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.silkmc.silk.core.Silk.serverOrThrow

object ItemStackSerializer : KSerializer<ItemStack> {
    private val emptyItemStack = "EMPTY"
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ItemStack", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack) {
        if (value.isEmpty) {
            encoder.encodeString(emptyItemStack)
            return
        }
        val registryManager = serverOrThrow.registryManager
        val nbt = value.encode(registryManager) as NbtCompound
        val string = NbtHelper.toNbtProviderString(nbt)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val registryManager = serverOrThrow.registryManager
        val string = decoder.decodeString()
        if (string == emptyItemStack) {
            return ItemStack.EMPTY
        }
        val nbt = NbtHelper.fromNbtProviderString(string)
        return ItemStack.fromNbt(registryManager, nbt).orElseThrow()
    }
}