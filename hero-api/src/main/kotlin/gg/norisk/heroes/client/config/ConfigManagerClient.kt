package gg.norisk.heroes.client.config

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.config.ConfigNode
import gg.norisk.heroes.common.config.IConfigManager
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.utils.DynamicStringConversion
import kotlinx.serialization.json.Json
import net.silkmc.silk.core.task.mcCoroutineTask

object ConfigManagerClient : IConfigManager {
    override val configHolders = hashMapOf<String, ConfigNode>()
    override val JSON = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override fun init() {
        Networking.s2cHeroSettingsPacket.receiveOnClient { packet, context ->
            mcCoroutineTask(sync = true, client = true) {
                val decoded = JSON.decodeFromString<List<Hero.HeroJson>>(packet)
                for (heroJson in decoded) {
                    logger.info("Loading HeroJson ${heroJson.internalKey}")
                    HeroManager.getHero(heroJson.internalKey)?.load(heroJson)
                }
            }
        }
    }
}
