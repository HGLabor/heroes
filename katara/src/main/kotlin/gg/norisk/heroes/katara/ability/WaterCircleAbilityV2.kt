package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.katara.ability.WaterBendingAbility.getCurrentBendingEntity
import gg.norisk.heroes.katara.client.render.IFluidRendererExt
import gg.norisk.heroes.katara.client.sound.WaterCircleSoundInstance
import gg.norisk.heroes.katara.entity.IKataraEntity
import gg.norisk.heroes.katara.entity.WaterBendingEntity
import gg.norisk.heroes.katara.registry.SoundRegistry
import gg.norisk.utils.OldAnimation
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.server.players
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object WaterCircleAbilityV2 {
    fun initServer() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("waterbending") {
                literal("watercircle") {
                    argument<Int>("amount") { amount ->
                        runs {
                            this.source.playerOrThrow.waterCircleAmount = amount()
                        }
                    }
                }
            }
        }
        syncedValueChangeEvent.listen { event ->
            if (event.key != WATER_CIRCLE_AMOUNT) return@listen
            val player = event.entity as? PlayerEntity? ?: return@listen
            if (player.world.isClient) {
                if (player.waterCircleAmount == 1) {
                    MinecraftClient.getInstance().soundManager.play(WaterCircleSoundInstance(player) { entity ->
                        ((entity as? PlayerEntity?)?.waterCircleAmount ?: 1) > 0
                    })
                }
            }
        }
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(AllowDamage { entity, source, amount ->
            if (entity is PlayerEntity) {
                if (source.isOf(DamageTypes.FALL) && entity.breakWaterCirclePiece()) {
                    return@AllowDamage false
                } else if ((source.isOf(DamageTypes.ON_FIRE)) && entity.breakWaterCirclePiece()) {
                    entity.extinguishWithSound()
                    return@AllowDamage false
                } else if ((source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.LAVA)) && entity.breakWaterCirclePiece()) {
                    entity.world.setBlockState(entity.blockPos, Fluids.WATER.getFlowing(1, true).blockState)
                    return@AllowDamage false
                }
            }
            return@AllowDamage true
        })
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick {
            for (player in it.players) {
                val currentEntity = player.getCurrentBendingEntity()
                if (currentEntity != null) {
                    (player as IKataraEntity).katara_entitySpinTracker.update(player)
                    if (player.katara_entitySpinTracker.hasSpunWildly()) {
                        (player as IKataraEntity).katara_entitySpinTracker.clear()
                        addToCircle(currentEntity, player)
                    }
                } else {
                    (player as IKataraEntity).katara_entitySpinTracker.clear()
                }
            }
        })
    }

    private fun addToCircle(entity: WaterBendingEntity?, player: PlayerEntity) {
        if (player.waterCircleAmount <= 0) {
            player.sound(SoundRegistry.WATER_CIRCLE_ADD,0.7f,Random.nextDouble(1.2,1.5))
            entity?.discard()
            player.waterCircleAmount += 1
        }
    }

    fun LivingEntity.breakWaterCirclePiece(): Boolean {
        if (waterCircleAmount == 1) {
            waterCircleAmount -= 1
            sound(SoundEvents.ENTITY_GENERIC_SPLASH, 1f, 1f)
            repeat(40) {
                (world as? ServerWorld?)?.spawnParticles(
                    ParticleTypes.SPLASH,
                    getParticleX(0.5) + Random.nextDouble(-1.0, 1.0),
                    randomBodyY + Random.nextDouble(-1.0, 1.0),
                    getParticleZ(0.5) + Random.nextDouble(-1.0, 1.0),
                    20,
                    0.001,
                    0.001,
                    0.001,
                    0.0
                )
            }
            return true
        }
        return false
    }

    fun initClient() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register {
            val clientPlayer = MinecraftClient.getInstance().player ?: return@register
            val tickDelta = it.tickCounter().getTickDelta(false)
            val matrixStack = it.matrixStack() ?: return@register
            for (player in it.world().players) {
                val amount = player.waterCircleAmount
                if (amount > 0) {
                    repeat(10) { index ->
                        renderWaterCircle(
                            player,
                            matrixStack,
                            player.getLerpedPos(tickDelta),
                            Fluids.FLOWING_WATER.defaultState.blockState,
                            OldAnimation(0.5f, 0.5f, 1.seconds.toJavaDuration()),
                            index,
                            10, // Pass the total amount for even distribution
                            System.currentTimeMillis() // Use current time in milliseconds for rotation
                        )
                    }
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun renderWaterCircle(
        player: PlayerEntity,
        matrixStack: MatrixStack,
        centerPos: Vec3d,
        state: BlockState,
        animation: OldAnimation,
        index: Int,
        totalAmount: Int,
        currentTimeMillis: Long
    ) {
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        val renderer = MinecraftClient.getInstance().blockRenderManager
        val world = MinecraftClient.getInstance().world ?: return
        val vertexConsumer = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
            RenderLayers.getFluidLayer(state.fluidState)
        )

        // Kreisposition berechnen
        val radius = 0.8 // Radius des Kreises
        val rotationSpeed = 0.002 // Geschwindigkeit der Rotation (Bogenmaß pro Millisekunde)
        val baseAngle = (index.toDouble() / totalAmount) * 2 * PI // Gleichmäßige Winkelverteilung entlang des Kreises
        val rotationOffset = (currentTimeMillis * rotationSpeed) % (2 * PI) // Rotationsversatz basierend auf der Zeit
        val angle = baseAngle + rotationOffset

        val timeFactor = (System.currentTimeMillis() % 10000L) / 1000.0 // Zeit in Sekunden (loop alle 10 Sekunden)
        val sineOffset = Math.sin((timeFactor + index * 0.5) * Math.PI) * 0.1 // Wellenbewegung mit tickDelta animiert


        val offsetX = radius * cos(angle)
        val offsetZ = radius * sin(angle)

        val blockPos = centerPos.add(offsetX, 0.0, offsetZ)

        matrixStack.push()
        matrixStack.translate(
            blockPos.x - camera.pos.x,
            blockPos.y - camera.pos.y + sineOffset,
            blockPos.z - camera.pos.z
        )
        matrixStack.translate(0.0, (player.height / 2.0), 0.0)
        matrixStack.scale(animation.get(), animation.get(), animation.get())
        matrixStack.multiply(rotateTowards(blockPos, centerPos, Quaternionf()))
        matrixStack.translate(-0.5, -0.5, -0.5)

        (renderer.fluidRenderer as IFluidRendererExt).katara_renderFluid(
            matrixStack,
            world,
            blockPos,
            vertexConsumer,
            state,
            state.fluidState,
            null
        )

        matrixStack.pop()
    }

    @Environment(EnvType.CLIENT)
    fun rotateTowards(from: Vec3d?, to: Vec3d, original: Quaternionf): Quaternionf {
        val direction = to.subtract(from).normalize()
        val forward = Vector3f(0f, 0f, -1f)

        val dir = Vector3f(direction.x.toFloat(), direction.y.toFloat(), direction.z.toFloat())
        val axis = Vector3f()
        forward.cross(dir, axis).normalize()

        val dot = forward.dot(dir)
        val angle = acos(max(-1.0, min(1.0, dot.toDouble()))).toFloat()

        val rotationQuat = Quaternionf().fromAxisAngleRad(axis, angle)
        return rotationQuat.mul(original)
    }

    private const val WATER_CIRCLE_AMOUNT = "WaterBending:WaterCircleAmount"

    var LivingEntity.waterCircleAmount: Int
        get() = this.getSyncedData<Int>(WATER_CIRCLE_AMOUNT) ?: 0
        set(value) = this.setSyncedData(WATER_CIRCLE_AMOUNT, value)
}
