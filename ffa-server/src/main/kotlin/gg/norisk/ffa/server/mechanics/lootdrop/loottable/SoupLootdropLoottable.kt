package gg.norisk.ffa.server.mechanics.lootdrop.loottable

import net.minecraft.item.Items

class SoupLootdropLoottable: LootdropLoottable() {
    override fun init(): SoupLootdropLoottable {
        item(Items.DIAMOND_HELMET, 1.0)
        item(Items.DIAMOND_CHESTPLATE, 1.0)
        item(Items.DIAMOND_LEGGINGS, 1.0)
        item(Items.DIAMOND_BOOTS, 1.0)
        item(Items.DIAMOND_SWORD, 1.1)
        item(Items.IRON_HELMET, 1.75)
        item(Items.IRON_CHESTPLATE, 1.75)
        item(Items.IRON_LEGGINGS, 1.75)
        item(Items.IRON_BOOTS, 1.75)
        item(Items.IRON_SWORD, 2.0)
        item(Items.COBWEB, 2.0, 1..6)
        item(Items.ENDER_PEARL, 1.5, 1..6)
        item(Items.WATER_BUCKET, 1.25)
        item(Items.LAVA_BUCKET, 1.25)
        item(Items.MUSHROOM_STEW, 2.0, 1..4)
        item(Items.RED_MUSHROOM, 2.0, 6..14)
        item(Items.BROWN_MUSHROOM, 2.0, 6..14)
        item(Items.EXPERIENCE_BOTTLE, 2.0, 1..3)
        item(Items.LAPIS_LAZULI, 2.0, 1..12)
        item(Items.ANVIL, 0.6)
        item(Items.ENCHANTING_TABLE, 0.8)
        exp(1.0, 50..100)
        return this
    }
}
