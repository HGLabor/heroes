package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.ext.playEmote
import gg.norisk.emote.ext.stopEmote
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.ability.operation.MultiplyBase
import gg.norisk.heroes.common.command.DebugCommand.sendDebugMessage
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.common.utils.toBlockPos
import gg.norisk.heroes.katara.KataraManager.toEmote
import gg.norisk.heroes.katara.client.render.IFluidRendererExt
import gg.norisk.heroes.katara.client.sound.VelocityBasedFlyingSoundInstance
import gg.norisk.heroes.katara.entity.IWaterBendingPlayer
import gg.norisk.heroes.katara.event.FluidEvents
import gg.norisk.utils.OldAnimation
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object WaterPillarAbility {
    private val animation by lazy { OldAnimation(0f, 360f, 0.6.seconds.toJavaDuration()) }

    fun initClient() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register {
            val tickDelta = it.tickCounter().getTickDelta(false)
            for (player in it.world().players) {/*renderBlock(it.matrixStack() ?: return@register, player.getLerpedPos(tickDelta), Blocks.DIRT.defaultState)
                renderBlock(
                    it.matrixStack() ?: return@register,
                    player.getLerpedPos(tickDelta).add(1.0, 0.0, 0.0),
                    Fluids.WATER.defaultState.blockState,
                    true
                )*/

                val dummy = player as IWaterBendingPlayer
                val origin = player.waterPillarOrigin ?: continue
                val currentPos = player.getLerpedPos(tickDelta).add(0.0, 0.2, 0.0)
                val positions = calculatePositionsBetween(origin, currentPos, 10)
                val world = it.world()

                for ((index, position) in positions.withIndex()) {
                    val pos = position
                    renderBlock(
                        it.matrixStack() ?: return@register,
                        pos,
                        origin,
                        currentPos,
                        Fluids.WATER.defaultState.blockState,
                        true,
                        index = index
                    )
                    if (kotlin.random.Random.nextInt(1, 11) > 9) {
                        val offset = 0.5
                        val randomXOffset = kotlin.random.Random.nextDouble(-offset, offset)
                        val randomYOffset = kotlin.random.Random.nextDouble(-offset, offset)
                        val randomZOffset = kotlin.random.Random.nextDouble(-offset, offset)
                        //val particle = listOf(ParticleTypes.BUBBLE_POP, ParticleTypes.SPLASH)
                        world.addParticle(
                            ParticleTypes.SPLASH,
                            pos.x + randomXOffset,
                            pos.y + randomYOffset,
                            pos.z + randomZOffset,
                            0.0,
                            0.0,
                            0.0
                        )
                    }
                }
            }
        }
    }

    fun renderBlock(
        matrixStack: MatrixStack,
        pos: Vec3d,
        origin: BlockPos,
        playerPos: Vec3d,
        state: BlockState,
        fluid: Boolean = false,
        index: Int = 0, // Neuer Parameter für den Index
    ) {
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        val renderer = MinecraftClient.getInstance().blockRenderManager
        val world = MinecraftClient.getInstance().world ?: return
        val vertexConsumer = if (fluid) {
            MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
                RenderLayers.getFluidLayer(state.fluidState)
            )
        } else {
            MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
                RenderLayers.getBlockLayer(state)
            )
        }

        matrixStack.push()
        matrixStack.translate(
            pos.x - camera.pos.x, pos.y - camera.pos.y, pos.z - camera.pos.z
        )

        val scale = 2f / (Math.pow(1.1, index.toDouble())).toFloat()
        matrixStack.scale(scale, scale, scale)

        val dx = origin.x - playerPos.x
        val dz = origin.z - playerPos.z
        val angle = Math.toDegrees(Math.atan2(dz, dx)).toFloat() // Berechne den Winkel

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle))
        //matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))

        // Rotation um die Y-Achse
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animation.get()))


        matrixStack.translate(-0.5, -0.5, -0.5)

        if (fluid) {
            (renderer.fluidRenderer as IFluidRendererExt).katara_renderFluid(
                matrixStack, world, pos, vertexConsumer, state, state.fluidState, null
            )
        } else {
            renderer.renderBlock(state, pos.toBlockPos(), world, matrixStack, vertexConsumer, true, Random.create())
        }

        matrixStack.pop()

        if (animation.isDone) {
            animation.reset()
        }
    }


    fun rotateTowards(from: Vec3d?, to: Vec3d, original: Quaternionf): Quaternionf {
        val direction = to.subtract(from).normalize()
        val forward = Vector3f(0f, 0f, -1f)

        val dir = Vector3f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
        val axis = Vector3f()
        forward.cross(dir, axis).normalize()

        val dot = forward.dot(dir)
        val angle = acos(max(-1.0, min(1.0, dot.toDouble()))).toFloat()

        /*if (dot < -0.9999f) {
            axis[1f, 0f] = 0f
            if (abs(forward.x.toDouble()) > 0.999f) axis[0f, 1f] = 0f
        }*/

        val rotationQuat = Quaternionf().fromAxisAngleRad(axis, angle)
        return rotationQuat.mul(original)
    }


    fun initServer() {
        syncedValueChangeEvent.listen {
            if (it.key != WATER_PILLAR) return@listen
            val player = it.entity as? PlayerEntity ?: return@listen
            if (!player.world.isClient) {
                if (player.isWaterPillar) {/*player.modifyVelocity(Vec3d(0.0, 1.3, 0.0))
                    player.sound(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 0.3, 1f)
                    mcCoroutineTask(sync = true, client = false, delay = 0.5.seconds) {
                        //player.sendMessage("JETZT".literal)
                        player.abilities.allowFlying = true
                        player.abilities.flying = true
                        player.sendAbilitiesUpdate()
                    }*/
                } else {
                    (player as ServerPlayerEntity).cleanUpWaterBendingBlocks()
                }
            } else {
                if (player.isWaterPillar) {
                    (player as AbstractClientPlayerEntity).playEmote("waterpillar".toEmote())
                    MinecraftClient.getInstance().soundManager.play(VelocityBasedFlyingSoundInstance(player) { entity ->
                        (entity as? PlayerEntity?)?.isWaterPillar == true
                    })
                } else {
                    (player as AbstractClientPlayerEntity).stopEmote("waterpillar".toEmote())
                }
            }
        }

        FluidEvents.fluidTickEvent.listen { event ->
            for (player in event.world.players.filter { it.isWaterPillar }) {
                val dummy = player as IWaterBendingPlayer
                if (player.katara_waterPillarBlocks.contains(event.blockPos)) {
                    event.isCancelled.set(true)
                    return@listen
                }
            }
        }
    }

    private const val WATER_PILLAR = "WaterBending:WaterPillar"
    private const val WATER_PILLAR_ORIGIN = "WaterBending:WaterPillarOrigin"

    private var PlayerEntity.isWaterPillar: Boolean
        get() = this.getSyncedData<Boolean>(WATER_PILLAR) ?: false
        set(value) = this.setSyncedData(WATER_PILLAR, value)

    private var PlayerEntity.waterPillarOrigin: BlockPos?
        get() = this.getSyncedData<BlockPos>(WATER_PILLAR_ORIGIN)
        set(value) = this.setSyncedData(WATER_PILLAR_ORIGIN, value)

    private fun ServerPlayerEntity.cleanUpWaterBendingBlocks() {
        val dummy = this as IWaterBendingPlayer
        for (pos in this.katara_waterPillarBlocks) {
            if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                world.setBlockState(pos, Blocks.AIR.defaultState)
            }
        }
        sound(SoundEvents.ENTITY_GENERIC_SPLASH, 1f, 1f)
        katara_waterPillarBlocks.clear()

        if (interactionManager.gameMode.isSurvivalLike) {
            abilities.allowFlying = false
            abilities.flying = false
            sendAbilitiesUpdate()
        }
        (this as IWaterBendingPlayer).katara_waterPillarOrigin = null
    }

    private fun calculatePositionsBetween(start: BlockPos, end: Vec3d, steps: Int = 10): List<Vec3d> {
        val positions = mutableSetOf<Vec3d>()

        val controlPoint = Vec3d(
            (start.x + end.x) / 2.0,
            (start.y + end.y) / 2.0 + 5, // Raise the control point to create a curve
            (start.z + end.z) / 2.0
        )

        for (i in 0..steps) {
            val t = i / steps.toDouble()
            val oneMinusT = 1 - t

            val x = (oneMinusT.pow(2) * start.x) + (2 * oneMinusT * t * controlPoint.x) + (t.pow(2) * end.x)
            val y = (oneMinusT.pow(2) * start.y) + (2 * oneMinusT * t * controlPoint.y) + (t.pow(2) * end.y)
            val z = (oneMinusT.pow(2) * start.z) + (2 * oneMinusT * t * controlPoint.z) + (t.pow(2) * end.z)

            positions.add(Vec3d(x, y, z))
        }

        return positions.toList()
    }

    val waterPillarDistance = NumberProperty(15.0, 5, "Water Pillar Distance", AddValueTotal(3.0,3.0,3.0,3.0,3.0)).apply {
        icon = {
            Components.item(Items.SPYGLASS.defaultStack)
        }
    }
    val waterPillarVelocityBoost = NumberProperty(1.0, 5, "Water Pillar Start Boost", MultiplyBase(1.0, 1.2, 1.4, 1.5, 1.8, 1.9)).apply {
        icon = {
            Components.item(Items.FIREWORK_ROCKET.defaultStack)
        }
    }

    val ability = object : ToggleAbility("Water Pillar") {
        init {
            HeroesManager.client {
                this.keyBind = HeroKeyBindings.fourthKeyBinding
            }
            this.condition = {
                it.isTouchingWater
            }
            this.properties = listOf(
                waterPillarDistance,
                waterPillarVelocityBoost,
            )
            this.cooldownProperty =
                CooldownProperty(20.0, 4, "Cooldown", AddValueTotal(-5.0, -5.0, -2.0, -3.0))
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/packed_ice.png")
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.WATER_BUCKET.defaultStack)
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            if (player is ServerPlayerEntity) {
                player.playEmote("waterpillar_start".toEmote())
                mcCoroutineTask(sync = true, client = false, delay = 0.5.seconds) {
                    player.waterPillarOrigin = player.blockPos
                    (player as IWaterBendingPlayer).katara_waterPillarOrigin = player.blockPos
                    player.isWaterPillar = true
                    player.abilities.allowFlying = true
                    player.abilities.flying = true
                    player.modifyVelocity(Vec3d(0.0, waterPillarVelocityBoost.getValue(player.uuid), 0.0))
                    player.sound(SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, 0.3, 1f)
                    mcCoroutineTask(sync = true, client = false, delay = 0.5.seconds) {
                        player.sendAbilitiesUpdate()
                    }
                }
            }
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            if (player is ServerPlayerEntity) {
                if (player.isWaterPillar) {
                    player.waterPillarOrigin = null
                    player.isWaterPillar = false
                    player.cleanUpWaterBendingBlocks()
                }
            }
        }

        override fun onTick(player: PlayerEntity) {
            if (player is ServerPlayerEntity && player.isWaterPillar) {
                val dummy = player as IWaterBendingPlayer
                val otherPos = mutableSetOf<BlockPos>()
                val origin = player.waterPillarOrigin ?: return
                for (vec3d in calculatePositionsBetween(origin, player.pos)) {
                    for (blockPos in vec3d.toBlockPos().filledSpherePositionSet(3)) {
                        otherPos.add(blockPos)
                    }
                }
                val distance = sqrt(origin.getSquaredDistance(player.pos))
                val maxDistance = waterPillarDistance.getValue(player.uuid)

                if (distance >= maxDistance) {
                    player.sendMessage(Text.translatable("heroes.katara.ability.water_pillar.too_far_away"))
                    player.sendDebugMessage("Max Distance: ${maxDistance}".literal)
                    player.isWaterPillar = false
                    player.waterPillarOrigin = null
                    addCooldown(player)
                    return
                }

                val world = player.world
                for (freePos in otherPos) {
                    if (kotlin.random.Random.nextInt(1, 1000) > 960) {
                        if (world.getBlockState(freePos)
                                .isOf(Blocks.AIR) && world.getBlockState(freePos.down()).isSolid
                        ) {
                            world.setBlockState(freePos, Fluids.WATER.getFlowing(1, true).blockState)
                            world.playSound(
                                null,
                                freePos,
                                SoundEvents.ENTITY_GENERIC_SPLASH,
                                SoundCategory.BLOCKS,
                                0.3f,
                                kotlin.random.Random.nextDouble(0.8, 1.3).toFloat()
                            )
                        }
                    }
                }
            } else {
                val dummy = player as IWaterBendingPlayer
                val origin = player.waterPillarOrigin ?: return

                repeat(1) {
                    player.world.addParticle(
                        ParticleTypes.BUBBLE_POP,
                        player.getParticleX(0.5),
                        player.randomBodyY,
                        player.getParticleZ(0.5),
                        0.0,
                        0.0,
                        0.0
                    )
                }

                if (player.age.mod(10) == 0) {
                    for (vec3d in calculatePositionsBetween(
                        origin,
                        player.getLerpedPos(MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)),
                        10
                    )) {
                        player.world.playSound(
                            MinecraftClient.getInstance().player,
                            vec3d.x,
                            vec3d.y,
                            vec3d.z,
                            SoundEvents.BLOCK_WATER_AMBIENT,
                            SoundCategory.BLOCKS,
                            0.5f,
                            kotlin.random.Random.nextDouble(0.5, 1.1).toFloat()
                        )
                    }
                }
            }
        }
    }
}
