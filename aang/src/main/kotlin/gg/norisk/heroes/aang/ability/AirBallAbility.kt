package gg.norisk.heroes.aang.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.ext.playEmote
import gg.norisk.emote.ext.stopEmote
import gg.norisk.heroes.aang.AangManager
import gg.norisk.heroes.aang.client.sound.AirBendingCircleSoundInstance
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.entity.aang
import gg.norisk.heroes.aang.registry.EmoteRegistry
import gg.norisk.heroes.aang.registry.EntityRegistry
import gg.norisk.heroes.aang.utils.CircleDetector3D
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.hero.isHero
import gg.norisk.heroes.common.networking.Networking.mousePacket
import gg.norisk.heroes.common.networking.Networking.mouseScrollPacket
import gg.norisk.heroes.common.networking.dto.MousePacket
import gg.norisk.heroes.common.utils.sound
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.random.Random

object AirBallAbility {
    val AIR_BENDING_KEY = "AangIsAirBending"
    val CURRENT_AIR_BENDING_KEY = "AangCurrentBendingId"

    val airBallMaxSize = NumberProperty(3.0, 3, "Max Size", AddValueTotal(1.0, 1.0, 3.0)).apply {
        icon = {
            Components.item(Items.WIND_CHARGE.defaultStack)
        }
    }

    fun initClient() {
        WorldRenderEvents.END.register(WorldRenderEvents.End { event ->
            val world = event.world() ?: return@End
            world.players.filter { it.isAirBending }.forEach { player ->
                player.spawnAirBendingParticle()
            }
        })
    }

    fun init() {
        syncedValueChangeEvent.listen { event ->
            if (event.key != AIR_BENDING_KEY) return@listen
            if (!event.entity.world.isClient) return@listen
            val player = event.entity as? AbstractClientPlayerEntity ?: return@listen
            if (player.isAirBending) {
                player.playEmote(EmoteRegistry.AIR_BENDING)
            } else {
                player.stopEmote(EmoteRegistry.AIR_BENDING)
            }
        }
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
        command("aang") {
            literal("toggleairbending") {
                runs {
                    val player = this.source.playerOrThrow
                    player.isAirBending = !player.isAirBending
                }
            }
        }
    }

    fun PlayerEntity.spawnAirBendingParticle() {
        if (!world.isClient) return
        val pos = this.getAirBendingPos()
        world.addParticle(
            ParticleTypes.CLOUD,
            pos.x,
            pos.y,
            pos.z,
            0.0,
            0.0,
            0.0
        )
    }

    fun PlayerEntity.getAirBendingPos(): Vec3d {
        return this.eyePos.add(this.directionVector.normalize().multiply(3.0))
    }

    fun PlayerEntity.handleTick() {
        val max = 85.0 //TODO als setting?
        if (!isHero(AangManager.Aang)) return
        val detector = aang.circleDetector ?: return
        val pos = getAirBendingPos()
        if (detector.addMouseMovement(pos.x, pos.y, pos.z)) {
            //this.sendMessage("Progress: ${detector.calculateCircleAccuracy()} $world".literal)
            val accuracy = detector.calculateCircleAccuracy()
            currentBendingEntity?.getAttributeInstance(EntityAttributes.GENERIC_SCALE)?.baseValue = 2 * (accuracy / 100)

            //TODO das als setting?
            if (accuracy >= max) {
                //this.sendMessage("Done $world".literal)
                this.isAirBending = false
                this.aang.circleDetector = null
                this.currentBendingEntity?.wasBended = true
                this.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.2, 1f)
            }
        }
    }

    var PlayerEntity.isAirBending: Boolean
        get() = this.getSyncedData<Boolean>(AIR_BENDING_KEY) ?: false
        set(value) = this.setSyncedData(AIR_BENDING_KEY, value)

    val PlayerEntity.currentBendingEntity: AirScooterEntity?
        get() {
            val id = if (currentBendingEntityId != -1) currentBendingEntityId else return null
            return world.getEntityById(id) as? AirScooterEntity?
        }

    var PlayerEntity.currentBendingEntityId: Int
        get() = this.getSyncedData<Int>(CURRENT_AIR_BENDING_KEY) ?: -1
        set(value) = this.setSyncedData(CURRENT_AIR_BENDING_KEY, value)

    private fun ServerPlayerEntity.scaleWindCharges(packet: Boolean) {
        val world = this.serverWorld

        val scale = 0.5
        val forceStrength = if (packet) scale else -scale

        val windCharges =
            world.iterateEntities().filterIsInstance<AirScooterEntity>()
                .filter { it.bendingType == AirScooterEntity.Type.PROJECTILE }
                .filter { it.ownerId == id }
        var soundFlag = true
        windCharges.forEach {
            val scaleAttribute =
                it.attributes.getCustomInstance(EntityAttributes.GENERIC_SCALE) ?: return@forEach
            scaleAttribute.baseValue += forceStrength
            if (scaleAttribute.baseValue < 0.5) {
                scaleAttribute.baseValue = 0.5
                soundFlag = false
            }
            if (scaleAttribute.baseValue > airBallMaxSize.getValue(this.uuid)) {
                scaleAttribute.baseValue = airBallMaxSize.getValue(this.uuid)
                soundFlag = false
            }
        }
        if (soundFlag) {
            windCharges.randomOrNull()?.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.1f, Random.nextDouble(0.8, 1.2))
        }
    }

    private fun ServerPlayerEntity.launchAnyAirBall(packet: MousePacket) {
        if (packet.isLeft() && packet.isClicked()) {
            val windCharges = getLaunchableWindCharges().randomOrNull()
            windCharges?.damage(this.damageSources.playerAttack(this), 0f)
        } else if (packet.isMiddle() && packet.isClicked()) {
            getLaunchableWindCharges().randomOrNull()?.launchBoomerang()
        }
    }

    private fun ServerPlayerEntity.getLaunchableWindCharges(): List<AirScooterEntity> {
        return serverWorld.iterateEntities().filterIsInstance<AirScooterEntity>()
            .filter { it.bendingType == AirScooterEntity.Type.PROJECTILE }
            .filter { it.wasBended }
            .filter { !it.wasLaunched }
    }

    val Ability = object : ToggleAbility("Air Ball") {
        init {
            client {
                this.keyBind = HeroKeyBindings.firstKeyBind
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
            this.maxDurationProperty =
                buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

            this.properties = listOf(airBallMaxSize)

            syncedValueChangeEvent.listen {
                val player = it.entity as? PlayerEntity ?: return@listen
                if (it.key == AIR_BENDING_KEY && player.world.isClient) {
                    if (player.isAirBending && player == MinecraftClient.getInstance().player) {
                        MinecraftClient.getInstance().soundManager.play(AirBendingCircleSoundInstance(player))
                    }
                }
            }

            mouseScrollPacket.receiveOnServer { packet, context ->
                mcCoroutineTask(sync = true, client = false) {
                    context.player.scaleWindCharges(packet)
                }
            }

            mousePacket.receiveOnServer { packet, context ->
                mcCoroutineTask(sync = true, client = false) {
                    context.player.launchAnyAirBall(packet)
                }
            }
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.WIND_CHARGE.defaultStack)
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/quartz_block_bottom.png")
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                val airScooter = EntityRegistry.AIR_SCOOTER.create(player.world) ?: return
                player.isAirBending = true
                player.aang.circleDetector = CircleDetector3D()
                airScooter.ownerId = player.id
                airScooter.bendingType = AirScooterEntity.Type.PROJECTILE
                airScooter.setPosition(player.getAirBendingPos())
                player.serverWorld.spawnEntity(airScooter)
                player.currentBendingEntityId = airScooter.id
            } else {
                player.aang.circleDetector = CircleDetector3D()
            }
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            player.stopAirBall()
        }

        private fun PlayerEntity.stopAirBall() {
            if (this is ServerPlayerEntity) {
                this.isAirBending = false
                if (this.aang.circleDetector != null) {
                    this.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.1, 1.5f)
                    this.currentBendingEntity?.discard()
                }
                this.aang.circleDetector = null
            } else {
                this.aang.circleDetector = null
            }
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            super.onEnd(player, abilityEndInformation)
            player.stopAirBall()
        }
    }
}
