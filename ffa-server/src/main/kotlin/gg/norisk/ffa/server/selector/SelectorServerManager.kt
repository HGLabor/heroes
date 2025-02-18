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
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.setHero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.heroes.common.player.InventorySorting.Companion.loadInventory
import gg.norisk.heroes.common.player.ffaPlayer
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.minecraft.world.World
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.item.itemStack
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
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("ffakit") {
                literal("uhc") {
                    runs {
                        KitEditor.handleUHCKit(this.source.playerOrThrow)
                    }
                }
                literal("soup") {
                    runs {
                        KitEditor.handleSoupKit(this.source.playerOrThrow)
                    }
                }
            }
        }
    }

    private fun ServerPlayerEntity.setArenaReady() {
        val inventory = this.ffaPlayer.inventorySorting
        if (inventory != null) {
            this.loadInventory(inventory)
        } else {
            KitEditor.handleKit(this)
        }
        if (!KitEditor.isUHC()) {
            //setSyncedData("duels:OLD_PVP", true)
            getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)?.baseValue = 100.0
        }
        hungerManager.foodLevel = 20
        hungerManager.saturationLevel = 5f
        clearStatusEffects()
        closeHandledScreen()
        setExperienceLevel(0)
        setExperiencePoints(0)
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            scoreboards.computeIfAbsent(this.uuid) { Scoreboard.getScoreboardForPlayer(this) }.displayToPlayer(this)
        }
        (this as LivingEntityAccessor).lastAttackTime = -10000
    }

    fun PlayerEntity.setSoupItems() {
        inventory.setStack(0, Items.STONE_SWORD.defaultStack)
        repeat(36) {
            giveItemStack(Items.MUSHROOM_STEW.defaultStack)
        }
        inventory.setStack(8, Tracker.tracker)
        inventory.setStack(13, ItemStack(Items.BOWL, 16))
        inventory.setStack(14, ItemStack(Items.RED_MUSHROOM, 16))
        inventory.setStack(15, ItemStack(Items.BROWN_MUSHROOM, 16))
    }

    fun PlayerEntity.setUHCItems() {
        equipStack(EquipmentSlot.HEAD, itemStack(Items.DIAMOND_HELMET) {
            //addEnchantment(Enchantments.PROTECTION.getEntry(world), 1)
        })
        equipStack(EquipmentSlot.CHEST, itemStack(Items.DIAMOND_CHESTPLATE) {
            //addEnchantment(Enchantments.PROTECTION.getEntry(world), 1)
        })
        equipStack(EquipmentSlot.LEGS, itemStack(Items.DIAMOND_LEGGINGS) {
            //addEnchantment(Enchantments.PROTECTION.getEntry(world), 1)
        })
        equipStack(EquipmentSlot.FEET, itemStack(Items.DIAMOND_BOOTS) {
            //addEnchantment(Enchantments.PROTECTION.getEntry(world), 1)
        })
        equipStack(EquipmentSlot.OFFHAND, itemStack(Items.SHIELD) {
            //addEnchantment(Enchantments.PROTECTION.getEntry(world), 1)
        })
        inventory.setStack(0, itemStack(Items.DIAMOND_SWORD) {
            addEnchantment(Enchantments.SHARPNESS.getEntry(world), 1)
        })
        inventory.setStack(1, itemStack(Items.DIAMOND_AXE) {
            addEnchantment(Enchantments.UNBREAKING.getEntry(world), 3)
        })
        inventory.setStack(2, itemStack(Items.GOLDEN_APPLE, 6) {
        })
        inventory.setStack(3, itemStack(Items.WATER_BUCKET) {
        })
        inventory.setStack(29, itemStack(Items.COOKED_BEEF, 16) {
        })
        inventory.setStack(30, itemStack(Items.WATER_BUCKET) {
        })
        inventory.setStack(4, itemStack(Items.LAVA_BUCKET) {
        })
        inventory.setStack(31, itemStack(Items.LAVA_BUCKET) {
        })
        inventory.setStack(5, itemStack(Items.COBBLESTONE, 64) {
        })
        inventory.setStack(32, itemStack(Items.OAK_PLANKS, 64) {
        })
        inventory.setStack(6, itemStack(Items.COBWEB, 8) {
        })
        inventory.setStack(7, itemStack(Items.BOW) {
            addEnchantment(Enchantments.UNBREAKING.getEntry(world), 3)
            addEnchantment(Enchantments.POWER.getEntry(world), 1)
        })
        inventory.setStack(8, itemStack(Items.CROSSBOW) {
            addEnchantment(Enchantments.UNBREAKING.getEntry(world), 3)
            addEnchantment(Enchantments.PIERCING.getEntry(world), 1)
        })
        inventory.setStack(17, Tracker.tracker)
        inventory.setStack(14, itemStack(Items.DIAMOND_PICKAXE) {
            addEnchantment(Enchantments.UNBREAKING.getEntry(world), 3)
            addEnchantment(Enchantments.EFFICIENCY.getEntry(world), 1)
        })
        inventory.setStack(9, itemStack(Items.ARROW, 16) {
        })
    }

    private fun RegistryKey<Enchantment>.getEntry(world: World): RegistryEntry<Enchantment> {
        return world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(this.value).get()
    }

    fun ServerPlayerEntity.setSelectorReady() {
        this.health = this.maxHealth
        isFFA = false
        changeGameMode(GameMode.SPECTATOR)
        closeHandledScreen()
        clearStatusEffects()
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
