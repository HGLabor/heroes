package gg.norisk.heroes.common

import gg.norisk.heroes.common.registry.SoundRegistry
import gg.norisk.heroes.server.HeroesManagerServer
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.featuretoggle.FeatureFlag
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import net.silkmc.silk.core.text.literalText
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Path


object HeroesManager : ModInitializer {
    const val MOD_ID = "hero-api"
    var baseDirectory: File = getBasePath(null)
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)

    lateinit var heroesFlag: FeatureFlag

    val prefix
        get() = literalText {
            text("[") { }
            text("Heroes") { }
            text("]") { }
            text(" ")
        }

    val isServer get() = FabricLoader.getInstance().environmentType == EnvType.SERVER || MinecraftClient.getInstance().server != null
    val isClient get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT

    override fun onInitialize() {
        logger.info("Init Hero-Api Common...")
        SoundRegistry.init()
        HeroesManagerServer.initServer()

        ServerLifecycleEvents.SERVER_STARTING.register {
            setBasePath(it.getSavePath(WorldSavePath("heroes")))
            logger.info("Found Server Path: ${baseDirectory}")
        }
    }

    private fun getBasePath(serverPath: Path?): File {
        val defaultPath = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
            FabricLoader.getInstance().configDir
        } else {
            serverPath ?: FabricLoader.getInstance().configDir
        }

        val baseDirectory = File(
            System.getProperty(
                "hero_folder_path",
                defaultPath.toFile().absolutePath
            ),
        ).apply {
            mkdirs()
        }

        return baseDirectory
    }

    private fun setBasePath(serverPath: Path?) {
        this.baseDirectory = getBasePath(serverPath)
    }

    fun client(callBack: () -> Unit) {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            callBack.invoke()
        }
    }
}
