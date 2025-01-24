package gg.norisk.datatracker.entity

import gg.norisk.datatracker.DataTracker.logger
import gg.norisk.datatracker.DataTracker.toId
import gg.norisk.datatracker.serialization.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.nbt.toNbt
import net.silkmc.silk.network.packet.s2cPacket
import org.joml.Vector3f
import java.util.*

interface ISyncedEntity {
    fun getSyncedValuesMap(): MutableMap<String, Any>
}

@Serializable
data class EntityWrapper(
    val entityId: Int,
    val key: String,
)

@Serializable
data class DataWrapper(
    val value: String,
    val clazz: String,
)

open class SyncedValueChangeEvent(val key: String, val entity: Entity, val oldValue: Any?)

val syncedValueChangeEvent = Event.onlySync<SyncedValueChangeEvent>()

fun initSyncedEntitiesClient() {
    addSyncedData.receiveOnClient { packet, context ->
        mcCoroutineTask(sync = true, client = true) {
            logger.debug("Received Packet {}", packet)
            val entity = context.client.world?.getEntityById(packet.first.entityId)

            if (registeredTypes.none { packet.second.clazz == it.key.toString() }) {
                throw Error("Please register a Serializer for $packet")
            }

            for ((clazz, serializer) in registeredTypes) {
                if (packet.second.clazz == clazz.toString()) {
                    val decodedValue = dataTrackerJson.decodeFromString(serializer as KSerializer<Any>, packet.second.value)
                    logger.info("Setting $entity $packet")
                    entity?.setSyncedData(packet.first.key, decodedValue)
                    break
                }
            }
        }
    }
    for ((clazz, value) in registeredTypes) {
        logger.info("Registering {} {}", clazz, value)
    }
    removeSyncedData.receiveOnClient { packet, context ->
        mcCoroutineTask(sync = true, client = true) {
            val entity = context.client.world?.getEntityById(packet.entityId)
            entity?.unsetSyncedData(packet.key)
        }
    }
}

//hier kann man noch weitere sachen registrieren
val registeredTypes = buildMap {
    put(Boolean::class, BooleanSerializer)
    put(String::class, StringSerializer)
    put(Float::class, FloatSerializer)
    put(Double::class, DoubleSerializer)
    put(UUID::class, UUIDSerializer)
    put(Int::class, IntSerializer)
    put(Long::class, LongSerializer)
    put(BlockPos::class, BlockPosSerializer)
    put(Vector3f::class, Vector3fSerializer)
}.toMutableMap()

val addSyncedData = s2cPacket<Pair<EntityWrapper, DataWrapper>>("add-sync".toId())
val removeSyncedData = s2cPacket<EntityWrapper>("remove-sync".toId())

internal val dataTrackerJson = Json {
    ignoreUnknownKeys = true
}

fun Entity.syncValue(key: String, value: Any, player: ServerPlayerEntity? = null) {
    if (registeredTypes.none { value::class == it.key }) {
        throw Error("Please register a Serializer for $value $key ${value::class}")
    }

    for ((clazz, serializer) in registeredTypes) {
        logger.debug("Value Class: {} Registered Class: {}  {}", value::class, clazz, value::class == clazz)
        if (value::class == clazz) {
            val encodedValue = dataTrackerJson.encodeToString(serializer as KSerializer<Any>, value)
            val pair = Pair(EntityWrapper(id, key), DataWrapper(encodedValue, clazz.toString()))
            logger.debug("Sending {}", pair)
            if (player != null) {
                addSyncedData.send(pair, player)
            } else {
                addSyncedData.sendToAll(pair)
            }
            break
        }
    }
}

internal fun Entity.writeSyncedNbtData(nbtCompound: NbtCompound) {
    for ((key, value) in (this as ISyncedEntity).getSyncedValuesMap()) {
        runCatching {
            //JUP, wäre irgendwie so geil wenn man den registered type code von oben smarter hier einbauen kann
            //das es direkt für alles geht aber erstmal low prio...
            when (value) {
                is Int -> {
                    nbtCompound.put(key, value.toNbt())
                }

                is Boolean -> {
                    nbtCompound.put(key, value.toNbt())
                }

                is String -> {
                    nbtCompound.put(key, value.toNbt())
                }

                is Double -> {
                    nbtCompound.put(key, value.toNbt())
                }

                is BlockPos -> {
                    nbtCompound.put(key, NbtHelper.fromBlockPos(value))
                }

                is Float -> {
                    nbtCompound.put(key, value.toNbt())
                }

                is Long -> {
                    nbtCompound.put(key, value.toNbt())
                }

                else -> {
                    logger.info("NOT SUPPORTED: [$key/$value] ${value::class}")
                }
            }
        }.onSuccess {}.onFailure {
            it.printStackTrace()
        }
    }
}

fun <T> Entity.getSyncedData(key: String): T? {
    return (this as ISyncedEntity).getSyncedValuesMap()[key] as? T?
}

fun <T> Entity.hasSyncedData(key: String): Boolean {
    return (this as ISyncedEntity).getSyncedValuesMap().containsKey(key)
}

fun Entity.syncValues(player: ServerPlayerEntity? = null) {
    for ((key, value) in (this as ISyncedEntity).getSyncedValuesMap()) {
        setSyncedData(key, value, player)
    }
}

fun Entity.unsetSyncedData(key: String, player: ServerPlayerEntity? = null) {
    logger.debug("Client={} Unset Synced Data {} {} {}", world.isClient, key, player)
    val oldValue = this.getSyncedData<Any?>(key)
    (this as ISyncedEntity).getSyncedValuesMap().remove(key)
    syncedValueChangeEvent.invoke(SyncedValueChangeEvent(key, this, oldValue))
    if (!world.isClient) {
        if (player != null) {
            removeSyncedData.send(EntityWrapper(id, key), player)
        } else {
            //TODO ka ob das jemals probleme machen sollte aber eig nicht oder
            removeSyncedData.sendToAll(EntityWrapper(id, key))
        }
    }
}


fun Entity.setSyncedData(key: String, value: Any?, player: ServerPlayerEntity? = null) {
    logger.debug("Client={} Synced Data {} {} {}", world.isClient, key, value, player)
    val oldValue = this.getSyncedData<Any?>(key)
    if (value == null) {
        (this as ISyncedEntity).getSyncedValuesMap().remove(key)
    } else {
        (this as ISyncedEntity).getSyncedValuesMap()[key] = value
    }
    syncedValueChangeEvent.invoke(SyncedValueChangeEvent(key, this, oldValue))
    if (!world.isClient) {
        if (value != null) {
            syncValue(key, value, player)
        } else {
            unsetSyncedData(key, player)
        }
    }
}
