package gg.norisk.heroes.toph.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.networking.dto.AnimationInterpolator
import gg.norisk.heroes.common.utils.RaycastUtils
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.toph.TophManager.isEarthBlock
import gg.norisk.heroes.toph.entity.ITrappedEntity
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import gg.norisk.heroes.toph.render.BlockTrapFeatureRenderer
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.silkmc.silk.core.entity.posUnder
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

val EarthTrappedKey = "isEarthTrapped"

val EarthTrapAbility = object : PressAbility("Earth Trap") {
    //TODO player based
    val distance = 64.0

    val earthTrapMaxDurationProperty = CooldownProperty(
        2.0, 3,
        "Max Duration",
        AddValueTotal(1.0, 1.0, 3.0)
    )
    val earthTrapSlownessProperty = NumberProperty(
        -0.7, 3,
        "Slowness",
        AddValueTotal(-0.1, -0.2, -0.3),
    ).apply {
        icon = {
            Components.item(Items.COBWEB.defaultStack)
        }
    }

    init {
        client {
            this.keyBind = HeroKeyBindings.pickItemKeyBinding
            //TODO condition sneaking

            LivingEntityFeatureRendererRegistrationCallback.EVENT.register { entityType, entityRenderer, registrationHelper, context ->
                val modelPart = EntityModelLayers.getLayers().toList()
                    .firstOrNull { it.id.path == entityType.untranslatedName.lowercase() }
                if (modelPart != null) {
                    val model = context.getPart(modelPart)
                    registrationHelper.register(
                        BlockTrapFeatureRenderer(
                            entityRenderer as FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>,
                            context.heldItemRenderer,
                            model
                        )
                    )
                }
            }
        }

        this.properties = listOf(earthTrapMaxDurationProperty, earthTrapSlownessProperty)

        this.condition = {
            it.isSneaking && it.world.getBlockState(it.posUnder).isEarthBlock
        }

        this.cooldownProperty =
            buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))

        syncedValueChangeEvent.listen {
            if (it.key == EarthTrappedKey) {
                if (it.entity.isEarthTrapped()) {
                    (it.entity as ITrappedEntity).earthRotationAnimation = AnimationInterpolator(
                        0f,
                        360f,
                        0.8.seconds.toJavaDuration(),
                        AnimationInterpolator.Easing.CUBIC_IN
                    )
                } else {
                    (it.entity as ITrappedEntity).earthRotationAnimation = AnimationInterpolator(
                        360f,
                        0f,
                        0.8.seconds.toJavaDuration(),
                        AnimationInterpolator.Easing.CUBIC_IN
                    )
                }
            }
        }
    }

    override fun getIconComponent(): Component {
        return Components.item(Items.SAND.defaultStack)
    }

    override fun getUnlockCondition(): Text {
        return literalText {
            text(Text.translatable("heroes.ability.$internalKey.unlock_condition"))
        }
    }

    override fun hasUnlocked(player: PlayerEntity): Boolean {
        return player.isCreative || (EarthPushAbility.cooldownProperty.isMaxed(player.uuid))
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    val EARTH_TRAP_SLOW_BOOST = EntityAttributeModifier(
        Identifier.of("earth_trap"),
        -0.7,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )

    override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
        if (player is ServerPlayerEntity) {
            if (player.isSneaking && player.world.getBlockState(player.posUnder).isEarthBlock) {
                val world = player.world as ServerWorld
                val clipWithDistance = RaycastUtils.clipWithDistance(player, player.world, distance) ?: return
                cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
                player.sound(SoundRegistry.STONE_SMASH)
                world.getOtherEntities(player, Box.from(clipWithDistance.pos).expand(5.0)).forEach {
                    val entity = it
                    if (!entity.isEarthTrapped() && world.getBlockState(entity.posUnder).isEarthBlock) {
                        entity.sound(SoundRegistry.EARTH_COLUMN_1)
                        entity.setSyncedData(EarthTrappedKey, true)
                        (entity as? LivingEntity?)?.apply {
                            getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.addTemporaryModifier(
                                EntityAttributeModifier(
                                    Identifier.of("earth_trap"),
                                    earthTrapSlownessProperty.getValue(player.uuid),
                                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                )
                            )
                        }
                        //TODO player based duration
                        repeat(5) { _ ->
                            (world as? ServerWorld?)?.spawnParticles(
                                ParticleRegistry.EARTH_DUST,
                                it.x,
                                it.y,
                                it.z,
                                7,
                                (0.01..0.04).random(),
                                (0.01..0.04).random(),
                                (0.01..0.04).random(),
                                (0.01..0.04).random()
                            )
                        }
                        mcCoroutineTask(
                            sync = true,
                            client = false,
                            delay = earthTrapMaxDurationProperty.getValue(player.uuid).seconds
                        ) {
                            entity.setSyncedData(EarthTrappedKey, false)
                            (entity as? LivingEntity?)?.apply {
                                getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.removeModifier(
                                    EARTH_TRAP_SLOW_BOOST.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Entity.isEarthTrapped() = getSyncedData<Boolean>(EarthTrappedKey) == true
