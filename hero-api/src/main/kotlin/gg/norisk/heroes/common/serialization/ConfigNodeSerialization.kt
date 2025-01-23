package gg.norisk.heroes.common.serialization

import gg.norisk.heroes.common.config.ConfigNode
import gg.norisk.heroes.common.config.ConfigRoot
import gg.norisk.heroes.common.utils.DynamicStringConversion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

object ConfigNodeSerialization {
    private fun convertSettings(configNode: ConfigNode): HashMap<String, JsonPrimitive> {
        val settings = hashMapOf<String, JsonPrimitive>()
        configNode.settings.forEach { (key, setting) ->
            val jsonValue = when (val value = setting.get()) {
                is Boolean -> JsonPrimitive(value)
                is Number -> JsonPrimitive(value)
                is String -> JsonPrimitive(value)
                else -> throw IllegalArgumentException("Unsupported type: $value")
            }
            settings[key] = jsonValue
        }
        return settings
    }

    fun createSerializable(configNode: ConfigNode): SerializableConfigNode {
        val settings = convertSettings(configNode)
        val subConfigs = hashMapOf<String, SerializableConfigNode>()

        if (configNode is ConfigRoot) {
            configNode.subConfigs.forEach { (key, subConfig) ->
                if (subConfig.settings.isEmpty()) return@forEach
                subConfigs[key] = createSerializable(subConfig)
            }
        }

        return SerializableConfigNode(settings, subConfigs.takeIf { it.isNotEmpty() })
    }
}

@Serializable
class SerializableConfigNode(
    private val settings: HashMap<String, JsonPrimitive>,
    private val subConfigs: HashMap<String, SerializableConfigNode>? = null
) {

    fun applyTo(configNode: ConfigNode) {
        settings.forEach { (key, value) ->
            val setting = configNode.settings[key] ?: return@forEach
            setting.set(DynamicStringConversion.convertString(value.toString(), setting.defaultValue::class))
        }

        if (configNode !is ConfigRoot) return
        subConfigs?.forEach { (key, subConfig) ->
            val config = configNode.subConfigs[key] ?: return@forEach
            subConfig.applyTo(config)
        }
    }
}