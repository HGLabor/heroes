package gg.norisk.datatracker.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@PublishedApi
internal object StringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor
    override fun serialize(encoder: Encoder, value: String): Unit = String.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): String = String.serializer().deserialize(decoder)
}

@PublishedApi
internal object BooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = Boolean.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Boolean): Unit = Boolean.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Boolean = Boolean.serializer().deserialize(decoder)
}


@PublishedApi
internal object IntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = Int.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Int): Unit = Int.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Int = Int.serializer().deserialize(decoder)
}

@PublishedApi
internal object FloatSerializer : KSerializer<Float> {
    override val descriptor: SerialDescriptor = Float.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Float): Unit = Float.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Float = Float.serializer().deserialize(decoder)
}

@PublishedApi
internal object DoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = Double.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Double): Unit = Double.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Double = Double.serializer().deserialize(decoder)
}

@PublishedApi
internal object LongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = Long.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Long): Unit = Long.serializer().serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Long = Long.serializer().deserialize(decoder)
}
