package gg.norisk.heroes.katara.registry

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.entity.IceShardEntity
import gg.norisk.heroes.katara.entity.WaterBendingEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object EntityRegistry {
    val WATER_BENDING: EntityType<WaterBendingEntity> = register("water_bending", { entityType, world ->
        WaterBendingEntity(entityType, world)
    }, 0.3125f, 0.3125f)
    val ICE_SHARD: EntityType<IceShardEntity> = register("ice_shard", { entityType, world ->
        IceShardEntity(entityType, world)
    }, 0.3125f, 0.3125f)

    fun init() {
        registerEntityAttributes()
    }

    private fun registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(WATER_BENDING, createGenericEntityAttributes())
    }

    fun createGenericEntityAttributes(): DefaultAttributeContainer.Builder {
        return PathAwareEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.80000000298023224)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.1)
    }

    private fun <T : Entity> register(
        name: String, entity: EntityType.EntityFactory<T>, width: Float, height: Float
    ): EntityType<T> {
        val dimension = EntityDimensions.changing(width, height).withEyeHeight(0f)
        val builder = EntityType.Builder.create(entity, SpawnGroup.CREATURE)
        return Registry.register(
            Registries.ENTITY_TYPE,
            name.toId(),
            builder.eyeHeight(0f)
                .dimensions(dimension.width, dimension.height)
                .apply {
                    requires(HeroesManager.heroesFlag)
                }
                .build(null)
        )
    }
}
