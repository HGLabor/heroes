package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.katara.ability.WaterBendingAbility.getCurrentBendingEntity
import gg.norisk.heroes.katara.ability.WaterBendingAbility.waterBendingDistance
import gg.norisk.heroes.katara.client.render.HealingWaterFeatureRenderer
import gg.norisk.heroes.katara.client.sound.WaterHealingSoundInstance
import gg.norisk.heroes.katara.entity.IKataraEntity
import gg.norisk.heroes.katara.entity.WaterBendingEntity
import gg.norisk.utils.OldAnimation
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.potion.Potions
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setPotion
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText
import kotlin.time.Duration.Companion.seconds

object HealingAbility {
    fun initServer() {
        syncedValueChangeEvent.listen {
            if (it.key != WATER_HEALING_EFFECT) return@listen
            if (!it.entity.world.isClient) {

            } else {
                if (it.entity.isReceivingWaterHealing) {
                    MinecraftClient.getInstance().soundManager.play(WaterHealingSoundInstance(it.entity) { entity ->
                        entity.isReceivingWaterHealing
                    })
                }
            }
        }
        /*ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick {
            for (player in it.players) {
                val currentEntity = player.getCurrentBendingEntity()
                if (currentEntity != null) {
                    (player as IKataraEntity).katara_entityCircleTracker.update(player)
                    if (player.katara_entityCircleTracker.isDrawingCircle) {
                        (player as IKataraEntity).katara_entityCircleTracker.clear()
                        //addToCircle(currentEntity, player)
                        player.sendMessage("CIRCLE: ".literal)
                    }
                } else {
                    (player as IKataraEntity).katara_entityCircleTracker.clear()
                }
            }
        })*/
    }

    data class WaterRender(
        val pos: Vec3d, var animation: OldAnimation, val startTime: Int = 0
    )

    var counter = 0

    fun PlayerEntity.getWaterBendingPos(distance: Double = this.waterBendingDistance): Vec3d {
        return this.getLeashPos(
            if (world.isClient) {
                MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
            } else 1f
        ).add(this.directionVector.normalize().multiply(distance).add(0.0, 1.0, 0.0))
    }

    fun initClient() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(LivingEntityFeatureRendererRegistrationCallback { entityType, entityRenderer, registrationHelper, context ->
            registrationHelper.register(HealingWaterFeatureRenderer(entityRenderer as FeatureRendererContext<EntityRenderState, EntityModel<EntityRenderState>>))
        })
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register {
            val player = MinecraftClient.getInstance().player ?: return@register

            val tickDelta = it.tickCounter().getTickDelta(false)
            for (entity in it.world().entities) {
                if (entity is WaterBendingEntity) {
                    entity.tickTrail(entity.getOwner(), tickDelta)
                }
            }
        }
    }

    private const val WATER_HEALING = "WaterBending:Healing"
    private const val WATER_HEALING_EFFECT = "WaterBending:HealingEffect"

    private var PlayerEntity.isHealing: Boolean
        get() = this.getSyncedData<Boolean>(WATER_HEALING) ?: false
        set(value) = this.setSyncedData(WATER_HEALING, value)

    var Entity.isReceivingWaterHealing: Boolean
        get() = this.getSyncedData<Boolean>(WATER_HEALING_EFFECT) ?: false
        set(value) = this.setSyncedData(WATER_HEALING_EFFECT, value)

    fun Entity.handleWaterHealing(owner: PlayerEntity) {
        (this as IKataraEntity).katara_waterHealingJob?.cancel()
        isReceivingWaterHealing = true
        val duration = waterHealingMaxDuration.getValue(owner.uuid).seconds
        if (this is LivingEntity) {
            addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.REGENERATION,
                    duration.inWholeMilliseconds.toInt() / 50,
                    waterHealingRegeneration.getValue(owner.uuid).toInt(),
                    false,
                    false
                )
            )
        }
        (this as IKataraEntity).katara_waterHealingJob = mcCoroutineTask(sync = true, delay = duration) {
            isReceivingWaterHealing = false
            //  sendMessage("Stopped WaterHealing".literal)
        }
    }

    val waterHealingRegeneration = NumberProperty(
        0.0, 2,
        "Regeneration",
        AddValueTotal(1.0, 1.0)
    ).apply {
        icon = {
            Components.item(itemStack(Items.POTION) {
                setPotion(
                    MinecraftClient.getInstance().world!!.registryManager.getOrThrow(RegistryKeys.POTION)
                        .getEntry(Potions.REGENERATION.value())
                )
            })
        }
    }
    val waterHealingMaxDuration = NumberProperty(
        5.0, 4,
        "Max Duration Lasts",
        AddValueTotal(1.0, 1.0, 1.0, 1.0)
    ).apply {
        icon = {
            Components.item(itemStack(Items.CLOCK) {})
        }
    }

    val ability = object : PressAbility("Healing") {
        init {
            HeroesManager.client {
                this.keyBind = HeroKeyBindings.thirdKeyBind
            }
            this.condition = {
                it.getCurrentBendingEntity() != null
            }
            //this.usageProperty = buildMultipleUses(1.0, 3, AddValueTotal(1.0, 1.0, 1.0))
            this.cooldownProperty = buildCooldown(90.0, 4, AddValueTotal(-10.0, -10.0, -10.0, -10.0))

            this.properties = listOf(waterHealingRegeneration, waterHealingMaxDuration)
        }

        override fun getIconComponent(): Component {
            return Components.item(itemStack(Items.POTION) {
                setPotion(
                    MinecraftClient.getInstance().world!!.registryManager.getOrThrow(RegistryKeys.POTION)
                        .getEntry(Potions.REGENERATION.value())
                )
            })
        }

        override fun hasUnlocked(player: PlayerEntity): Boolean {
            return WaterBendingAbility.ability.cooldownProperty.isMaxed(player.uuid) || player.isCreative
        }

        override fun getUnlockCondition(): Text {
            return literalText {
                text(Text.translatable("heroes.ability.$internalKey.unlock_condition"))
            }
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/packed_ice.png")
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            if (player is ServerPlayerEntity) {
                val entity = player.getCurrentBendingEntity()
                if (player.isSneaking) {
                    entity?.apply {
                        player.sound(SoundEvents.ENTITY_PLAYER_HURT_FREEZE, 1f, 2f)
                        freeze()
                    }
                } else {
                    player.isHealing = true
                    entity?.apply {
                        isHealing = !isHealing
                        if (isHealing) {
                            entity.sound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 1f, 2f)
                        } else {
                            entity.sound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 1f, 0.3)
                        }
                    }
                }
            }
        }
    }
}