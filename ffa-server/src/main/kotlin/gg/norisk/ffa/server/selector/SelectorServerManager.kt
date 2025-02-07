package gg.norisk.ffa.server.selector

import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.ffa.server.mechanics.KitEditor
import gg.norisk.ffa.server.mechanics.Scoreboard
import gg.norisk.ffa.server.mechanics.Tracker
import gg.norisk.ffa.server.mixin.accessor.LivingEntityAccessor
import gg.norisk.ffa.server.world.WorldManager.findSpawnLocation
import gg.norisk.ffa.server.world.WorldManager.getCenter
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.db.DatabaseInventory.Companion.loadInventory
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.setHero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.silkmc.silk.game.sideboard.Sideboard
import java.util.*

object SelectorServerManager {
    private val scoreboards = mutableMapOf<UUID, Sideboard>()

    fun initServer() {
        HeroEvents.heroSelectEvent.listen { event ->
            event.canSelect = true
            val player = event.player as? ServerPlayerEntity ?: return@listen
            val server = player.server
            player.changeGameMode(GameMode.SURVIVAL)
            player.isFFA = true
            val spawn = server.overworld.findSpawnLocation().toCenterPos()
            player.teleport(server.overworld, spawn.x, spawn.y, spawn.z, 0f, 0f)
            player.setArenaReady()
        }
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, sender, server ->
            handler.player.setSelectorReady()
        })
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, player ->
            logger.info("REMOVING SCOREBOARD FOR ${handler.player}")
            scoreboards.remove(handler.player.uuid)
        })
    }

    private fun ServerPlayerEntity.setArenaReady() {
        setSyncedData("duels:OLD_PVP", true)
        getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)?.baseValue = 100.0
        val inventory = this.dbPlayer.inventory
        if (inventory != null) {
            this.loadInventory(inventory)
        } else {
            KitEditor.handleKit(this)
        }
        scoreboards.computeIfAbsent(this.uuid) { Scoreboard.getScoreboardForPlayer(this) }.displayToPlayer(this)
        (this as LivingEntityAccessor).lastAttackTime = -10000
    }

    fun PlayerEntity.setSoupItems() {
        inventory.setStack(0, Items.STONE_SWORD.defaultStack)
        repeat(36) {
            giveItemStack(Items.MUSHROOM_STEW.defaultStack)
        }
        inventory.setStack(8, Tracker.tracker)
        inventory.setStack(13, ItemStack(Items.BOWL, 32))
        inventory.setStack(14, ItemStack(Items.RED_MUSHROOM, 32))
        inventory.setStack(15, ItemStack(Items.BROWN_MUSHROOM, 32))
    }

    fun PlayerEntity.setUHCItems() {
        inventory.setStack(0, Items.STONE_SWORD.defaultStack)
        inventory.setStack(8, Tracker.tracker)
        inventory.setStack(13, ItemStack(Items.BOWL, 32))
        inventory.setStack(14, ItemStack(Items.RED_MUSHROOM, 32))
        inventory.setStack(15, ItemStack(Items.BROWN_MUSHROOM, 32))
    }

    fun ServerPlayerEntity.setSelectorReady() {
        this.health = this.maxHealth
        isFFA = false
        changeGameMode(GameMode.SPECTATOR)
        setHero(null)
        val packet = HeroSelectorPacket(HeroManager.registeredHeroes.keys.toList(), true, true)
        Networking.s2cHeroSelectorPacket.send(
            packet,
            this,
        )
        val spawn = server.overworld.getCenter().toCenterPos()
        this.teleport(server.overworld, spawn.x, spawn.y, spawn.z, 0f, 0f)
        scoreboards[uuid]?.hideFromPlayer(this)
    }
}
