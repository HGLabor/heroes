package gg.norisk.heroes.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.silkmc.silk.core.logging.logger
import org.bson.BsonBinary
import org.bson.UuidRepresentation
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: UUID) {
        runCatching {
            if (encoder is BsonEncoder && !SerializationHelper.isNameState(encoder)) {
                // Wenn BSON und kein NAME (wie in einer Map), serialize als binary
                val bsonBinary = BsonBinary(value, UuidRepresentation.STANDARD)
                return encoder.encodeBsonValue(bsonBinary)
            }
        }.onFailure {
            print("Caught exception when serializing uuid: ${it.stackTraceToString()}")
        }

        // Wenn nicht BSON, dann serialize als String
        encoder.encodeString(value.toString())
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): UUID {
        runCatching {
            if (decoder is BsonDecoder && !SerializationHelper.isNameState(decoder)) {
                val decoded = decoder.decodeBsonValue()
                //println("Value: $decoded")
                return decoded.asBinary().asUuid()
            }
        }.onFailure {
            print("Caught exception when serializing uuid: ${it.stackTraceToString()}")
        }
        // Wenn nicht BSON, dann serialize als String
        var uuidString = decoder.decodeString()
        if (!uuidString.contains("-")) {
            uuidString = insertDashUUID(uuidString)
        }
        return UUID.fromString(uuidString)
    }

    private fun insertDashUUID(uuid: String): String {
        var sb = StringBuilder(uuid)
        sb.insert(8, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(13, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(18, "-")
        sb = StringBuilder(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }
}
