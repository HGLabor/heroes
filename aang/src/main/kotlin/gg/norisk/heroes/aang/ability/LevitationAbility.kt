package gg.norisk.heroes.aang.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.ext.playEmote
import gg.norisk.emote.ext.stopEmote
import gg.norisk.heroes.aang.client.sound.VelocityBasedFlyingSoundInstance
import gg.norisk.heroes.aang.registry.EmoteRegistry
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.silkmc.silk.core.event.EntityEvents

object LevitationAbility {
    val AIR_LEVITATING_KEY = "AangIsAirLevitating"

    var PlayerEntity.isAirLevitating: Boolean
        get() = this.getSyncedData<Boolean>(AIR_LEVITATING_KEY) ?: false
        set(value) = this.setSyncedData(AIR_LEVITATING_KEY, value)

    fun init() {
        syncedValueChangeEvent.listen { event ->
            if (event.key != AIR_LEVITATING_KEY) return@listen
            if (!event.entity.world.isClient) return@listen
            val player = event.entity as? AbstractClientPlayerEntity ?: return@listen
            if (player.isAirLevitating) {
                player.playEmote(EmoteRegistry.LEVITATION)
                MinecraftClient.getInstance().soundManager.play(VelocityBasedFlyingSoundInstance(player) {
                    (it as? PlayerEntity?)?.isAirLevitating == true
                })
            } else {
                player.stopEmote(EmoteRegistry.LEVITATION)
            }
        }
        EntityEvents.checkInvulnerability.listen { event ->
            if (event.source.isOf(DamageTypes.FALL)) {
                val player = event.entity as? PlayerEntity ?: return@listen
                if (player.isAirLevitating) {
                    event.isInvulnerable.set(true)
                }
            }
        }
    }

    fun PlayerEntity.handleTick() {
        if (isAirLevitating) {
            world.addParticle(
                ParticleTypes.CLOUD,
                this.getParticleX(0.5),
                this.randomBodyY,
                this.getParticleZ(0.5),
                0.001,
                0.001,
                0.001,
            )
        }
    }

    val Ability = object : HoldAbility("Levitation") {
        init {
            client {
                this.keyBind = HeroKeyBindings.fifthKeyBind
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
            this.maxDurationProperty =
                buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.FEATHER.defaultStack)
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/quartz_block_bottom.png")
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                player.isAirLevitating = true
                player.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY)?.baseValue = 0.01
            }
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            super.onEnd(player, abilityEndInformation)
            if (player is ServerPlayerEntity) {
                player.isAirLevitating = false
                player.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY)?.baseValue =
                    EntityAttributes.GENERIC_GRAVITY.value().defaultValue
            }
        }
    }
}
