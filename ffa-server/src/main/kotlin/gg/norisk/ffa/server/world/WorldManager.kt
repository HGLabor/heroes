package gg.norisk.ffa.server.world

import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.ffa.server.FFAServer.logger
import gg.norisk.ffa.server.mechanics.lootdrop.LootdropManager
import gg.norisk.ffa.server.utils.CloudNetManager
import gg.norisk.ffa.server.world.MapPlacer.chunkSize
import gg.norisk.ffa.server.world.MapPlacer.mapSize
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.utils.oldTeleport
import kotlinx.coroutines.Job
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.loader.api.FabricLoader
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
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Server
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

object WorldManager {
    var currentPair = Pair(0, 0)
    var mapReset = 30 * 60L
    var mapResetTask: Job? = null
    val usedMaps = mutableSetOf<Pair<Int, Int>>()
    val maxCount get() = (-chunkSize..chunkSize).count()
    val counter = AtomicLong(mapReset)

    fun initServer() {
        Events.Server.postStart.listen { event ->
            //MapPlacer.generateMap(Silk.serverOrThrow.overworld)
        }
        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { server, world ->
            world.gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server)
            world.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server)
            world.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server)
        })
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            logger.info("Init Map Reset Cycle...")
            usedMaps.clear()
            if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
                //restartServerTimer(server)
                mapResetCycle(server)
                setWorldBorder(server.overworld)
                LootdropManager.onArenaReset()
            }
        }

        command("ffa") {
            literal("resetmap") {
                requires { it.hasPermissionLevel(PermissionLevel.OWNER.level) }
                literal("settimer") {
                    argument<Long>("seconds") { seconds ->
                        suggestSingle { mapReset }
                        runs {
                            val sender = this.source
                            mapReset = seconds()
                            this.source.server.broadcastText(literalText {
                                text(HeroesManager.prefix)
                                text(sender.displayName)
                                text(" has set the map reset timer to ${mapReset.getTimeAsString()}!")
                            })
                        }
                    }
                }
                runs {
                    val sender = this.source
                    this.source.server.broadcastText(literalText {
                        text(HeroesManager.prefix)
                        text(sender.displayName)
                        text(" executed map reset!")
                    })
                    mapResetCycle(this.source.server, true)
                }
            }
        }
    }

    fun restartServerTimer(server: MinecraftServer) {
        val targetTime = LocalTime.of(4, 0) // Zielzeit: 3 Uhr nachts
        val now = LocalTime.now()

        // Berechne die Differenz zwischen jetzt und der Zielzeit
        val initialDelay = if (now.isBefore(targetTime)) {
            Duration.between(now, targetTime)
        } else {
            Duration.between(now, targetTime.plusHours(24))
        }

        val countdown = Duration.ofMinutes(5) // 5 Minuten Countdown

        // Ziehe den Countdown vom initialen Delay ab, damit der Neustart pünktlich erfolgt
        var adjustedDelay = initialDelay.minus(countdown)
        if (adjustedDelay.isNegative || adjustedDelay.isZero) {
            adjustedDelay = adjustedDelay.plusDays(1)
        }

        logger.info("Server Restart in ${adjustedDelay} ${adjustedDelay.seconds.getTimeAsString()}")

        // Starte den Countdown, wenn der berechnete Delay größer als 0 ist
        mcCoroutineTask(sync = true, delay = adjustedDelay.toKotlinDuration()) { delay ->
            mcCoroutineTask(sync = true, period = 20.ticks, howOften = countdown.seconds) { counter ->
                val left = counter.counterDownToZero
                if (left.mod(60) == 0 || left <= 5 || left == 10L || left == 20L || left == 30L) {
                    server.broadcastText("SERVER RESTART IN ${left.getTimeAsString()}")
                }

                if (left == 0L) {
                    restartServer(server)
                }
            }
        }
    }

    fun restartServer(server: MinecraftServer) {
        server.broadcastText("RESTARTING SERVER")
        CloudNetManager.stopCloudNetService()
    }

    fun mapResetCycle(server: MinecraftServer, force: Boolean = false) {
        currentPair = getFreeMapPos()
        if (force) {
            server.overworld.players.forEach { player ->
                player.teleportToNewMap(currentPair.first, currentPair.second)
            }
            setWorldBorder(server.overworld)
            LootdropManager.onArenaReset()
        }
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
                usedMaps.add(currentPair)
                mapResetCycle(server)
                server.overworld.players.forEach { player ->
                    player.teleportToNewMap(currentPair.first, currentPair.second)
                }
                setWorldBorder(server.overworld)
                LootdropManager.onArenaReset()
            }
        }
    }

    fun PlayerEntity.isInKitEditorWorld(): Boolean {
        return this.world.registryKey.value == Identifier.of("hero-api", "kit-editor")
    }

    fun Number.getTimeAsString(): String {
        val builder = StringBuilder()
        this.toInt().seconds.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) builder.append(days).append("d ")
            if (hours > 0) builder.append(hours).append("h ")
            if (minutes > 0) builder.append(minutes).append("m ")
            builder.append(seconds).append("s")
        }
        return builder.toString()
    }

    fun AtomicLong.getTimeAsString(): String {
        return get().getTimeAsString()
    }

    fun setWorldBorder(world: World) {
        world.worldBorder.setCenter(
            (currentPair.first * mapSize).toDouble() + mapSize / 2.0,
            (currentPair.second * mapSize).toDouble() + mapSize / 2.0
        )
        world.worldBorder.size = mapSize.toDouble()
    }

    fun getFreeMapPos(): Pair<Int, Int> {
        //val chunkX = Random.nextInt(-512 / mapSize, 3072 / mapSize)
        //val chunkZ = Random.nextInt(-512 / mapSize, 3072 / mapSize)
        val chunkX = Random.nextInt(-1, (chunkSize * 2))
        val chunkZ = Random.nextInt(-1, (chunkSize * 2))
        //val chunkX = Random.nextInt(-mapSize, mapSize * (chunkSize * 2)) / mapSize
        //val chunkZ = Random.nextInt(-mapSize, mapSize * (chunkSize * 2)) / mapSize

        val pair = Pair(chunkX, chunkZ)

        //TODO bypass für volle map sollte aber eig nicht passieren da große map
        if (usedMaps.size >= maxCount) {
            return pair
        }

        return if (usedMaps.contains(pair)) {
            getFreeMapPos()
        } else {
            pair
        }
    }

    fun ServerPlayerEntity.teleportToNewMap(x: Int, z: Int, mapSize: Int = MapPlacer.mapSize) {
        val world = this.serverWorld

        val relativeX = (this.blockPos.x and (mapSize - 1)) + this.x.fractional().absoluteValue
        val relativeZ = (this.blockPos.z and (mapSize - 1)) + this.z.fractional().absoluteValue

        //TODO bug dass es dings ist hä also z und x wird manchmal ganz seltsam

        val realCoordinateX = mapSize * x + relativeX
        val realCoordinateZ = mapSize * z + relativeZ
        this.oldTeleport(
            world,
            realCoordinateX,
            this.y,
            realCoordinateZ,
            PositionFlag.VALUES,
            this.yaw,
            this.pitch,
            true
        )
    }

    fun Double.fractional(): Double {
        return this - this.toInt()
    }

    fun ServerWorld.getCenter(): BlockPos {
        val x = (currentPair.first * mapSize).toDouble() + mapSize / 2.0
        val z = (currentPair.second * mapSize).toDouble() + mapSize / 2.0
        return BlockPos(x.toInt(), 64, z.toInt())
    }

    fun ServerWorld.findSpawnLocation(): BlockPos {
        //smaller number -> bigger radius
        val startMultiplier = 0.15
        val endMultiplier = startMultiplier * 2
        val xRange =
            (currentPair.first * mapSize + (mapSize * startMultiplier).toInt()..currentPair.first * mapSize + (mapSize - mapSize * endMultiplier).toInt())
        val zRange =
            (currentPair.second * mapSize + (mapSize * startMultiplier).toInt()..currentPair.second * mapSize + (mapSize - mapSize * endMultiplier).toInt())
        //logger.info("X-Range: $xRange | ${(mapSize * startMultiplier).toInt()} | ${(mapSize - mapSize * endMultiplier).toInt()}")
        //logger.info("Z-Range: $zRange | ${(mapSize * startMultiplier).toInt()} | ${(mapSize - mapSize * endMultiplier).toInt()}")
        return SpawnLocating.findOverworldSpawn(this, xRange.random(), zRange.random()) ?: this.findSpawnLocation()
    }
}
