package gg.norisk.heroes.common

import gg.norisk.heroes.common.animations.IAnimationManager
import gg.norisk.heroes.common.config.IConfigManager
import gg.norisk.heroes.common.db.JsonProvider
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.ability.IAbilityManager
import gg.norisk.heroes.common.registry.SoundRegistry
import gg.norisk.heroes.server.HeroesManagerServer
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resource.featuretoggle.FeatureFlag
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import org.apache.logging.log4j.LogManager


object HeroesManager : ModInitializer {
    const val MOD_ID = "hero-api"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)

    val configManagers = mutableListOf<IConfigManager>()
    val animationManagers = mutableListOf<IAnimationManager>()
    val abilityManagers = mutableListOf<IAbilityManager>()
    lateinit var heroesFlag: FeatureFlag

    val prefix get() = literalText {
        text("[") {  }
        text("Heroes") {  }
        text("]") {  }
        text(" ")
    }

    val isServer get() = FabricLoader.getInstance().isDevelopmentEnvironment || FabricLoader.getInstance().environmentType == EnvType.SERVER
    val isClient get() = FabricLoader.getInstance().isDevelopmentEnvironment || FabricLoader.getInstance().environmentType == EnvType.CLIENT

    override fun onInitialize() {
        logger.info("Init Hero-Api Common...")
        SoundRegistry.init()
        HeroesManagerServer.initServer()
        logger.info("Config Folder... ${JsonProvider.baseFolder.absolutePath} ${System.getProperty("hero_folder_path")}")
    }

    fun client(callBack: () -> Unit) {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            callBack.invoke()
        }
    }
}
