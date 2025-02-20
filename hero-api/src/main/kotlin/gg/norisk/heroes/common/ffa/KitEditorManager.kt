package gg.norisk.heroes.common.ffa

import gg.norisk.heroes.common.HeroesManager.isServer
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.prefix
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.heroes.common.player.InventorySorting
import gg.norisk.heroes.common.player.InventorySorting.Companion.CURRENT_VERSION
import gg.norisk.heroes.common.player.InventorySorting.Companion.loadInventory
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.common.utils.PlayStyle
import gg.norisk.heroes.common.utils.oldTeleport
import gg.norisk.heroes.server.database.player.PlayerProvider
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.block.Blocks
import net.minecraft.entity.ItemEntity
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
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literalText
import kotlin.math.cos
import kotlin.math.sin

object KitEditorManager {
    var world: ServerWorld? = null
    var resetInventory: (PlayerEntity) -> Unit = {
        it.inventory.clear()
        it.inventory.main.set(0, Items.STONE_SWORD.defaultStack)
    }
    var onBack: (ServerPlayerEntity) -> Unit = {
        it.oldTeleport(it.server.overworld, 0.0, 100.0, 0.0, PositionFlag.VALUES, 0f, 0f, true)
    }
    private val kitEditorSpawn = Vec3d(0.5, 90.5, 0.5)

    val hasKitWorld get() = world != null

    fun init() {
        if (!isServer) return
        ServerLifecycleEvents.SERVER_STARTED.register {
            for (world in it.worlds) {
                logger.info("Found Worlds: $world ${world.registryKey.value}")
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

        ServerEntityEvents.ENTITY_LOAD.register { entity, world ->
            val item = entity as? ItemEntity? ?: return@register
            if (item.world == this.world) {
                entity.discard()
            }
        }

        ServerEntityEvents.ENTITY_LOAD.register(ServerEntityEvents.Load { entity, world ->
            val player = entity as? ServerPlayerEntity? ?: return@Load
            if (world == this.world) {
                player.changeGameMode(GameMode.ADVENTURE)
                mcCoroutineTask(sync = false, client = false) {
                    val ffaPlayer = PlayerProvider.get(player.uuid)
                    logger.info("Loaded ${ffaPlayer}")
                    if (ffaPlayer.inventorySorting == null) {
                        resetInventory.invoke(player)
                        ffaPlayer.inventorySorting = player.toDatabaseInventory()
                    }
                    mcCoroutineTask(sync = true, client = false) {
                        player.loadInventory(ffaPlayer.inventorySorting!!)
                    }
                    entity.sendMessage(Text.translatable("ffa.mechanic.kit.editor.enter"))
                }
            }
        })

        ServerEntityEvents.ENTITY_UNLOAD.register(ServerEntityEvents.Unload { entity, world ->
            val player = entity as? ServerPlayerEntity? ?: return@Unload
            if (world == this.world) {
                val inventory = player.toDatabaseInventory()
                mcCoroutineTask(sync = false, client = true) {
                    val ffaPlayer = PlayerProvider.get(player.uuid)
                    ffaPlayer.inventorySorting = inventory
                    player.ffaPlayer = ffaPlayer
                    mcCoroutineTask(sync = false, client = false) {
                        PlayerProvider.save(ffaPlayer)
                        println("Saved ${ffaPlayer}")
                        entity.sendMessage(Text.translatable("ffa.mechanic.kit.editor.save"))
                    }
                    entity.sendMessage(Text.translatable("ffa.mechanic.kit.editor.left"))
                    mcCoroutineTask(sync = true, client = false) {
                        player.loadInventory(inventory)
                    }
                }
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
                        text(Text.translatable("ffa.mechanic.kit.editor.inventory_instruction"))
                    })
                    kitEditorWorld.setBlockState(BlockPos(0, 89, 0), Blocks.GOLD_BLOCK.defaultState)
                }
            }
        }
    }


    fun onBack(player: ServerPlayerEntity) {
        onBack.invoke(player)
    }

    fun onReset(player: ServerPlayerEntity) {
        resetInventory.invoke(player)
    }

    private fun teleportToKitEditorSpawn(player: ServerPlayerEntity) {
        player.oldTeleport(
            world!!,
            kitEditorSpawn.x,
            kitEditorSpawn.y,
            kitEditorSpawn.z,
            PositionFlag.VALUES,
            0f,
            0f,
            true
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
                true,
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
                true,
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
                true,
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

    private fun ServerPlayerEntity.toDatabaseInventory(): InventorySorting {
        return InventorySorting(
            uuid,
            PlayStyle.current,
            CURRENT_VERSION,
            inventory.armor.toTypedArray(),
            inventory.offHand.toTypedArray(),
            inventory.main.toTypedArray(),
        )
    }
}
