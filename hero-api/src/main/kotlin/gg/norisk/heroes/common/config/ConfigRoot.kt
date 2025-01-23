package gg.norisk.heroes.common.config

import gg.norisk.heroes.common.HeroesManager.configManagers
import gg.norisk.heroes.common.serialization.ConfigNodeSerialization
import gg.norisk.heroes.common.serialization.SerializableConfigNode
import kotlinx.serialization.encodeToString
import net.fabricmc.loader.api.FabricLoader
import net.silkmc.silk.core.Silk
import java.io.File

abstract class ConfigRoot(name: String, val configPath: String?) : ConfigNode(name) {
    val file: File? by lazy {
        val server = Silk.server
        if (server == null || !server.isDedicated) return@lazy null
        val fileName = StringBuilder().apply {
            if (configPath != null) append("$configPath.")
            append(name)
        }.toString().replace('.', '/')

        File(FabricLoader.getInstance().gameDir.toFile(), "heroes/$fileName.json").also {
            if (!it.exists()) {
                it.parentFile.mkdirs()
                it.createNewFile()
            }
        }
    }

    val subConfigs = hashMapOf<String, ConfigNode>()

    fun addSubconfig(subConfig: ConfigNode) {
        subConfigs[subConfig.name] = subConfig
    }

    private fun serialized(): String {
        val serializable = ConfigNodeSerialization.createSerializable(this)
        return configManagers.random().JSON.encodeToString(serializable) //lmao
    }

    fun saveToFile() {
        val file = file ?: return
        file.writeText(serialized())
    }

    fun readFromFile() {
        var fileContent = file?.readText() ?: return
        if (fileContent.isBlank()) {
            saveToFile()
            fileContent = file?.readText() ?: return
        }

        val serializable = configManagers.random().JSON.decodeFromString<SerializableConfigNode>(fileContent)
        serializable.applyTo(this)
    }
}
