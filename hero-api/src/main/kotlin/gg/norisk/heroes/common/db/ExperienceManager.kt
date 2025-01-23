package gg.norisk.heroes.common.db

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.prefix
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.server.config.ConfigManagerServer.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText
import java.awt.Color
import java.io.File

object ExperienceManager {
    private var reasons = mutableSetOf<Reason>()
    val KILLED_PLAYER = register("killed_player", 500)
    val PLAYER_DEATH = register("player_death", 25)
    val SOUP_EATEN = register("soup_eaten", 5)
    val SMALL_ABILITY_USE = register("small_ability_use", 5)
    val RECRAFT = register("soup_recraft", 5)
    val KILL_STREAK = register("kill_streak", 100)
    val END_KILL_STREAK = register("end_kill_streak", 1000)
    val DEALING_DAMAGE = register("dealing_damage", 1)
    val TAKING_DAMAGE = register("taking_damage", 1)
    val IDLE = register("idle", 1)

    fun register(key: String, value: Int): Reason {
        return Reason(key, value).apply {
            reasons.add(this)
        }
    }

    fun addXp(player: ServerPlayerEntity, reason: Reason, printMessage: Boolean = false) {
        var configReason = reasons.find { it.key == reason.key }
        if (configReason == null) {
            logger.info("Reason $reason was not found in config, using default")
            configReason = reason
        }

        val receivedXp = configReason.value
        val dbPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
        dbPlayer.xp += receivedXp
        player.dbPlayer = dbPlayer

        if (printMessage) {
            player.sendMessage(literalText {
                text(prefix)
                text("+$receivedXp XP") {
                    color = Color.GREEN.rgb
                }
            })
        }

        mcCoroutineTask(sync = false, client = false) {
            DatabaseManager.provider.save(dbPlayer)
        }
    }

    fun init() {
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            loadConfig()
        })
    }

    private val file
        get() = File(JsonProvider.baseFolder, "xp-config.json")

    fun loadConfig() {
        createDefaultConfig(loadFromFile().isEmpty())
        reasons = loadFromFile()
    }

    fun loadFromFile(): MutableSet<Reason> {
       return runCatching<MutableSet<Reason>> {
             JSON.decodeFromString(file.readText())
        }.onFailure {
            it.printStackTrace()
        }.onSuccess {
            logger.info("Loaded Xp Config")
        }.getOrDefault(mutableSetOf())
    }

    fun createDefaultConfig(force: Boolean) {
        if (!file.exists() or force) {
            file.createNewFile()
            file.writeText(JSON.encodeToString(reasons))
            logger.info("Created Default Xp Config")
        }
    }

    @Serializable
    data class Reason(val key: String, val value: Int)
}