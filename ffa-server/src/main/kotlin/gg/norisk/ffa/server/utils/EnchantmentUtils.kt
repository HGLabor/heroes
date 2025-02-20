package gg.norisk.ffa.server.utils

import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.world.World

object EnchantmentUtils {
    fun RegistryKey<Enchantment>.getEntry(world: World): RegistryEntry<Enchantment> {
        return world.registryManager.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(this.value).get()
    }
}
