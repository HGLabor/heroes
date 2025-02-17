package gg.norisk.ffa.server.world

import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.ffa.server.FFAServer.logger
import gg.norisk.ffa.server.mechanics.lootdrop.LootdropManager
import gg.norisk.ffa.server.schematic.SchematicHandler
import gg.norisk.ffa.server.world.MapPlacer.mapSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.SpawnLocating
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Server
import net.silkmc.silk.core.event.ServerEvents
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literal
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

object WorldManager {
    var mapReset = 30L
    var mapResetTask: Job? = null
    val counter = AtomicLong(mapReset)
    val worlds = mutableListOf<ServerWorld>()
    var currentWorldIndex = 0
    val schematicDir by lazy {
        Silk.serverOrThrow.runDirectory.resolve("schematics/").toFile().apply {
            if (!exists()) mkdirs()
        }
    }

    fun init() {
        ServerEvents.postStart.listen { event ->
            schematicDir.listFiles().forEach { file ->
                CoroutineScope(Dispatchers.IO).launch {
                    SchematicHandler.loadSchematic(file)
                }
            }
        }
    }

    fun mapResetCycle(server: MinecraftServer) {
        mapResetTask?.cancel()
        counter.set(mapReset)
        mapResetTask = infiniteMcCoroutineTask(period = 20.ticks, sync = true, client = false) {
            val players = server.playerManager.playerList
            if (players.isEmpty()) {
                return@infiniteMcCoroutineTask
            }
            //logger.info("Map Reset In: ${counter.get()}")
            counter.decrementAndGet()
            //if (counter.get() < 300) {
            for (player in players) {
                if (player.isFFA) {
                    Random
                    player.sendMessage("Map Reset ${counter.getTimeAsString()}".literal, true)
                }
            }
            //}
            if (counter.get() == 0L) {
                resetMap()
            }
        }
    }

    private fun resetMap() {
        val server = Silk.serverOrThrow
        val lastWorld = getCurrentWorld()
        mapResetCycle(server)

        lastWorld.players.forEach { player ->
            player.teleportToNewWorld(getCurrentWorld())
        }
        setWorldBorder(getCurrentWorld())
        LootdropManager.onArenaReset()
        SchematicHandler.pasteSchematic(lastWorld, SchematicHandler.cachedSchematics.values.random())
    }

    fun PlayerEntity.isInKitEditorWorld(): Boolean {
        return this.world.registryKey.value == Identifier.of("hero-api", "kit-editor")
    }

    fun AtomicLong.getTimeAsString(): String {
        val builder = StringBuilder()
        get().seconds.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) builder.append(days).append("d ")
            if (hours > 0) builder.append(hours).append("h ")
            if (minutes > 0) builder.append(minutes).append("m ")
            builder.append(seconds).append("s")
        }
        return builder.toString()
    }

    fun setWorldBorder(world: World) {
        world.worldBorder.setCenter(
            mapSize / 2.0,
            mapSize / 2.0
        )
        world.worldBorder.size = mapSize.toDouble()
    }

    fun getCurrentWorld(): ServerWorld {
        return worlds[currentWorldIndex % worlds.size]
    }

    fun initServer() {
        Events.Server.postStart.listen { event ->
            //MapPlacer.generateMap(Silk.serverOrThrow.overworld)
        }
        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { server, world ->
            world.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)
            world.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server)
            world.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server)

            if (world.registryKey.value.toString().contains("ffa-arena")) {
                worlds += world
                server.broadcastText("LOADED FFA ARENA: ${world.registryKey.value}")
            }
        })

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            logger.info("Init Map Reset Cycle...")
            //if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            mapResetCycle(server)
            setWorldBorder(getCurrentWorld())
            LootdropManager.onArenaReset()
            //}
        }
    }

    private fun ServerPlayerEntity.teleportToNewWorld(newWorld: ServerWorld) {
        val newY = run {
            var lastUnsafePos = blockPos

            if (newWorld.getBlockState(lastUnsafePos).isAir && newWorld.getBlockState(lastUnsafePos.up()).isAir) {
                while (!newWorld.isTopSolid(lastUnsafePos.down(), this)) {
                    lastUnsafePos = lastUnsafePos.down()
                }
                lastUnsafePos.y
            } else {
                while (newWorld.getBlockState(lastUnsafePos.up()).block != Blocks.AIR
                    || newWorld.getBlockState(lastUnsafePos.up().up()).block != Blocks.AIR
                ) {
                    lastUnsafePos = lastUnsafePos.up()
                }
                lastUnsafePos.up().y
            }
        }
        this.teleport(
            newWorld,
            this.x,
            newY.toDouble(),
            this.y,
            PositionFlag.VALUES,
            this.yaw,
            this.pitch
        )
    }

    fun ServerWorld.getCenter(): BlockPos {
        return BlockPos(0, topY, 0)
    }

    fun ServerWorld.findSpawnLocation(): BlockPos {
        val radius = (worldBorder.size / 2).toInt()
        val x = Random.nextInt(-radius, radius)
        val z = Random.nextInt(-radius, radius)
        return SpawnLocating.findOverworldSpawn(this, x, z) ?: this.findSpawnLocation()
    }
}
