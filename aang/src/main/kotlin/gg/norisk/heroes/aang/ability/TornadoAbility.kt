package gg.norisk.heroes.aang.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.client.sound.TornadoSoundInstance
import gg.norisk.heroes.aang.entity.TornadoEntity
import gg.norisk.heroes.aang.entity.aang
import gg.norisk.heroes.aang.mixin.accessor.CameraAccessor
import gg.norisk.heroes.aang.registry.EntityRegistry
import gg.norisk.heroes.aang.utils.PlayerRotationTracker
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.network.packet.s2cPacket
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

object TornadoAbility {
    val tornadoSoundPacketS2C = s2cPacket<Int>("tornado-sound-packet".toId())
    var currentYaw: Float = 0f
    var currentPitch: Float = 0f
    var rotationAngle = 0f  // Variable zum Speichern des Rotationswinkels

    var PlayerEntity.isTornadoMode: Boolean
        get() = this.getSyncedData<Boolean>("isTornadoMode") ?: false
        set(value) = this.setSyncedData("isTornadoMode", value)

    fun Camera.handleTornadoCamera(blockView: BlockView, entity: Entity, bl: Boolean, bl2: Boolean, f: Float) {
        val dummy = (this as CameraAccessor)
        val player = entity as? PlayerEntity? ?: return
        val pos = pos.add(20.0, 19.0, 0.0)
        val (pitch, yaw) = lookAt(pos, entity.pos)
        if (!player.isTornadoMode) {
            currentYaw = yaw
            currentPitch = pitch
            return
        }
        currentYaw = MathHelper.lerp(f * 0.05f, currentYaw, yaw)
        currentPitch = MathHelper.lerp(f * 0.05f, currentPitch, pitch)
        invokeSetPos(pos.x, pos.y, pos.z)
        invokeSetRotation(currentYaw, currentPitch)
    }

    fun lookAt(currentPos: Vec3d, center: Vec3d): Pair<Float, Float> {
        val d = center.x - currentPos.x
        val e = center.y - currentPos.y
        val f = center.z - currentPos.z
        val g = sqrt(d * d + f * f)
        val pitch = MathHelper.wrapDegrees((-(MathHelper.atan2(e, g) * 180.0f / Math.PI.toFloat())).toFloat())
        val yaw = MathHelper.wrapDegrees((MathHelper.atan2(f, d) * 180.0f / Math.PI.toFloat()).toFloat() - 90.0f)
        return Pair(pitch, yaw)
    }

    private fun ServerPlayerEntity.summonTornado() {
        val tornadoEntity = EntityRegistry.TORNADO.create(this.serverWorld) ?: return
        aang.aang_tornadoEntity = tornadoEntity
        tornadoEntity.setPosition(this.pos)
        tornadoEntity.ownerId = this.id
        tornadoEntity.rotationTracker = PlayerRotationTracker()
        tornadoEntity.isGrowingMode = true
        aang.aang_tornadoTasks += mcCoroutineTask(sync = true, client = false, delay = 5.seconds) {
            tornadoEntity.isGrowingMode = false
            tornadoEntity.rotationTracker?.movementIncreaseRate =
                tornadoIncreaseRateProperty.getValue(this@summonTornado.uuid).toFloat()
            tornadoEntity.rotationTracker?.onlyDecay = true
            tornadoEntity.rotationTracker?.movementDecayRate =
                tornadoDecreaseRateProperty.getValue(this@summonTornado.uuid).toFloat()
            aang.aang_tornadoTasks += mcCoroutineTask(
                sync = true,
                client = false,
                delay = tornadoMaxDurationProperty.getValue(this@summonTornado.uuid).seconds
            ) {
                tornadoEntity.disappear(tornadoEntity.controllingPassenger)
            }
        }
        this.networkHandler.sendPacket(TitleS2CPacket("SPIN YOUR MOUSE".literal))
        this.networkHandler.sendPacket(TitleFadeS2CPacket(5, 20, 5))
        this.isTornadoMode = true
        this.serverWorld.spawnEntity(tornadoEntity)
        this.startRiding(tornadoEntity, true)
    }

    fun initClient() {
        tornadoSoundPacketS2C.receiveOnClient { packet, context ->
            mcCoroutineTask(sync = true, client = true) {
                val client = context.client
                val entity = context.client.world?.getEntityById(packet) as? TornadoEntity? ?: return@mcCoroutineTask
                client.soundManager.play(TornadoSoundInstance(entity))
            }
        }
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, tickCounter ->
            val player = MinecraftClient.getInstance().player ?: return@HudRenderCallback
            val vehicle = player.vehicle as? TornadoEntity? ?: return@HudRenderCallback
            if (vehicle.ownerId != player.id) return@HudRenderCallback
            if (!vehicle.isGrowingMode) return@HudRenderCallback
            val rotationTracker = vehicle.rotationTracker ?: return@HudRenderCallback
            val scale = rotationTracker.getPercentageBetween(1f, 5f)
            val speed = rotationTracker.getPercentageBetween(2f, 7f)

            val width = drawContext.scaledWindowWidth / 2
            val height = drawContext.scaledWindowHeight / 2
            val matrixStack = drawContext.matrices
            rotationAngle = (rotationAngle + speed) % 360f
            matrixStack.push()
            // Bewege den Ursprungspunkt auf die Mitte des Bildschirms
            matrixStack.translate(width.toDouble(), height.toDouble(), 0.0)
            matrixStack.scale(scale, scale, scale)

            // Drehe das Zeichen basierend auf der aktuellen Rotation
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle))

            // Bewege den Ursprungspunkt zurück, um den Text korrekt zu positionieren
            matrixStack.translate(-width.toDouble(), -height.toDouble(), 0.0)
            drawContext.drawText(MinecraftClient.getInstance().textRenderer, "↓".literal, width, height, -1, false)
            matrixStack.pop()
        })
    }

    fun init() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
        command("aang") {
            literal("toggleplayerrotationtracker") {
                runs {
                    this.source.playerOrThrow.summonTornado()
                }
            }
        }
    }

    val tornadoMaxDurationProperty = CooldownProperty(
        10.0, 3,
        "Max Duration",
        AddValueTotal(5.0, 5.0, 5.0)
    )
    val tornadoIncreaseRateProperty = NumberProperty(
        0.005, 3,
        "Tornado Increase Rate",
        AddValueTotal(0.0025, 0.0025, 0.005)
    ).apply {
        icon = {
            Components.item(Items.GLOWSTONE_DUST.defaultStack)
        }
    }
    val tornadoDecreaseRateProperty = NumberProperty(
        0.2, 3,
        "Tornado Decrease Rate",
        AddValueTotal(-0.0025, -0.0025, -0.005)
    ).apply {
        icon = {
            Components.item(Items.REDSTONE.defaultStack)
        }
    }

    val Ability = object : PressAbility("Tornado") {

        init {
            client {
                this.keyBind = HeroKeyBindings.fourthKeyBinding
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))

            this.properties =
                listOf(tornadoMaxDurationProperty, tornadoIncreaseRateProperty, tornadoDecreaseRateProperty)
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.WIND_CHARGE.defaultStack)
        }

        override fun hasUnlocked(player: PlayerEntity): Boolean {
            return player.isCreative || (AirBallAbility.Ability.cooldownProperty.isMaxed(player.uuid) && AirBallAbility.airBallMaxSize.isMaxed(
                player.uuid
            ))
        }

        override fun getUnlockCondition(): Text {
            return literalText {
                text(Text.translatable("heroes.ability.$internalKey.unlock_condition"))
            }
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/quartz_block_bottom.png")
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            cleanUp(player)
        }

        private fun cleanUp(player: PlayerEntity) {
            player.aang.aang_tornadoTasks.forEach { it.cancel() }
            player.aang.aang_tornadoEntity?.disappear(player)
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                if (!player.isTornadoMode) {
                    abilityScope.cancelCooldown()
                    player.summonTornado()
                } else {
                    cleanUp(player)
                }
            }
        }
    }
}
