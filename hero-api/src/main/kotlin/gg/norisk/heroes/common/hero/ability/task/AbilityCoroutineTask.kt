package gg.norisk.heroes.common.hero.ability.task

import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.hero.ability.AbilityScope
import kotlinx.coroutines.*
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.*
import java.util.*
import kotlin.time.Duration

object AbilityCoroutineManager {
    val playerJobs = hashMapOf<UUID, MutableList<Job>>()

    fun cancelServerJobs(player: PlayerEntity) {
        playerJobs[player.uuid]?.forEach(Job::cancel)
        playerJobs.remove(player.uuid)
    }

    fun cancelClientJobs() {
        val uuid = playerJobs.keys.firstOrNull()
        playerJobs[uuid]?.forEach(Job::cancel)
        playerJobs.remove(uuid)
    }
}

@OptIn(DelicateSilkApi::class)
inline fun abilityCoroutineTask(
    executingPlayer: PlayerEntity,
    sync: Boolean = true,
    client: Boolean = false,
    scope: CoroutineScope = if (sync) {
        if (client) mcClientCoroutineScope else mcCoroutineScope
    } else silkCoroutineScope,
    howOften: Long = 1,
    period: Duration = 1.ticks,
    delay: Duration = Duration.ZERO,
    crossinline task: suspend CoroutineScope.(task: CoroutineTask) -> Unit
): Job {
    val uuid = if (client) MinecraftClient.getInstance().player!!.uuid else executingPlayer.uuid
    return mcCoroutineTask(sync, client, scope, howOften, period, delay) { coroutineTask ->
        if (isActive && !executingPlayer.isAlive) {
            logger.info("${executingPlayer.name.literalString} is currently dead, cancelling coroutine job")
            cancel()
        }
        ensureActive()
        task(this, coroutineTask)
        AbilityCoroutineManager.playerJobs[uuid]?.remove(this.coroutineContext.job)
    }.also { job ->
        AbilityCoroutineManager.playerJobs.computeIfAbsent(uuid) { mutableListOf() }.add(job)
    }
}
