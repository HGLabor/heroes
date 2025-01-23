package gg.norisk.heroes.common.config

import gg.norisk.heroes.common.HeroesManager.configManagers
import kotlinx.serialization.json.Json

interface IConfigManager {
    val configHolders: HashMap<String, ConfigNode>
    val JSON: Json

    fun init()

    fun register(node: ConfigNode) {
        if (node.name.isBlank()) throw IllegalArgumentException("The name of ConfigNodes must not be blank.")

        //TODO wieso
        //if (configHolders.contains(node.key)) throw IllegalArgumentException("Duplicate key of ConfigHolder: ${node.key}")
        configManagers.forEach { it.configHolders[node.key] = node }
    }
}
