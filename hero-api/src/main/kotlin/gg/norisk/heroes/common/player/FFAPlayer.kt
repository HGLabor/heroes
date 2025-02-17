package gg.norisk.heroes.common.player

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.ability.PropertyPlayer
import gg.norisk.heroes.common.serialization.ItemStackSerializer
import gg.norisk.heroes.common.serialization.UUIDSerializer
import gg.norisk.heroes.common.utils.PlayStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

@Serializable
data class FFAPlayer(
    @SerialName("_id")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    var xp: Int = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var currentKillStreak: Int = 0,
    var highestKillStreak: Int = 0,
    var bounty: Int = 0,
    var heroes: MutableMap<String, MutableMap<String, MutableMap<String, PropertyPlayer>>> = mutableMapOf(),
    var inventory: InventorySorting? = null,
)

@Serializable
data class InventorySorting(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val playStyle: PlayStyle,
    val version: Int,
    val armor: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(4) { ItemStack.EMPTY },
    val offhand: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(1) { ItemStack.EMPTY },
    val main: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(36) { ItemStack.EMPTY },
) {
    companion object {
        var CURRENT_VERSION = 0

        fun ServerPlayerEntity.loadInventory(inventorySorting: InventorySorting) {
            logger.info("Loading inventory $inventorySorting")
            inventory.clear()
            inventorySorting.armor.forEachIndexed { index, itemStack ->
                inventory.armor[index] = itemStack.copy()
            }
            inventorySorting.main.forEachIndexed { index, itemStack ->
                inventory.main[index] = itemStack.copy()
            }
            inventorySorting.offhand.forEachIndexed { index, itemStack ->
                inventory.offHand[index] = itemStack.copy()
            }
        }
    }
}

private const val FFA_PLAYER = "HeroApi:FfaPlayer"
var PlayerEntity.ffaPlayer: FFAPlayer
    get() = this.getSyncedData<FFAPlayer>(FFA_PLAYER) ?: FFAPlayer(this.uuid)
    set(value) = this.setSyncedData(FFA_PLAYER, value, (this as? ServerPlayerEntity?))

private const val FFA_BOUNTY = "HeroApi:Bounty"
var PlayerEntity.ffaBounty: Int
    get() = this.getSyncedData<Int>(FFA_BOUNTY) ?: 0
    set(value) = this.setSyncedData(FFA_BOUNTY, value)

