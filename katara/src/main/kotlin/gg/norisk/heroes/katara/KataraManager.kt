package gg.norisk.heroes.katara

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager.registerHero
import gg.norisk.heroes.katara.ability.*
import gg.norisk.heroes.katara.registry.EntityRegistry
import gg.norisk.heroes.katara.registry.EntityRendererRegistry
import gg.norisk.heroes.katara.registry.SoundRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object KataraManager : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    const val MOD_ID = "katara"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)
    fun String.toEmote(): Identifier {
        return "emotes/$this.animation.json".toId()
    }

    val waterBenderOverlay = "waterbender_overlay.png".toId()

    override fun onInitialize() {
        logger.info("Starting $MOD_ID Hero...")
        SoundRegistry.init()
        WaterPillarAbility.initServer()
        HealingAbility.initServer()
        WaterFormingAbility.initServer()
        WaterBendingAbility.initServer()
        WaterCircleAbilityV2.initServer()
        EntityRegistry.init()
        //WaterBubbleAbility.initServer()
    }

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            registerHeroes()
        }
        EntityRendererRegistry.init()
        WaterPillarAbility.initClient()
        HealingAbility.initClient()
        WaterFormingAbility.initClient()
        WaterCircleAbilityV2.initClient()
        WaterBendingAbility.initClient()
    }

    override fun onInitializeServer() {
        registerHero(Katara)
    }

    private fun registerHeroes() {
        registerHero(Katara)
    }

    val Katara: Hero by Hero("Katara") {
        ability(WaterBendingAbility.ability)
        ability(WaterCircleAbilityV2.ability)
        ability(WaterPillarAbility.ability)
        ability(WaterFormingAbility.ability)
        ability(IceShardAbility.ability)
        ability(HealingAbility.ability)
        color = 0x416bdf
        overlaySkin = waterBenderOverlay
    }
}
