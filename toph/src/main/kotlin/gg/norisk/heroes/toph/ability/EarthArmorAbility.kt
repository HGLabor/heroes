package gg.norisk.heroes.toph.ability

import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.utils.pos3i
import gg.norisk.heroes.toph.TophManager.toEmote
import gg.norisk.heroes.toph.TophManager.toId
import gg.norisk.heroes.toph.entity.IBendingItemEntity
import gg.norisk.heroes.toph.registry.SoundRegistry
import gg.norisk.heroes.toph.render.ChestItemFeatureRenderer
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particle.BlockStateParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.entity.touchedBlockNoAir
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.task.mcCoroutineTask
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import kotlin.random.Random

object EarthArmorAttributeModifiers {

    val earthArmorArmorProperty = NumberProperty(
        2.0, 3,
        "Armor",
        AddValueTotal(1.0, 1.0, 1.0), {
            Components.item(Items.IRON_CHESTPLATE.defaultStack)
        }
    )
    val earthArmorKnockbackProperty = NumberProperty(
        0.05, 3,
        "Knockback",
        AddValueTotal(-0.01, -0.02, -0.03), {
            Components.item(Items.ANVIL.defaultStack)
        }
    )
    val earthArmorSpeedProperty = NumberProperty(
        -0.005, 3,
        "Speed",
        AddValueTotal(0.002, 0.002, 0.002), {
            Components.item(Items.SUGAR.defaultStack)
        }
    )

    fun addTo(stack: ItemStack, player: PlayerEntity) {
        val ARMOR_ENTRY = AttributeModifiersComponent.Entry(
            EntityAttributes.GENERIC_ARMOR,
            EntityAttributeModifier(
                "armor_modifier".toId(),
                earthArmorArmorProperty.getValue(player.uuid),
                EntityAttributeModifier.Operation.ADD_VALUE
            ),
            AttributeModifierSlot.ARMOR
        )

        val KNOCKBACK_ENTRY = AttributeModifiersComponent.Entry(
            EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
            EntityAttributeModifier(
                "knockback_modifier".toId(),
                earthArmorKnockbackProperty.getValue(player.uuid),
                EntityAttributeModifier.Operation.ADD_VALUE
            ),
            AttributeModifierSlot.ARMOR
        )

        val SPEED_ENTRY = AttributeModifiersComponent.Entry(
            EntityAttributes.GENERIC_MOVEMENT_SPEED,
            EntityAttributeModifier(
                "speed_modifier".toId(),
                earthArmorSpeedProperty.getValue(player.uuid),
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ),
            AttributeModifierSlot.ARMOR
        )

        val componentFromStack =
            stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS) ?: AttributeModifiersComponent.DEFAULT
        val newList: MutableList<AttributeModifiersComponent.Entry> = ArrayList()
        if (componentFromStack != null) {
            newList.addAll(componentFromStack.modifiers)
            newList.add(ARMOR_ENTRY)
            newList.add(KNOCKBACK_ENTRY)
            newList.add(SPEED_ENTRY)
        }

        stack.set(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            AttributeModifiersComponent(newList, true)
        )
    }

    val EarthArmorAbility = object : PressAbility("Earth Armor") {
        init {
            client {
                this.keyBind = HeroKeyBindings.firstKeyBind

                LivingEntityFeatureRendererRegistrationCallback.EVENT.register { entityType, entityRenderer, registrationHelper, context ->
                    if (entityType !== EntityType.PLAYER) return@register
                    registrationHelper.register(
                        ChestItemFeatureRenderer(
                            entityRenderer as FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>,
                            context.heldItemRenderer
                        )
                    )
                }
            }

            this.properties = listOf(earthArmorArmorProperty, earthArmorSpeedProperty, earthArmorKnockbackProperty)

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.IRON_CHESTPLATE.defaultStack)
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/packed_mud.png")
        }

        override fun onStart(player: PlayerEntity) {
            super.onStart(player)
            if (player is ServerPlayerEntity) {
                player.swingHand(Hand.MAIN_HAND, true)
                val world = player.world as ServerWorld
                player.playEmote("earth-armor".toEmote())
                (player as ServerPlayerEntity).spawnEarthArmorParticle()
                world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.EARTH_ARMOR,
                    SoundCategory.PLAYERS,
                    1f,
                    1f
                )
                cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player)

                player.pos3i.filledSpherePositionSet(4)
                    .filter {
                        world.getBlockState(it).isIn(BlockTags.PICKAXE_MINEABLE) || world.getBlockState(it)
                            .isIn(BlockTags.SHOVEL_MINEABLE)
                    }
                    .filter { pos -> Direction.values().any { world.getBlockState(pos.offset(it)).isAir } }
                    .shuffled()
                    .take(4)
                    .forEachIndexed { index, blockPos ->
                        mcCoroutineTask(client = false, sync = true, delay = index.ticks) {
                            val center = blockPos.toCenterPos()
                            val state = world.getBlockState(blockPos)
                            val itemStack = state.block.asItem().defaultStack
                            val itemEntity =
                                ItemEntity(world, center.x, center.y, center.z, itemStack)
                            (itemEntity as IBendingItemEntity).bender = player.uuid
                            world.spawnEntity(itemEntity)
                            world.breakBlock(blockPos, false, player)
                        }
                    }
            }
        }
    }
}

fun ItemEntity.handlePlayerCollision(player: PlayerEntity, ci: CallbackInfo) {
    val dummy = this as IBendingItemEntity
    if (world.isClient) return
    if (bender == player.uuid) {
        ci.cancel()
        discard()

        val blockItem = this.stack.item as? BlockItem ?: return
        world.playSoundFromEntity(
            null,
            player,
            blockItem.block.defaultState.soundGroup.placeSound,
            SoundCategory.PLAYERS,
            0.7f,
            1f
        )

        val slot = EquipmentSlot.entries
            .filter { it.isArmorSlot }
            .firstOrNull { player.getEquippedStack(it).isEmpty }
            ?: return

        EarthArmorAttributeModifiers.addTo(stack, player)
        player.equipStack(slot, this.stack)
    }
}

fun ServerPlayerEntity.spawnEarthArmorParticle() {
    val block = this.touchedBlockNoAir ?: return
    repeat(20) {
        (world as ServerWorld).spawnParticles(
            BlockStateParticleEffect(ParticleTypes.BLOCK, block.block.defaultState),
            this.x,
            getBodyY(Random.nextDouble(0.1, 0.5)),
            this.z,
            7,
            (this.width / 4.0f).toDouble(),
            (this.height / 4.0f).toDouble(),
            (this.width / 4.0f).toDouble(),
            0.05
        )
    }
}

fun ItemEntity.moveToBender() {
    val dummy = this as IBendingItemEntity
    if (world.isClient) return
    val block = this.stack.item as? BlockItem ?: return
    if (bender != null) {
        val player = world.getPlayerByUuid(bender)
        if (player == null) {
            discard()
            return
        }
        val direction = player.eyePos.subtract(this.pos).normalize().multiply(0.5)
        modifyVelocity(direction)

        (world as ServerWorld).spawnParticles(
            BlockStateParticleEffect(ParticleTypes.BLOCK, block.block.defaultState),
            this.x,
            getBodyY(0.6666666666666666),
            this.z,
            7,
            (this.width / 4.0f).toDouble(),
            (this.height / 4.0f).toDouble(),
            (this.width / 4.0f).toDouble(),
            0.05
        )
    }
}
