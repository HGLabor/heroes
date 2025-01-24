package gg.norisk.datatracker.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.joml.Vector3f

object Vector3fSerializer : KSerializer<Vector3f> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vector3f") {
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
    }

    override fun serialize(encoder: Encoder, value: Vector3f) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
        }
    }

    override fun deserialize(decoder: Decoder): Vector3f {
        return decoder.decodeStructure(descriptor) {
            var x = 0f
            var y = 0f
            var z = 0f

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeFloatElement(descriptor, index)
                    1 -> y = decodeFloatElement(descriptor, index)
                    2 -> z = decodeFloatElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            Vector3f(x, y, z)
        }
    }
}