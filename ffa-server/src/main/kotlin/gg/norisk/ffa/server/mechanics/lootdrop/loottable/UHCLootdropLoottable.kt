package gg.norisk.ffa.server.mechanics.lootdrop.loottable

import net.minecraft.item.Items

class UHCLootdropLoottable: LootdropLoottable() {
    override fun init(): LootdropLoottable {
        item(Items.GOLDEN_APPLE, 0.2, 2..6)
        item(Items.POTATO, 0.3, 16..32)
        item(Items.DIAMOND_SWORD, 0.1)
        item(Items.COBBLESTONE, 0.5, 32..64)
        exp(0.2, 50..100)
        return this
    }
}
