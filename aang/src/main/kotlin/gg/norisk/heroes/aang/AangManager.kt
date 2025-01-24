package gg.norisk.heroes.aang

import gg.norisk.heroes.aang.ability.*
import gg.norisk.heroes.aang.registry.*
import gg.norisk.heroes.client.renderer.SkinUtils
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager.registerHero
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.awt.Color

object AangManager : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    const val MOD_ID = "aang"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)
    val aangSkin = "aang.png".toId()
    val aangOverlaySkin = "aang_overlay.png".toId()
    override fun onInitialize() {
        logger.info("Starting $MOD_ID Hero...")
        EntityRegistry.init()
        ParticleRegistry.init()
        EmoteRegistry.init()
        AirBallAbility.init()
        SpiritualProjectionAbility.init()
        SoundRegistry.init()
        TornadoAbility.init()
        LevitationAbility.init()
    }

    override fun onInitializeClient() {
        SkinUtils.initClient()
        EntityRendererRegistry.init()
        ParticleRendererRegistry.init()
        AirScooterAbility.initClient()
        AirBallAbility.initClient()
        TornadoAbility.initClient()
        SpiritualProjectionAbility.initClient()
        ClientLifecycleEvents.CLIENT_STARTED.register {
            registerHeroes()
        }
    }

    override fun onInitializeServer() {
        registerHero(Aang)
    }

    private fun registerHeroes() {
        registerHero(Aang)
    }

    val Aang by Hero("Aang") {
        ability(AirScooterAbility.Ability)
        ability(AirBallAbility.Ability)
        ability(SpiritualProjectionAbility.Ability)
        ability(TornadoAbility.Ability)
        ability(LevitationAbility.Ability)
        color = Color.decode("#33C3FFFF").rgb
        overlaySkin = aangOverlaySkin
    }
}
