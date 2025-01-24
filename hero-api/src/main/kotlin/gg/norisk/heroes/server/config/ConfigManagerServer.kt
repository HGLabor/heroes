package gg.norisk.heroes.server.config

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.networking.Networking
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity

object ConfigManagerServer {
    val JSON = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun sendHeroSettings(vararg hero: Hero, playerEntity: ServerPlayerEntity? = null) {
        val heroJsons = hero.map { it.toHeroJson() }
        val encoded = JSON.encodeToString(heroJsons)
        if (playerEntity != null) {
            Networking.s2cHeroSettingsPacket.send(encoded, playerEntity)
        } else {
            Networking.s2cHeroSettingsPacket.sendToAll(encoded)
        }
    }

    fun init() {
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
