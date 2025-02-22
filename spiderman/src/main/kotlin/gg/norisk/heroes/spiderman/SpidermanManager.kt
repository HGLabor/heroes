package gg.norisk.heroes.spiderman

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager.registerHero
import gg.norisk.heroes.spiderman.ability.WebShootAbility
import gg.norisk.heroes.spiderman.registry.EntityRegistry
import gg.norisk.heroes.spiderman.registry.EntityRendererRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.awt.Color

object SpidermanManager : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    private const val MOD_ID = "spiderman"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)

    override fun onInitialize() {
        logger.info("Starting $MOD_ID Hero...")
        EntityRegistry.init()
    }

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            registerHero(Spiderman)
        }
        EntityRendererRegistry.init()
    }

    override fun onInitializeServer() {
        registerHero(Spiderman)
    }

    val Spiderman by Hero("Spiderman") {
        color = Color.RED.rgb
        ability(WebShootAbility)
    }
}
