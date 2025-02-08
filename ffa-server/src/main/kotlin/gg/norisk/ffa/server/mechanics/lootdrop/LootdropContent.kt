package gg.norisk.ffa.server.mechanics.lootdrop

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import kotlin.random.Random

object LootdropContent {
    private val lootTable = mutableListOf<LootdropItem>()
    private var totalWeight = 0.0

    init {
        item(Items.GOLDEN_APPLE, 0.2, 2..6)
        item(Items.POTATO, 0.3, 16..32)
        item(Items.DIAMOND_SWORD, 0.1)
        item(Items.COBBLESTONE, 0.5, 32..64)
        exp(0.2, 50..100)
    }

    private fun item(itemStack: ItemStack, weight: Double, amountRange: IntRange = 1..1) {
        register(ItemStackLootdropItem(itemStack, weight, amountRange))
    }

    private fun item(item: Item, weight: Double, amountRange: IntRange = 1..1) {
        register(ItemStackLootdropItem(item.defaultStack, weight, amountRange))
    }

    private fun exp(weight: Double, amountRange: IntRange) {
        register(ExperienceLootdropItem(weight, amountRange))
    }

    private fun register(lootdropItem: LootdropItem) {
        if (lootdropItem.weight < 0) {
            throw IllegalArgumentException("weight of LootDropItem must be greater than zero")
        }

        lootTable.add(lootdropItem)
        totalWeight += lootdropItem.weight
    }

    fun generateLoot(count: Int): List<LootdropItem> {
        return Array(count) { getRandom() }.mapNotNull { it }
    }

    private fun getRandom(): LootdropItem? {
        if (lootTable.isEmpty()) return null

        var randomValue = Random.nextDouble(0.0, totalWeight)
        for (item in lootTable) {
            randomValue -= item.weight
            if (randomValue <= 0) {
                return item
            }
        }
        return null
    }
}

sealed class LootdropItem {
    abstract val weight: Double
    abstract val amountRange: IntRange
}

class ItemStackLootdropItem(
    val itemStack: ItemStack,
    override val weight: Double,
    override val amountRange: IntRange,
) : LootdropItem()

class ExperienceLootdropItem(
    override val weight: Double,
    override val amountRange: IntRange,
) : LootdropItem()
