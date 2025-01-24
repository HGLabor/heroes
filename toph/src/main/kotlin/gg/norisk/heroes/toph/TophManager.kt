package gg.norisk.heroes.toph

import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager.registerHero
import gg.norisk.heroes.toph.ability.*
import gg.norisk.heroes.toph.ability.EarthArmorAttributeModifiers.EarthArmorAbility
import gg.norisk.heroes.toph.particle.EarthDustParticle
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.block.BlockState
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.awt.Color

object TophManager : ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    private const val MOD_ID = "toph"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)
    fun String.toEmote(): Identifier {
        return "emotes/$this.animation.json".toId()
    }

    override fun onInitialize() {
        ParticleRegistry.init()
        SoundRegistry.init()
        logger.info("Starting $MOD_ID Hero...")
    }

    override fun onInitializeClient() {
        ParticleFactoryRegistry.getInstance()
            .register(ParticleRegistry.EARTH_DUST as ParticleType<ParticleEffect>, EarthDustParticle::CosySmokeFactory)
        ClientLifecycleEvents.CLIENT_STARTED.register {
            registerHero(Toph)
        }
    }

    override fun onInitializeServer() {
        registerHero(Toph)
    }

    val Toph by Hero("Toph") {
        ability(EarthArmorAbility)
        ability(SeismicSenseAbility)
        ability(EarthColumnInstantAbility)
        ability(EarthSurfAbility)
        ability(EarthPushAbility)
        ability(EarthTrapAbility)
        color = Color.GREEN.rgb
        overlaySkin = "textures/toph_overlay.png".toId()
    }

    val BlockState.isEarthBlock
        get() = isIn(BlockTags.PICKAXE_MINEABLE) || isIn(BlockTags.SHOVEL_MINEABLE)
}
