package gg.norisk.heroes.toph.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.emote.network.EmoteNetworking.stopEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.BlockOutlineRenderer
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.Networking.mouseScrollPacket
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.networking.dto.BlockInfoSmall
import gg.norisk.heroes.common.serialization.BlockPosSerializer
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.toph.TophManager.toEmote
import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.network.earthColumnBlockInfos
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.math.geometry.produceCirclePositions
import net.silkmc.silk.core.math.geometry.produceFilledSpherePositions
import net.silkmc.silk.core.task.mcCoroutineTask

val EarthColumnKey = "isEarthColumn"
val EarthColumnRadiusKey = "earth_column_radius"

//TODO enrico das ist ein fall für dich und deine descriptions oder?
@Serializable
data class EarthColumnDescription(
    val blocks: List<BlockInfoSmall>,
    val radius: Int,
    val center: @Serializable(with = BlockPosSerializer::class) BlockPos,
)

val EarthColumnInstantAbility = object : HoldAbility(
    "Earth Column"
) {
    init {
        client {
            this.keyBind = HeroKeyBindings.thirdKeyBind
            WorldRenderEvents.AFTER_TRANSLUCENT.register {
                val world = it.world()
                val player = MinecraftClient.getInstance().player ?: return@register
                val matrices = it.matrixStack() ?: return@register
                if (!player.isEarthColumn()) return@register
                val tickCounter = it.tickCounter()
                val radius = player.getSyncedData<Int>(EarthColumnRadiusKey) ?: 1
                val hitResult = player.raycast(maxDistance, tickCounter.getTickDelta(false), false)

                if (hitResult != null && hitResult.type == HitResult.Type.BLOCK) {
                    ((hitResult as BlockHitResult).blockPos).produceFilledSpherePositions(radius) { pos ->
                        val blockState: BlockState = world.getBlockState(pos)
                        if (world.canBeBended(pos, blockState) && blockState.isSolid) {
                            BlockOutlineRenderer.drawBlockBox(
                                matrices,
                                it.consumers() ?: return@produceFilledSpherePositions,
                                pos,
                                1.0f,
                                1.0f,
                                1.0f,
                                0.5f
                            )
                        }
                    }
                }
            }
        }

        this.cooldownProperty =
            buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
        this.maxDurationProperty =
            buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

        mouseScrollPacket.receiveOnServer { packet, context ->
            val player = context.player
            var radius = player.getSyncedData<Int>(EarthColumnRadiusKey) ?: 1
            radius += if (!packet) -1 else 1
            if (radius <= 0) {
                radius = 1
            }
            if (radius >= 5) {
                radius = 5
            }
            player.setSyncedData(EarthColumnRadiusKey, radius)
        }

        earthColumnBlockInfos.receiveOnServer { earthColumn, context ->
            val world = context.player.serverWorld
            val player = context.player
            mcCoroutineTask(sync = true, client = false, howOften = 5, period = 0.ticks) {
                earthColumn.move(world, it.round.toInt(), it.counterDownToZero == 0L)
            }
        }
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    val maxDistance = 45.0

    val EARTH_COLUMN_SLOW_BOOST = EntityAttributeModifier(
        "earth_column".toId(),
        -0.7,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )

    override fun onStart(player: PlayerEntity) {
        super.onStart(player)
        if (player is ServerPlayerEntity) {
            player.setSyncedData(EarthColumnKey, true)
            player.playEmote("earth-column-start".toEmote())
            player.world.playSoundFromEntity(
                null,
                player,
                SoundRegistry.ARM_WHOOSH,
                SoundCategory.PLAYERS,
                1f,
                1f
            )
            kotlin.runCatching {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    ?.addTemporaryModifier(EARTH_COLUMN_SLOW_BOOST)
            }
        }
    }

    override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
        super.onEnd(player, abilityEndInformation)
        if (player is ServerPlayerEntity) {
            val startTime = System.currentTimeMillis()
            val world = player.world as ServerWorld
            player.stopEmote("earth-column-start".toEmote())
            player.playEmote("earth-column-end".toEmote())
            player.world.playSoundFromEntity(
                null,
                player,
                SoundRegistry.EARTH_ARMOR,
                SoundCategory.PLAYERS,
                0.6f,
                1.4f
            )
            cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
            player.sound(SoundRegistry.STONE_SMASH)
            player.setSyncedData(EarthColumnKey, false)
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                ?.removeModifier(EARTH_COLUMN_SLOW_BOOST.id)
        } else if (player == MinecraftClient.getInstance().player) {
            val world = player.world
            val tickDelta = MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
            val hitResult = player.raycast(maxDistance, tickDelta, false)

            //Jap das ist sehr kriminell und wenn das rausgefunden wird wars das lmao
            //man mmüsste halt einen check einbauen liebe grüße an die zukunft XD <3
            //Der vorteil hierdran ist halt dass der client die blöcke berechnet (sehr wichtig)
            //Man könnte einen stichprobenartigen check einbauen
            if (hitResult != null && hitResult.type == HitResult.Type.BLOCK && player.isEarthColumn()) {
                val blockInfos = mutableSetOf<BlockInfoSmall>()
                val radius = player.getSyncedData<Int>(EarthColumnRadiusKey) ?: 1
                ((hitResult as BlockHitResult).blockPos).produceFilledSpherePositions(radius) { pos ->
                    val blockState: BlockState = world.getBlockState(pos)
                    if (world.canBeBended(pos, blockState)) {
                        repeat(32) {
                            val newPos = pos.up(it)
                            val newBlockState = world.getBlockState(newPos)
                            if (it == 0 && newBlockState.isSolid) {
                                blockInfos.add(BlockInfoSmall(world.getBlockState(pos.down(1)), pos.down(1)))
                            }
                            blockInfos.add(BlockInfoSmall(newBlockState, newPos))
                        }
                    }
                }

                earthColumnBlockInfos.send(
                    EarthColumnDescription(
                        blockInfos.toList(), radius, hitResult.blockPos
                    )
                )
            }
        }
    }
}

private fun EarthColumnDescription.move(world: ServerWorld, height: Int, isFinished: Boolean) {
    blocks.forEach { (state, pos) ->
        val newPos = pos.up(height)
        if (world.getBlockState(newPos) == state) return@forEach
        world.setBlockState(newPos, state)
        world.getOtherEntities(null, Box.from(newPos.toCenterPos())).filterIsInstance<LivingEntity>()
            .forEach { entity ->
                entity.teleport(
                    world,
                    entity.x,
                    entity.y + 1,
                    entity.z,
                    PositionFlag.VALUES,
                    entity.yaw,
                    entity.pitch
                )
            }
    }

    if (isFinished) {
        mcCoroutineTask(sync = true, client = false, delay = 0.ticks) {
            blocks.forEach { (state, pos) ->
                if (state.isAir) return@forEach
                val newPos = pos.up(it.round.toInt()).up().toCenterPos()
                world.getOtherEntities(null, Box.from(newPos).expand(2.0)).filterIsInstance<LivingEntity>()
                    .forEach { entity ->
                        entity.modifyVelocity(Vec3d(0.0, 2.0, 0.0))
                    }
            }
        }
    }

    center.up(height).apply {
        world.playSound(null, this, SoundRegistry.EARTH_COLUMN_1, SoundCategory.BLOCKS, 1f, 1f)
    }

    center.up().produceCirclePositions(radius) { pos ->
        val particlePos = pos.toCenterPos()
        world.spawnParticles(
            ParticleRegistry.EARTH_DUST,
            particlePos.x,
            particlePos.y - 0.3,
            particlePos.z,
            if (radius == 1) 1 else 5,
            (0.1..0.4).random(),
            (0.1..0.4).random(),
            (0.1..0.4).random(),
            (0.01..0.04).random()
        )
    }
}

fun World.canBeBended(blockPos: BlockPos, blockState: BlockState): Boolean {
    return true
}

fun PlayerEntity.isEarthColumn() = getSyncedData<Boolean>(EarthColumnKey) == true
