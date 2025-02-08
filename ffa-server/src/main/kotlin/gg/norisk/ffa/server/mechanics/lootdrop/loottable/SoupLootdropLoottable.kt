package gg.norisk.ffa.server.mechanics.lootdrop.loottable

import net.minecraft.item.Items

class SoupLootdropLoottable: LootdropLoottable() {
    override fun init(): SoupLootdropLoottable {
        item(Items.MUSHROOM_STEW, 0.2, 1..4)
        item(Items.POTATO, 0.3, 16..32)
        item(Items.RED_MUSHROOM, 0.2, 6..18)
        item(Items.BROWN_MUSHROOM, 0.2, 6..18)
        exp(0.04, 50..100)
        return this
    }
}
