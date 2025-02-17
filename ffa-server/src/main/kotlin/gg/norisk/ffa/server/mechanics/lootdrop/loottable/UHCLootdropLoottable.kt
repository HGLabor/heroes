package gg.norisk.ffa.server.mechanics.lootdrop.loottable

import gg.norisk.ffa.server.utils.EnchantmentUtils.getEntry
import gg.norisk.ffa.server.world.WorldManager
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Items
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.item.itemStack

class UHCLootdropLoottable : LootdropLoottable() {
    override fun init(): LootdropLoottable {
        item(Items.NETHERITE_HELMET, 1.0)
        item(Items.NETHERITE_CHESTPLATE, 1.0)
        item(Items.NETHERITE_LEGGINGS, 1.0)
        item(Items.NETHERITE_BOOTS, 1.0)
        item(Items.NETHERITE_SWORD, 1.1)
        item(Items.DIAMOND_HELMET, 0.45)
        item(Items.DIAMOND_CHESTPLATE, 0.45)
        item(Items.DIAMOND_LEGGINGS, 0.45)
        item(Items.DIAMOND_BOOTS, 0.45)
        item(Items.DIAMOND_SWORD, 0.33)
        item(Items.COBWEB, 2.0, 3..8)
        item(Items.ENDER_PEARL, 2.0, 1..6)
        item(Items.WATER_BUCKET, 1.3)
        item(Items.LAVA_BUCKET, 1.3)
        item(Items.EXPERIENCE_BOTTLE, 1.5, 1..5)
        item(Items.LAPIS_LAZULI, 2.0, 1..12)
        item(Items.ANVIL, 0.3)
        item(Items.ENCHANTING_TABLE, 0.8)
        item(Items.ARROW, 1.4, 8..24)
        item(Items.OAK_LOG, 1.7, 32..64)
        item(Items.COBBLESTONE, 1.3, 64..128)

        item(itemStack(Items.IRON_SWORD) {
            addEnchantment(Enchantments.FIRE_ASPECT.getEntry(WorldManager.getCurrentWorld()), 1)
        }, 0.3)

        exp(1.0, 50..100)
        return this
    }
}
