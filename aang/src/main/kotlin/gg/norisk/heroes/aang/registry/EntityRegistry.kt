package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.entity.TornadoEntity
import gg.norisk.heroes.common.HeroesManager
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
    val AIR_SCOOTER = Registry.register(
        Registries.ENTITY_TYPE,
        "air_scooter".toId(),
        EntityType.Builder.create(::AirScooterEntity, SpawnGroup.MISC)
            .requires(HeroesManager.heroesFlag)
            .dimensions(0.3125f, 0.3125f)
            .build(null)
    )
    val TORNADO = Registry.register(
        Registries.ENTITY_TYPE,
        "tornado".toId(),
        EntityType.Builder.create(::TornadoEntity, SpawnGroup.MISC)
            .requires(HeroesManager.heroesFlag)
            .dimensions(0.6f, 1f)
            .build(null)
    )

    fun init() {
        registerEntityAttributes()
    }

    private fun registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(AIR_SCOOTER, createGenericEntityAttributes())
        FabricDefaultAttributeRegistry.register(TORNADO, createGenericEntityAttributes())
    }

    fun createGenericEntityAttributes(): DefaultAttributeContainer.Builder {
        return PathAwareEntity.createLivingAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.80000000298023224)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.1)
    }

    private fun <T : Entity> register(
        name: String, entity: EntityType.EntityFactory<T>,
        width: Float, height: Float
    ): EntityType<T> {
        val dimension = EntityDimensions.changing(width, height).withEyeHeight(0f)
        val builder = EntityType.Builder.create(entity, SpawnGroup.CREATURE)
        return Registry.register(
            Registries.ENTITY_TYPE,
            name.toId(),
            builder.eyeHeight(0f).dimensions(dimension.width, dimension.height).requires(HeroesManager.heroesFlag)
                .build(null)
        )
    }
}
