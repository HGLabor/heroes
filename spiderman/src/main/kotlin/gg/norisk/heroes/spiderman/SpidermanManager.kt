package gg.norisk.heroes.spiderman

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager.registerHero
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.apache.logging.log4j.LogManager
import java.awt.Color

object SpidermanManager : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    private const val MOD_ID = "spiderman"
    val logger = LogManager.getLogger(MOD_ID)

    override fun onInitialize() {
        logger.info("Starting $MOD_ID Hero...")
    }

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            registerHero(Toph)
        }
    }

    override fun onInitializeServer() {
        registerHero(Toph)
    }

    val Toph by Hero("Spiderman") {
        color = Color.RED.rgb
    }
}
