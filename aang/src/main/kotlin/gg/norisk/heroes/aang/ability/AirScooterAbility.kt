package gg.norisk.heroes.aang.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.emote.network.EmoteNetworking.stopEmote
import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility.isUsingSpiritualProjection
import gg.norisk.heroes.aang.client.sound.AirScooterSoundInstance
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.entity.aang
import gg.norisk.heroes.aang.registry.EmoteRegistry
import gg.norisk.heroes.aang.registry.EntityRegistry
import gg.norisk.heroes.aang.registry.ParticleRegistry
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.Speedlines.showSpeedlines
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.hero.ability.task.abilityCoroutineTask
import gg.norisk.heroes.common.utils.sound
import gg.norisk.utils.Easing
import gg.norisk.utils.OldAnimation
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import kotlinx.coroutines.cancel
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.network.packet.s2cPacket
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object AirScooterAbility {
    val airScooterSoundPacketS2C = s2cPacket<Int>("air-scooter-sound".toId())
    val AIR_SCOOTING_KEY = "AangIsAirScooting"

    val airScooterSpeed = NumberProperty(0.2, 3, "Speed", AddValueTotal(0.1, 0.1, 0.1)).apply {
        icon = {
            Components.item(Items.WIND_CHARGE.defaultStack)
        }
    }
    val airScooterStepHeight = NumberProperty(2.0, 3, "Step Height", AddValueTotal(1.0, 1.0, 1.0)).apply {
        icon = {
            Components.item(Items.QUARTZ_STAIRS.defaultStack)
        }
    }

    fun initClient() {
        airScooterSoundPacketS2C.receiveOnClient { packet, context ->
            mcCoroutineTask(sync = true, client = true) {
                val client = context.client
                val entity = context.client.world?.getEntityById(packet) ?: return@mcCoroutineTask
                client.soundManager.play(AirScooterSoundInstance(entity))
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { event ->
            val world = event.world ?: return@EndTick
            for (player in world.players) {
                val scooter = world.entities.filterIsInstance<AirScooterEntity>()
                    .filter { it.bendingType == AirScooterEntity.Type.SCOOTER }
                    .filter { it.ownerId == player.id }
                scooter.forEach {
                    it.setPosition(player.pos.add(0.0, 0.2, 0.0))
                }
            }
        })
    }

    var PlayerEntity.isAirScooting: Boolean
        get() = this.getSyncedData<Boolean>(AIR_SCOOTING_KEY) ?: false
        set(value) = this.setSyncedData(AIR_SCOOTING_KEY, value)

    fun Entity.handleDrag(): Boolean {
        return (this is PlayerEntity && this.isAirScooting) || this is AirScooterEntity
    }

    fun PlayerEntity.spawnAirScooter() {
        val world = world as? ServerWorld? ?: return
        val airScooter = EntityRegistry.AIR_SCOOTER.create(world, SpawnReason.MOB_SUMMONED) ?: return
        airScooter.bendingType = AirScooterEntity.Type.SCOOTER
        airScooter.ownerId = id
        airScooter.setPosition(this.pos)
        world.spawnEntity(airScooter)
    }

    fun Entity.handleBox(box: Box): Box {
        if (this is PlayerEntity && this.isAirScooting) {
            return box.stretch(0.0, -1.0, 0.0)
        }
        return box
    }

    fun PlayerEntity.handleFallDamage(
        f: Float,
        g: Float,
        damageSource: DamageSource,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        if (isAirScooting) {
            cir.returnValue = false
        }
    }

    fun PlayerEntity.handleTravel(vec3d: Vec3d): Vec3d {
        spawnAirScooterDust()
        val x = sidewaysSpeed * 0.5f
        val z = 1f

        this.movementSpeed = 0.5f
        return Vec3d(x.toDouble(), vec3d.y, z.toDouble())
    }

    private fun Entity.spawnAirScooterDust() {
        if (!world.isClient) return
        repeat(5) {
            val offset = 0.2
            val randomX = Random.nextDouble(-offset, offset).toFloat()
            val randomY = Random.nextDouble(-offset, offset).toFloat()
            val randomZ = Random.nextDouble(-offset, offset).toFloat()
            world.addParticle(
                ParticleRegistry.AIR_SCOOTER_DUST,
                this.x + randomX,
                this.y + randomY,
                this.z + randomZ,
                0.0,
                0.0,
                0.0
            )
        }
    }

    fun PlayerEntity.stopRidingAirBall() {
        aang.aang_airScooterTasks.forEach { it.cancel() }
        if (this is ServerPlayerEntity) {
            this.isAirScooting = false
            this.showSpeedlines = false
            //das hier suckt iwie lieber modifiers usen
            this.getAttributeInstance(EntityAttributes.STEP_HEIGHT)?.baseValue = 0.6
            this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)?.baseValue =
                0.10000000149011612
            this.getAttributeInstance(EntityAttributes.GRAVITY)?.baseValue = 0.08
            this.stopEmote(EmoteRegistry.AIR_SCOOTER_SITTING)
            this.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.2, 2f)
        } else if (this == MinecraftClient.getInstance().player) {
            this.showSpeedlines = false
        }
    }

    val Ability = object : ToggleAbility("Air Scooter") {

        init {
            client {
                this.keyBind = HeroKeyBindings.secondKeyBind
            }

            this.cooldownProperty =
                buildCooldown(90.0, 4, AddValueTotal(-20.0, -10.0, -5.0, -5.0))
            this.maxDurationProperty =
                buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

            this.properties = listOf(
                airScooterSpeed,
                airScooterStepHeight
            )

            syncedValueChangeEvent.listen {
                val player = it.entity as? PlayerEntity ?: return@listen
                if (it.key == AIR_SCOOTING_KEY) {
                    if (player.isAirScooting) {
                        player.spawnAirScooter()
                    } else {
                        if (player.world.isClient) {

                        }
                    }
                }
            }
        }

        override fun canUse(player: ServerPlayerEntity): Boolean {
            return !player.isUsingSpiritualProjection()
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
                player.playEmote(EmoteRegistry.AIR_SCOOTER)
                player.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.5)
                player.aang.aang_airScooterTasks += abilityCoroutineTask(
                    sync = true,
                    client = false,
                    delay = 0.6.seconds,
                    executingPlayer = player
                ) {
                    player.modifyVelocity(0.0, 0.55, 0.0)
                }
                player.aang.aang_airScooterTasks += abilityCoroutineTask(
                    sync = true,
                    client = false,
                    delay = 0.83.seconds,
                    executingPlayer = player
                ) {
                    //player.modifyVelocity(0.0,1.0,0.0)
                    airScooterSoundPacketS2C.sendToAll(player.id)
                    player.isAirScooting = true
                    player.getAttributeInstance(EntityAttributes.STEP_HEIGHT)?.baseValue =
                        airScooterStepHeight.getValue(player.uuid)
                    val speedAnimation =
                        OldAnimation(
                            0.1f,
                            airScooterSpeed.getValue(player.uuid).toFloat(),
                            1.seconds.toJavaDuration(),
                            Easing.CUBIC_IN
                        )
                    player.aang.aang_airScooterTasks += infiniteMcCoroutineTask(sync = true, client = false) {
                        if (speedAnimation.isDone) cancel()
                        player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)?.baseValue =
                            speedAnimation.get().toDouble()
                    }
                    player.getAttributeInstance(EntityAttributes.GRAVITY)?.baseValue = 0.02
                    player.playEmote(EmoteRegistry.AIR_SCOOTER_SITTING)
                }
            } else if (player == MinecraftClient.getInstance().player) {
                player.showSpeedlines = true
            }
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            player.stopRidingAirBall()
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            super.onEnd(player, abilityEndInformation)
            player.stopRidingAirBall()
        }
    }
}
