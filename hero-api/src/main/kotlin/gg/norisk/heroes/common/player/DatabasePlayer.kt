package gg.norisk.heroes.common.player

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.ability.PropertyPlayer
import gg.norisk.heroes.common.serialization.ItemStackSerializer
import gg.norisk.heroes.common.serialization.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

@Serializable
data class DatabasePlayer(
    @SerialName("_id")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    var xp: Int = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var currentKillStreak: Int = 0,
    var highestKillStreak: Int = 0,
    var bounty: Int = 0,
    val heroes: MutableMap<String, MutableMap<String, MutableMap<String, PropertyPlayer>>> = mutableMapOf(),
    var inventory: DatabaseInventory? = null,
)

@Serializable
data class DatabaseInventory(
    val version: Int,
    val armor: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(4) { ItemStack.EMPTY },
    val offhand: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(1) { ItemStack.EMPTY },
    val main: Array<@Serializable(with = ItemStackSerializer::class) ItemStack> = Array(36) { ItemStack.EMPTY },
) {
    companion object {
        var CURRENT_VERSION = 0
        fun ServerPlayerEntity.toDatabaseInventory(): DatabaseInventory {
            return DatabaseInventory(
                CURRENT_VERSION,
                inventory.armor.toTypedArray(),
                inventory.offHand.toTypedArray(),
                inventory.main.toTypedArray(),
            )
        }

        fun ServerPlayerEntity.loadInventory(databaseInventory: DatabaseInventory) {
            logger.info("Loading inventory $databaseInventory")
            inventory.clear()
            databaseInventory.armor.forEachIndexed { index, itemStack ->
                inventory.armor[index] = itemStack.copy()
            }
            databaseInventory.main.forEachIndexed { index, itemStack ->
                inventory.main[index] = itemStack.copy()
            }
            databaseInventory.offhand.forEachIndexed { index, itemStack ->
                inventory.offHand[index] = itemStack.copy()
            }
        }
    }
}

private const val DATABASE_PLAYER = "HeroApi:DataBasePlayer"
var PlayerEntity.dbPlayer: DatabasePlayer
    get() = this.getSyncedData<DatabasePlayer>(DATABASE_PLAYER) ?: DatabasePlayer(this.uuid)
    set(value) = this.setSyncedData(DATABASE_PLAYER, value, (this as? ServerPlayerEntity?))

private const val FFA_BOUNTY = "HeroApi:Bounty"
var PlayerEntity.ffaBounty: Int
    get() = this.getSyncedData<Int>(FFA_BOUNTY) ?: 0
    set(value) = this.setSyncedData(FFA_BOUNTY, value)

