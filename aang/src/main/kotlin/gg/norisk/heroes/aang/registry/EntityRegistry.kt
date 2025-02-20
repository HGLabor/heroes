package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.entity.TornadoEntity
import gg.norisk.heroes.common.HeroesManager
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object EntityRegistry {
    val AIR_SCOOTER = Registry.register(
        Registries.ENTITY_TYPE,
        "air_scooter".toId(),
        EntityType.Builder.create(::AirScooterEntity, SpawnGroup.MISC)
            .requires(HeroesManager.heroesFlag)
            .dimensions(0.3125f, 0.3125f)
            .build(keyOf("air_scooter"))
    )
    val TORNADO = Registry.register(
        Registries.ENTITY_TYPE,
        "tornado".toId(),
        EntityType.Builder.create(::TornadoEntity, SpawnGroup.MISC)
            .requires(HeroesManager.heroesFlag)
            .dimensions(0.6f, 1f)
            .build(keyOf("tornado"))
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
            .add(EntityAttributes.MOVEMENT_SPEED, 0.80000000298023224)
            .add(EntityAttributes.FOLLOW_RANGE, 16.0).add(EntityAttributes.MAX_HEALTH, 10.0)
            .add(EntityAttributes.ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.ATTACK_KNOCKBACK, 0.1)
    }

    private fun keyOf(id: String): RegistryKey<EntityType<*>> {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, id.toId())
    }
}
