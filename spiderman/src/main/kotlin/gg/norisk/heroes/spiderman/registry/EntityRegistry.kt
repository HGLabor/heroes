package gg.norisk.heroes.spiderman.registry

import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.spiderman.SpidermanManager.toId
import gg.norisk.heroes.spiderman.entity.SwingWebEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object EntityRegistry {
    val SWING_WEB = Registry.register(
        Registries.ENTITY_TYPE,
        "swing_web".toId(),
        EntityType.Builder.create(::SwingWebEntity, SpawnGroup.MISC)
            .requires(HeroesManager.heroesFlag)
            .dimensions(0.3125f, 0.3125f)
            .build(keyOf("swing_web"))
    )

    fun init() {
    }

    private fun keyOf(id: String): RegistryKey<EntityType<*>> {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, id.toId())
    }
}
