package gg.norisk.heroes.toph.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.emote.network.EmoteNetworking.stopEmote
import gg.norisk.heroes.client.events.ClientEvents
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.Speedlines.showSpeedlines
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.ToggleAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.utils.calculateProbability
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.common.utils.toBlockPos
import gg.norisk.heroes.toph.TophManager.toEmote
import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import gg.norisk.heroes.toph.sound.StoneSlideSoundInstance
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.server.players
import kotlin.math.cos
import kotlin.math.sin

val EarthSurfKey = "isEarthSurfing"

val earthSurfRadius = NumberProperty(1.0, 3, "Radius", AddValueTotal(1.0, 1.0, 1.0)).apply {
    icon = {
        Components.item(Items.STONE_SHOVEL.defaultStack)
    }
}

@OptIn(ExperimentalSilkApi::class)
val EarthSurfAbility = object : ToggleAbility("Earth Surf") {

    val earthSurfStepHeight = NumberProperty(3.0, 3, "Step Height", AddValueTotal(1.0, 1.0, 1.0)).apply {
        icon = {
            Components.item(Items.MUD_BRICK_STAIRS.defaultStack)
        }
    }
    val earthSurfSpeedBoost = NumberProperty(1.1, 4, "Speed", AddValueTotal(0.1, 0.1, 0.1, 0.3)).apply {
        icon = {
            Components.item(Items.SUGAR.defaultStack)
        }
    }

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

        this.properties = listOf(earthSurfStepHeight, earthSurfSpeedBoost, earthSurfRadius)

        this.cooldownProperty =
            buildCooldown(30.0, 4, AddValueTotal(-4.0, -4.0, -4.0, -2.0))
        this.maxDurationProperty =
            buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

        syncedValueChangeEvent.listen {
            val player = it.entity as? PlayerEntity ?: return@listen
            if (it.key == EarthSurfKey) {
                if (player.isEarthSurfing()) {
                    player.attributes.getCustomInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue =
                        earthSurfStepHeight.getValue(player.uuid)
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

    override fun getIconComponent(): Component {
        return Components.item(itemStack(Items.IRON_BOOTS) {})
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    val EARTH_SURF_SPEED_BOOST = EntityAttributeModifier(
        "earth_surf".toId(),
        1.3,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )

    override fun onDisable(player: PlayerEntity) {
        super.onDisable(player)
        cleanUp(player)
    }

    private fun cleanUp(player: PlayerEntity) {
        if (player is ServerPlayerEntity) {
            player.stopEmote("earth-surfing".toEmote())
            player.setSyncedData(EarthSurfKey, false)
            player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue = 0.6
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.removeModifier(EARTH_SURF_SPEED_BOOST.id)
        } else if (MinecraftClient.getInstance().player == player) {
            player.showSpeedlines = false
        }
    }

    override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
        super.onStart(player, abilityScope)
        if (player is ServerPlayerEntity) {
            player.playEmote("earth-surfing".toEmote())
            player.setSyncedData(EarthSurfKey, true)
            runCatching {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    ?.addTemporaryModifier(
                        EntityAttributeModifier(
                            "earth_surf".toId(),
                            earthSurfSpeedBoost.getValue(player.uuid),
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )
                    )
            }
            player.sound(SoundRegistry.EARTH_ARMOR)
            cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
        } else if (player == MinecraftClient.getInstance().player) {
            player.showSpeedlines = true
        }
    }

    override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
        super.onEnd(player, abilityEndInformation)
        cleanUp(player)
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
    val radius = earthSurfRadius.getValue(uuid).toInt()
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
