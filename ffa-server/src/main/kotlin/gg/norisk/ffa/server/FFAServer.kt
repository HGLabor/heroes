package gg.norisk.ffa.server

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.ffa.server.command.KillCommand
import gg.norisk.ffa.server.mechanics.Bounty
import gg.norisk.ffa.server.mechanics.CombatTag
import gg.norisk.ffa.server.mechanics.KitEditor
import gg.norisk.ffa.server.selector.SelectorServerManager
import gg.norisk.ffa.server.world.WorldManager
import gg.norisk.heroes.common.HeroesManager.isServer
import net.fabricmc.api.ModInitializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object FFAServer : ModInitializer {
    private const val MOD_ID = "ffa-server"
    val logger = LogManager.getLogger(MOD_ID)
    fun String.toId(): Identifier = Identifier.of(MOD_ID, this)

    override fun onInitialize() {
        if (!isServer) return
        SelectorServerManager.initServer()
        WorldManager.initServer()
        KillCommand.init()
        KitEditor.initServer()
        Bounty.init()
        CombatTag.init()
    }

    const val FFA_KEY = "hero-ffa"
    var PlayerEntity.isFFA: Boolean
        get() = this.getSyncedData<Boolean>(FFA_KEY) ?: false
        set(value) = this.setSyncedData(FFA_KEY, value)
}
