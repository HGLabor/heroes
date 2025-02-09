package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.ability.WaterBendingAbility.getCurrentBendingEntity
import gg.norisk.heroes.katara.entity.IceShardEntity
import gg.norisk.heroes.katara.registry.EntityRegistry
import gg.norisk.heroes.katara.registry.SoundRegistry
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.directionVector
import kotlin.random.Random

object IceShardAbility {

    private const val ICE_SHARDING = "WaterBending:ICE_SHARDING"
    var PlayerEntity.isIceShooting: Boolean
        get() = this.getSyncedData<Boolean>(ICE_SHARDING) ?: false
        set(value) = this.setSyncedData(ICE_SHARDING, value)

    val ICE_SHARD_SLOW_BOOST = EntityAttributeModifier(
        "ice_shard".toId(),
        -0.3,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    )

    val ability = object : HoldAbility("Ice Shards") {

        init {
            HeroesManager.client {
                this.keyBind = HeroKeyBindings.fifthKeyBind
            }
            this.condition = {
                it.getCurrentBendingEntity() != null
            }

            this.cooldownProperty =
                buildCooldown(24.0, 4, AddValueTotal(-2.0, -2.0, -2.0, -2.0))
            this.maxDurationProperty =
                buildMaxDuration(1.0, 4, AddValueTotal(0.5, 0.5, 0.5, 0.5))
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.ARROW.defaultStack)
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/packed_ice.png")
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                player.isIceShooting = true
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    ?.addTemporaryModifier(ICE_SHARD_SLOW_BOOST)
            }
        }

        override fun onTick(player: PlayerEntity) {
            super.onTick(player)
            if (player.isIceShooting) {
                launchProjectiles(player.world, player)
            }
        }

        fun cleanUp(player: PlayerEntity) {
            if (player is ServerPlayerEntity) {
                player.isIceShooting = false
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    ?.removeModifier(ICE_SHARD_SLOW_BOOST.id)
            }
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            cleanUp(player)
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            super.onEnd(player, abilityEndInformation)
            cleanUp(player)
        }

        private fun launchProjectiles(world: World, user: PlayerEntity) {
            if (!world.isClient) {
                val entity = user.getCurrentBendingEntity()
                entity?.discard()
                val iceShard = IceShardEntity(EntityRegistry.ICE_SHARD, world)
                val pos = user.eyePos.add(0.0, -0.1, 0.0).add(user.directionVector.normalize().multiply(1.0))
                val offset = 1.0
                val randomX = Random.nextDouble(-offset, offset)
                val randomY = Random.nextDouble(-offset, offset)
                val randomZ = Random.nextDouble(-offset, offset)
                val spawnPos = Vec3d(pos.x + randomX, pos.y + randomY, pos.z + randomZ)
                iceShard.setPosition(spawnPos.x, spawnPos.y, spawnPos.z)
                //snowballEntity.setItem(Items.SNOWBALL.defaultStack)
                iceShard.setVelocity(user, user.pitch, user.yaw, 0.0f, 5f, 0.0f)
                world.playSound(
                    null,
                    spawnPos.getX(),
                    spawnPos.getY(),
                    spawnPos.getZ(),
                    SoundRegistry.ICE_PLACE,
                    SoundCategory.PLAYERS,
                    0.75f,
                    Random.nextDouble(1.5, 2.0).toFloat()
                )
                world.spawnEntity(iceShard)
            }
        }
    }
}