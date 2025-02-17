package gg.norisk.heroes.common.ffa.experience

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.prefix
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.common.utils.createIfNotExists
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import gg.norisk.heroes.server.database.player.PlayerProvider
import kotlinx.serialization.encodeToString
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText
import java.awt.Color

object Experience {
    private val configFile = HeroesManager.baseDirectory.resolve("xp-config.json").createIfNotExists()

    fun init() {
        loadConfig()
    }

    fun add(player: ServerPlayerEntity, reason: ExperienceReason, printMessage: Boolean = false) {
        mcCoroutineTask(sync = false, client = false) {
            val receivedXp = reason.value
            val ffaPlayer = PlayerProvider.get(player.uuid)
            ffaPlayer.xp += receivedXp
            player.ffaPlayer = ffaPlayer

            if (printMessage) {
                player.sendMessage(literalText {
                    text(prefix)
                    text("+$receivedXp XP") {
                        color = Color.GREEN.rgb
                    }
                })
            }

            PlayerProvider.save(ffaPlayer)
        }
    }

    private fun loadConfig() {
        val currentConfig = loadFromFile()
        createDefaultConfig(currentConfig.isEmpty())

        currentConfig.forEach { configReason ->
            val reason = ExperienceRegistry.reasons.firstOrNull { reason -> reason.key == configReason.key }
            if (reason == null) {
                logger.warn("Found invalid reason with key `${configReason.key}` in config")
                return@forEach
            }
            reason.value = configReason.value
        }
    }

    private fun loadFromFile(): MutableSet<ExperienceReason> {
        return runCatching<MutableSet<ExperienceReason>> {
            JSON.decodeFromString(configFile.readText())
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            logger.info("Loaded Xp Config")
        }.getOrDefault(mutableSetOf())
    }

    private fun createDefaultConfig(force: Boolean) {
        if (force) {
            configFile.createNewFile()
            configFile.writeText(JSON.encodeToString(ExperienceRegistry.reasons))
            logger.info("Created Default Xp Config")
        }
    }
}

fun PlayerEntity.addXp(reason: ExperienceReason, printMessage: Boolean = false) {
    Experience.add(this as ServerPlayerEntity, reason, printMessage)
}
