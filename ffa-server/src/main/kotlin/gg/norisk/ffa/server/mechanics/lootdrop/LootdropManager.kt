package gg.norisk.ffa.server.mechanics.lootdrop

import kotlinx.coroutines.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.text.broadcastText
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object LootdropManager {
    private val MAP_DURATION = 30.minutes
    private val MAX_LOOTDROP_DELAY = 6.minutes
    private val LOOTDROP_COUNT = 6
    private val world by lazy { Silk.serverOrThrow.overworld }

    private var currentJob: Job? = null

    private var lootdropTimes: List<Long> = listOf()
    private var lootdropIndex: Int = 0

    fun onArenaReset() {
        currentJob?.cancel()
        lootdropTimes = generateLootdropTimes().map { it + System.currentTimeMillis() }
        lootdropIndex = 0

        currentJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (lootdropIndex >= lootdropTimes.size) return@launch

                if (System.currentTimeMillis() >= lootdropTimes[lootdropIndex]) {
                    lootdropIndex++
                    spawnLootdrop()
                }
                delay(1.seconds)
            }
        }
    }

    private fun spawnLootdrop() {
        val pos = findRandomLocation()
        Lootdrop(world, pos).drop()
        Silk.serverOrThrow.broadcastText("Dropping Lootdrop at ${pos.toShortString()}")
    }

    private fun findRandomLocation(): BlockPos {
        val worldBorder = world.worldBorder

        val centerX = worldBorder.centerX.toInt()
        val centerZ = worldBorder.centerZ.toInt()
        val radius = (worldBorder.size / 2).toInt()

        val x = centerX + Random.nextInt(-radius, radius)
        val z = centerZ + Random.nextInt(-radius, radius)
        return world.getTopPosition(Heightmap.Type.WORLD_SURFACE, BlockPos(x, 0, z))
    }

    private fun generateLootdropTimes(): List<Long> {
        val lootdropTimes = mutableListOf<Long>()
        val mapDurationMillis = MAP_DURATION.inWholeMilliseconds

        var nextSpawnTime = Random.nextLong(0, MAX_LOOTDROP_DELAY.inWholeMilliseconds)
        for (i in 0 until LOOTDROP_COUNT) {
            if (nextSpawnTime >= mapDurationMillis) break // damit keine lootdrops nach reset spawnen (aber ig sollte eh nicht?)

            lootdropTimes.add(nextSpawnTime)
            val spawnDelay = Random.nextLong(0, MAX_LOOTDROP_DELAY.inWholeMilliseconds)
            nextSpawnTime += spawnDelay
        }

        return lootdropTimes
    }

}
