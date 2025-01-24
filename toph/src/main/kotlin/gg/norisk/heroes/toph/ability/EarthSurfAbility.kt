package gg.norisk.heroes.toph.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.heroes.client.events.ClientEvents
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.Speedlines.showSpeedlines
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.utils.calculateProbability
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.common.utils.toBlockPos
import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import gg.norisk.heroes.toph.sound.StoneSlideSoundInstance
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.server.players
import kotlin.math.cos
import kotlin.math.sin

val EarthSurfKey = "isEarthSurfing"

@OptIn(ExperimentalSilkApi::class)
val EarthSurfAbility = object : ToggleAbility("Earth Surf") {
    init {
        client {
            this.keyBind = HeroKeyBindings.secondKeyBind
            ClientEvents.cameraClipToSpaceEvent.listen { event ->
                val player = MinecraftClient.getInstance().player ?: return@listen
                if (player.isEarthSurfing()) {
                    event.value = 8.0
                }
            }
        }

        this.cooldownProperty =
            buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
        this.maxDurationProperty =
            buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

        syncedValueChangeEvent.listen {
            val player = it.entity as? PlayerEntity ?: return@listen
            if (it.key == EarthSurfKey) {
                if (player.isEarthSurfing()) {
                    player.attributes.getCustomInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue = 6.0
                    if (player.world.isClient) {
                        MinecraftClient.getInstance().soundManager.play(StoneSlideSoundInstance(player))
                    }
                } else {
                    player.attributes.getCustomInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue = 0.6
                }
            }
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            for (player in server.players.filter { it.isEarthSurfing() }) {
                player.spawnEarthCircle()
            }
        }
    }

    val EARTH_SURF_SPEED_BOOST = EntityAttributeModifier(
        "earth_surf".toId(),
        1.3,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )

    override fun onStart(player: PlayerEntity) {
        super.onStart(player)
        if (player is ServerPlayerEntity) {
            //AnimationManagerServer.playAnimation(heroPlayer, "earth-surfing".toId())
            player.setSyncedData(EarthSurfKey, true)
            kotlin.runCatching {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    ?.addTemporaryModifier(EARTH_SURF_SPEED_BOOST)
            }
            player.sound(SoundRegistry.EARTH_ARMOR)
            cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
        } else if (player == MinecraftClient.getInstance().player) {
            player.showSpeedlines = true
        }
    }

    override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
        super.onEnd(player, abilityEndInformation)
        if (player is ServerPlayerEntity) {
            //AnimationManagerServer.resetAnimation(heroPlayer)
            player.setSyncedData(EarthSurfKey, false)
            player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue = 0.6
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                ?.removeModifier(EARTH_SURF_SPEED_BOOST.id)
        } else if (MinecraftClient.getInstance().player == player) {
            player.showSpeedlines = false
        }
    }
}

fun PlayerEntity.isEarthSurfing() = getSyncedData<Boolean>(EarthSurfKey) == true

val Entity.bodyDirectionVector: Vec3d
    get() {
        val rotY = Math.toRadians(yaw.toDouble())
        val rotX = Math.toRadians(0.0)
        val xz = cos(rotX)
        return Vec3d(-xz * sin(rotY), -sin(rotX), xz * cos(rotY))
    }

fun PlayerEntity.spawnEarthCircle() {
    val radius = 4
    this.pos.add(0.0, 0.0, 0.0).add(
        this.bodyDirectionVector.normalize().multiply(-(radius.toDouble() + 2))
    ).toBlockPos()
        .filledSpherePositionSet(radius)
        .forEach {
            val blockState = world.getBlockState(it)
            if (!blockState.isAir) {
                if (calculateProbability(5.0)) {
                    world.playSound(
                        null,
                        it,
                        SoundRegistry.STONE_SMASH,
                        SoundCategory.BLOCKS,
                        1f,
                        (0.9..1.4).random().toFloat()
                    )
                }
                if (calculateProbability(25.0)) {
                    (this as? ServerPlayerEntity?)?.apply {
                        cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), this)
                    }
                }

                val blockPos = it

                val fallingBlock = FallingBlockEntity.spawnFromBlock(world, blockPos, blockState)
                fallingBlock.modifyVelocity(Vec3d(0.0, 0.5, 0.0))
                fallingBlock.dropItem = false


                world.setBlockState(it, Blocks.AIR.defaultState)
                val particlePos = it.toCenterPos()
                (world as? ServerWorld?)?.spawnParticles(
                    ParticleRegistry.EARTH_DUST,
                    particlePos.x,
                    particlePos.y + 1,
                    particlePos.z,
                    7,
                    (0.1..0.4).random(),
                    (0.1..0.4).random(),
                    (0.1..0.4).random(),
                    (0.01..0.04).random()
                )
            }
        }
    /* this.pos.add(0.0, 1.0, 0.0).add(
         this.bodyDirectionVector.normalize().multiply(-(radius.toDouble() + 1))
     ).toBlockPos()
         .circlePositionSet(radius)
         .forEach {
             val particlePos = it.toCenterPos()
             (world as? ServerWorld?)?.spawnParticles(
                 ParticleRegistry.EARTH_DUST,
                 particlePos.x.toDouble(),
                 particlePos.y - 0.3,
                 particlePos.z.toDouble(),
                 if (radius == 1) 1 else 5,
                 (0.1..0.4).random(),
                 (0.1..0.4).random(),
                 (0.1..0.4).random(),
                 (0.01..0.04).random()
             )
         }*/
}
