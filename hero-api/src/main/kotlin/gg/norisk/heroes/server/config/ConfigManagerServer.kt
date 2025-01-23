package gg.norisk.heroes.server.config

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.config.ConfigNode
import gg.norisk.heroes.common.config.IConfigManager
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.networking.Networking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.annotations.ExperimentalSilkApi

object ConfigManagerServer : IConfigManager {
    override val configHolders = hashMapOf<String, ConfigNode>()
    override val JSON = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun sendHeroSettings(vararg hero: Hero<*>, playerEntity: ServerPlayerEntity? = null) {
        val heroJsons = hero.map { it.toHeroJson() }
        val encoded = JSON.encodeToString(heroJsons)
        if (playerEntity != null) {
            Networking.s2cHeroSettingsPacket.send(encoded, playerEntity)
        } else {
            Networking.s2cHeroSettingsPacket.sendToAll(encoded)
        }
    }

    @OptIn(ExperimentalSilkApi::class)
    override fun init() {
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, sender, server ->
            sendHeroSettings(
                *HeroManager.registeredHeroes.values.toTypedArray(),
                playerEntity = handler.player
            )
        })

        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            HeroManager.reloadHeroes(*HeroManager.registeredHeroes.values.toTypedArray())
        })
    }
}
