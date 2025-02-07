package gg.norisk.heroes.toph.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.BlockOutlineRenderer
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.utils.SphereUtils
import gg.norisk.heroes.common.utils.toVec
import gg.norisk.heroes.toph.TophManager.isEarthBlock
import gg.norisk.heroes.toph.TophManager.toEmote
import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.entity.toph
import gg.norisk.heroes.toph.mixin.render.GameRendererAccessor
import gg.norisk.heroes.toph.registry.SoundRegistry
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.silkmc.silk.core.entity.posUnder
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.seconds

val SeismicSenseShader = "shaders/post/seismic_sense.json".toId()
val SeismicSenseKey = "hasSeismicSense"

val SeismicSenseAbility = object : PressAbility("Seismic Sense") {
    init {
        client {
            this.keyBind = HeroKeyBindings.fourthKeyBinding

            WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
                val world = context.world()
                val player = MinecraftClient.getInstance().player ?: return@register
                val matrices = context.matrixStack() ?: return@register
                player.toph.seismicBlocks.removeIf { (timestamp, _) -> timestamp < System.currentTimeMillis() }
                player.toph.seismicBlocks.forEach { (_, pos) ->
                    val blockState: BlockState = world.getBlockState(pos)
                    if (!blockState.isAir && world.worldBorder.contains(pos) && blockState.isSolid) {
                        BlockOutlineRenderer.drawBlockBox(
                            matrices,
                            context.consumers() ?: return@forEach,
                            pos,
                            1.0f,
                            1.0f,
                            1.0f,
                            0.4f
                        )
                    }
                }
            }
        }

        this.cooldownProperty =
            buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    override fun onStart(player: PlayerEntity) {
        super.onStart(player)
        println("Seismic Sense Hallo $player")
        if (player is ServerPlayerEntity) {
            val world = player.world as ServerWorld
            player.playEmote("seismic-sense".toEmote())
            player.addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, 140, 2, false, false))
            mcCoroutineTask(sync = true, client = false, delay = 0.32.seconds) {
                player.addStatusEffect(StatusEffectInstance(StatusEffects.DARKNESS, 140, 1, false, false))
                player.setSyncedData(SeismicSenseKey, true)
                cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player)
                world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.SEISMIC_SENSE_START,
                    SoundCategory.PLAYERS,
                    2f,
                    1f
                )
                world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.EARTH_ARMOR,
                    SoundCategory.PLAYERS,
                    0.4f,
                    2f
                )
                mcCoroutineTask(sync = true, delay = 90.ticks, client = false) {
                    player.removeStatusEffect(StatusEffects.DARKNESS)
                    player.removeStatusEffect(StatusEffects.SLOWNESS)
                    player.setSyncedData(SeismicSenseKey, false)
                }
            }
        } else if (MinecraftClient.getInstance().player == player) {
            player.sendMessage("HALLO?".literal)
            mcCoroutineTask(sync = true, client = true, delay = 0.32.seconds) {
                val gameRenderer = MinecraftClient.getInstance().gameRenderer as GameRendererAccessor
                gameRenderer.invokeLoadPostProcessor(SeismicSenseShader)
                player.spawnSeismicSenseGlowCircle()
                mcCoroutineTask(sync = true, delay = 90.ticks, client = true) {
                    MinecraftClient.getInstance().gameRenderer.disablePostProcessor()
                }
            }
        }
    }
}

fun Entity.handleSeismicSenseOutline(cir: CallbackInfoReturnable<Boolean>) {
    val player = MinecraftClient.getInstance().player ?: return
    player.toph.seismicEntities.removeIf { (timestamp, _) -> timestamp < System.currentTimeMillis() }
    if (player.toph.seismicEntities.any { (_, uuid) -> uuid == this.uuid } && player.hasSeismicSense()) {
        cir.returnValue = true
    }
}

fun World.isVisibleVonAtleastEinerSeite(blockPos: BlockPos): Boolean {
    return Direction.values().any { direction -> getBlockState(blockPos.offset(direction)).isAir }
}

fun PlayerEntity.spawnSeismicSenseGlowCircle() {
    toph.seismicBlocks.clear()
    val player = this
    val world = this.world as ClientWorld

    val maxRadius = 50
    mcCoroutineTask(sync = true, client = true, howOften = 4, period = 10.ticks) {
        world.playSoundFromEntity(
            player,
            player,
            SoundRegistry.SEISMIC_SENSE_WAVE,
            SoundCategory.PLAYERS,
            0.5f,
            1f
        )
        repeat(maxRadius) { radius ->
            mcCoroutineTask(delay = radius.ticks, client = true, sync = true) {
                if (world.getBlockState(player.posUnder).isEarthBlock && hasSeismicSense()) {
                    SphereUtils.generateSphere(player.posUnder, radius, true).forEach {
                        val blockState = world.getBlockState(it)
                        /*if (blockState.isAir) {
                            getNextBottomBlock(world, it)?.apply {
                                seismicBlocks.add(Pair(System.currentTimeMillis() + 50L, this))
                            }
                        }*/
                        if (!blockState.isAir) {
                            if (world.isVisibleVonAtleastEinerSeite(it)) {
                                player.toph.seismicEntities.addAll(
                                    world.getOtherEntities(
                                        player,
                                        Box.from(it.toVec()).expand(2.0)
                                    ).map { entity -> entity.uuid }.map { Pair(System.currentTimeMillis() + 1000L, it) }
                                )
                                player.toph.seismicBlocks.add(Pair(System.currentTimeMillis() + 50L, it))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Entity.hasSeismicSense(): Boolean {
    val player = this as? PlayerEntity? ?: return false
    return player.getSyncedData<Boolean>(SeismicSenseKey) == true
}

fun handleSeismicSenseShader(ci: CallbackInfo) {
    val player = MinecraftClient.getInstance().player ?: return
    if (player.getSyncedData<Boolean>(SeismicSenseKey) == true) {
        ci.cancel()
    }
}

fun PlayerEntity.handleSeismicSenseDarkness(original: Float): Float {
    return original * 4
}
