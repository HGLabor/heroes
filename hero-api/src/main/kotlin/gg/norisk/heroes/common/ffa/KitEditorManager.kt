package gg.norisk.heroes.common.ffa

import com.github.juliarn.npclib.api.Npc
import com.github.juliarn.npclib.api.Position
import com.github.juliarn.npclib.api.event.AttackNpcEvent
import com.github.juliarn.npclib.api.event.ShowNpcEvent
import com.github.juliarn.npclib.api.profile.Profile
import com.github.juliarn.npclib.api.profile.ProfileProperty
import com.github.juliarn.npclib.api.protocol.meta.EntityMetadataFactory
import com.github.juliarn.npclib.common.event.DefaultInteractNpcEvent
import com.github.juliarn.npclib.fabric.FabricPlatform
import gg.norisk.heroes.common.HeroesManager.isServer
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.prefix
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.db.DatabaseInventory.Companion.loadInventory
import gg.norisk.heroes.common.db.DatabaseInventory.Companion.toDatabaseInventory
import gg.norisk.heroes.common.db.DatabaseManager
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.particle.ItemStackParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.silkmc.silk.core.event.ServerEvents
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object KitEditorManager {
    var world: ServerWorld? = null
    var resetInventory: (PlayerEntity) -> Unit = {
        it.inventory.clear()
        it.inventory.main.set(0, Items.STONE_SWORD.defaultStack)
    }
    var onBack: (ServerPlayerEntity) -> Unit = {
        it.teleport(it.server.overworld, 0.0, 100.0, 0.0, PositionFlag.VALUES, 0f, 0f)
    }
    private val kitEditorSpawn = Vec3d(0.5, 90.5, 0.5)
    val platform by lazy { FabricPlatform.minestomNpcPlatformBuilder().extension(this).actionController({}).build() }
    lateinit var backNpc: Npc<World, ServerPlayerEntity, ItemStack, Any>
    lateinit var resetNpc: Npc<World, ServerPlayerEntity, ItemStack, Any>
    val hasKitWorld get() = world != null

    fun init() {
        if (!isServer) return
        ServerLifecycleEvents.SERVER_STARTED.register {
            for (world in it.worlds) {
                println("$world ${world.registryKey.value}")
                if (world.registryKey.value == "kit-editor".toId()) {
                    this.world = world
                    break
                }
            }
            logger.info("Found Kit Editor World $world")
        }

        ServerTickEvents.END_WORLD_TICK.register {
            if (it == world) {
                for (player in it.players) {
                    val distance = player.squaredDistanceTo(kitEditorSpawn)
                    //player.sendMessage("Distance: $distance".literal)
                    if (distance > 300) {
                        teleportToKitEditorSpawn(player)
                    }
                }
            }
        }

        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { server, world ->
            if (world.registryKey.value == "kit-editor".toId()) {
                this.world = world
                this.world?.worldBorder?.size = 10000.0
                server.broadcastText("LOADED KIT EDITOR WORLD")
                server.broadcastText("LOADED KIT EDITOR WORLD")
                /*/world.timeOfDay = 6000
                world.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server)
                world.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server)
                world.gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(false, server)
                world.gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, server)
                world.gameRules.get(GameRules.DO_ENTITY_DROPS).set(false, server)*/
            }
        })

        ServerEntityEvents.ENTITY_LOAD.register(ServerEntityEvents.Load { entity, world ->
            val player = entity as? ServerPlayerEntity? ?: return@Load
            if (world == this.world) {
                player.changeGameMode(GameMode.ADVENTURE)
                val dbPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
                println("Loaded ${dbPlayer}")
                if (dbPlayer.inventory == null) {
                    resetInventory.invoke(player)
                    dbPlayer.inventory = player.toDatabaseInventory()
                }
                player.loadInventory(dbPlayer.inventory!!)
                entity.sendMessage("Joint Kit World".literal)
            }
        })

        ServerEntityEvents.ENTITY_UNLOAD.register(ServerEntityEvents.Unload { entity, world ->
            val player = entity as? ServerPlayerEntity? ?: return@Unload
            if (world == this.world) {
                val inventory = player.toDatabaseInventory()
                val dbPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
                dbPlayer.inventory = inventory
                player.dbPlayer = dbPlayer
                mcCoroutineTask(sync = false, client = false) {
                    DatabaseManager.provider.save(dbPlayer)
                    println("Saved ${dbPlayer}")
                    entity.sendMessage("Saved Kit Editor".literal)
                }
                entity.sendMessage("Leavt Kit World".literal)
                player.loadInventory(inventory)
            }
        })

        Networking.c2sKitEditorRequestPacket.receiveOnServer { packet, context ->
            mcCoroutineTask(sync = true, client = false) {
                val player = context.player
                val kitEditorWorld = world ?: return@mcCoroutineTask
                val event = HeroEvents.PreKitEditorEvent(player)
                HeroEvents.preKitEditorEvent.invoke(event)
                if (!event.isCancelled.get()) {
                    Networking.s2cHeroSelectorPacket.send(
                        HeroSelectorPacket(
                            emptyList(),
                            false,
                            hasKitWorld
                        ), player
                    )
                    teleportToKitEditorSpawn(player)
                    player.sendMessage(literalText {
                        text(prefix)
                        text("Hier kannst dein Inventar nach belieben anordnen damit es nächstes mal gespeichert wird")
                    })
                    kitEditorWorld.setBlockState(BlockPos(0, 89, 0), Blocks.GOLD_BLOCK.defaultState)
                }
            }
        }

        ServerEvents.postStart.listen { event ->
            if (world != null) {
                spawnNpcs()
                registerNpcEvents()
            }
        }
    }

    private fun registerNpcEvents() {
        val eventManager = platform.eventManager()
        eventManager.registerEventHandler(ShowNpcEvent.Post::class.java) { showEvent: ShowNpcEvent.Post ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            npc.changeMetadata(EntityMetadataFactory.skinLayerMetaFactory(), true).schedule(player)
        }
        eventManager.registerEventHandler(DefaultInteractNpcEvent::class.java) { showEvent: DefaultInteractNpcEvent ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            if (npc.entityId() == resetNpc.entityId()) {
                onReset(player)
            } else if (npc.entityId() == backNpc.entityId()) {
                onBack(player)
            }
        }
        eventManager.registerEventHandler(AttackNpcEvent::class.java) { showEvent: AttackNpcEvent ->
            val npc = showEvent.npc<World, ServerPlayerEntity, ItemStack, Any>()
            val player = showEvent.player<ServerPlayerEntity>()

            if (npc.entityId() == resetNpc.entityId()) {
                onReset(player)
            } else if (npc.entityId() == backNpc.entityId()) {
                onBack(player)
            }
        }
    }

    private fun onBack(player: ServerPlayerEntity) {
        onBack.invoke(player)
    }

    private fun onReset(player: ServerPlayerEntity) {
        resetInventory.invoke(player)
    }

    private fun teleportToKitEditorSpawn(player: ServerPlayerEntity) {
        player.teleport(
            world,
            kitEditorSpawn.x,
            kitEditorSpawn.y,
            kitEditorSpawn.z,
            PositionFlag.VALUES,
            0f,
            0f
        )
        player.playSoundToPlayer(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.3f, 1f)
        player.serverWorld.syncWorldEvent(2003, player.blockPos, 0)
    }

    private fun spawnEnderEyeBreak(blockPos: BlockPos, player: ServerPlayerEntity) {
        val d: Double = blockPos.getX().toDouble() + 0.5
        val e: Double = blockPos.getY().toDouble()
        val f: Double = blockPos.getZ().toDouble() + 0.5

        for (k in 0..7) {
            player.serverWorld.spawnParticles(
                player,
                ItemStackParticleEffect(ParticleTypes.ITEM, ItemStack(Items.ENDER_EYE)),
                false,
                d,
                e,
                f,
                1,
                player.world.random.nextGaussian() * 0.15,
                player.world.random.nextDouble() * 0.2,
                player.world.random.nextGaussian() * 0.15,
                0.0
            )
        }


        var g = 0.0
        while (g < Math.PI * 2) {
            player.serverWorld.spawnParticles(
                player,
                ParticleTypes.PORTAL,
                false,
                d + cos(g) * 5.0,
                e - 0.4,
                f + sin(g) * 5.0,
                1,
                cos(g) * -5.0,
                0.0,
                sin(g) * -5.0,
                0.0
            )
            player.serverWorld.spawnParticles(
                player,
                ParticleTypes.PORTAL,
                false,
                d + cos(g) * 5.0,
                e - 0.4,
                f + sin(g) * 5.0,
                1,
                cos(g) * -5.0,
                0.0,
                sin(g) * -5.0,
                0.0
            )
            g += Math.PI / 20
        }
    }

    private fun spawnNpcs() {
        backNpc = platform
            .newNpcBuilder()
            .flag(Npc.LOOK_AT_PLAYER, true)
            .flag(Npc.HIT_WHEN_PLAYER_HITS, true)
            .flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true)
            .position(Position.position(1.5, 90.00, 5.5, "hero-api:kit-editor"))
            .profile(
                Profile.resolved(
                    "FFA", UUID.randomUUID(), setOf(
                        ProfileProperty.property(
                            "textures",
                            "eyJ0aW1lc3RhbXAiOjE1ODQ0NjA2NDI0OTEsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbl9vcmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5MzEzZjM2OTdhMGZmZjk1MzE1NDlmMzNhNjIyZmUxOWY2MTZhN2Q1OTA3MTY2NDY1Y2EzMDYyOGMzYzFjZWEifX19",
                            "cdjJcFcAQtn6GtdJSkaLrQl2IlzUpkbDSSLl/a6/IGoJWJu7SDjZeXRKSJ55MYo5KZu38dG1dmlxiEhlF9pRfWtxW4+NXm7EI5fpKeoHBfXyxR3wJC5Yujo+9T+5TQkjAc4zGvgSQS4cRlqa231W4T77YLHCmV+E4rOVqvcXBsPomhtwckDwoD+NjfLH+PBcNkgYULgyUKSOvQVgbetgwjqrw8ZXt5LK9KWZsYKJZdUirapKwmXi/ZgD8h8z6i/K/3Qc4URjPTeqPahsr/hN/TWAGtr9TWf+iIgq91H8pau7FEMxuRgqayMlCLJD+JWjgkbK9Z6/HHJp7s7oGznn3MQy4Sj9vytRN0mLb+MsRwZ3ejOTopFfCynr7EdNSANcdJQUKk2/kjHwNSz067PSW4I+nzQA3tbFcohRkdUyDwZPs7Ajc9OadhS8W6AsQTPsNrxpNxf8yoO/vMvcIgwr/0PLI2VHUEWDVaDNUqzGDwHXn8O55ehje1ECFv5e48qFAC50xXrVJjN4Rtkq8OrjTamOSrHnm2PxlJUgthjqu6fxZZ1dBoKzMBlE56mIy9PLm0HjCS08zcQUvsK+IDW4l7ECWi1oRWrhPDt1wXD4AOlOeYln1C+KSlrBfdRNIW8bgx3pAaeI9Dm0qFpWjDZAKT/uxCs0Lwx0nySUYjM3yvo="
                        )
                    )
                )
            ).buildAndTrack()
        resetNpc = platform
            .newNpcBuilder()
            .flag(Npc.LOOK_AT_PLAYER, true)
            .flag(Npc.HIT_WHEN_PLAYER_HITS, true)
            .flag(Npc.SNEAK_WHEN_PLAYER_SNEAKS, true)
            .position(Position.position(-0.5, 90.00, 5.5, "hero-api:kit-editor"))
            .profile(
                Profile.resolved(
                    "RESET", UUID.randomUUID(), setOf(
                        ProfileProperty.property(
                            "textures",
                            "ewogICJ0aW1lc3RhbXAiIDogMTYxNjgzMjkxMTE4NSwKICAicHJvZmlsZUlkIiA6ICI1N2IzZGZiNWY4YTY0OWUyOGI1NDRlNGZmYzYzMjU2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYaWthcm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5OWIwNWI5YTFkYjRkMjliNWU2NzNkNzdhZTU0YTc3ZWFiNjY4MTg1ODYwMzVjOGEyMDA1YWViODEwNjAyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                            "TUpkJfSfrwykKlZAOWnURNM17wX5P+S7OCJDQkeeQyLqETA96DzqK25JwMpTeJNFYcSrkDYGM6ba7nPAewhBkB3U8JaI2hdNvOHw0rQk2bxMAfg+B3Tp+VxqQb2YPQL0z8hqrJOKjzjINSkAhI2g2/rYXfNizXjiUn4f1WUejvKzsYrTcGV0TlqJeJeydJ0nVpo9ssLVu5ksr+6mOKElwvVEMgPV+0VdWC5XH3jOVSQUX1rjIW5aS+nf0A90GKu7ENxv0j0Cj03IsrL1ytx+ZguFk2vxywr49i2l5iXAOwo/qO7+3mHyzYkEyl/so2zbo9VTTGkVLJ/bmQPcbBEF0HLxl3v/m0QoGy2x/cMR2BlITtAKRQOO2zSzDLZmScYSFr0aOnGmO1qvQexn6/JLrZrDqqXFsjFTuATVwHnEXiHSb4DJ5kZds6X1Fy/4UdYLxry423sMfXZeg+49+qvfNJlsg4v+gbcPtQIBMoBKEq+wexa0PBnH7WxpJbKQhyyiQG1tzrcmhZmbA8d2eD8zmsGammI+DCQJmF+Cu3J2ftbkjON0hj09Ow4uy56RCbLkwJigbXf8v6vpBSG7QzxIxKvhwiQHeaku4CyQ6VjxzIMGowM5v5O4x7FZZcRQh0N70/GjMnTWDaVK6htQ+OixHNJ14ju10zbkpyrq484ZmRQ="
                        )
                    )
                )
            ).buildAndTrack()
    }
}