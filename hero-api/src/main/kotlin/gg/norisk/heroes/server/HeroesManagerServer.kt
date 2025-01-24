package gg.norisk.heroes.server

import gg.norisk.heroes.common.HeroesManager.MOD_ID
import gg.norisk.heroes.common.HeroesManager.logger
import gg.norisk.heroes.common.HeroesManager.toId
import gg.norisk.heroes.common.command.DebugCommand
import gg.norisk.heroes.common.db.DatabaseManager
import gg.norisk.heroes.common.db.ExperienceManager
import gg.norisk.heroes.common.events.HeroEvents
import gg.norisk.heroes.common.ffa.KitEditorManager
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.hero.setHero
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.heroes.server.config.ConfigManagerServer
import gg.norisk.heroes.server.hero.ability.AbilityManagerServer
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.silkmc.silk.core.task.mcCoroutineTask

object HeroesManagerServer {
    fun initServer() {
        logger.info("Init Hero server...")

        ConfigManagerServer.init()
        AbilityManagerServer.init()
        DebugCommand.initServer()
        DatabaseManager.init()
        ExperienceManager.init()
        KitEditorManager.init()

        handleHeroSelectorPacket()

        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent {
            val type = if (FabricLoader.getInstance().environmentType == EnvType.SERVER) {
                ResourcePackActivationType.ALWAYS_ENABLED
            } else {
                ResourcePackActivationType.NORMAL
            }
            val result = ResourceManagerHelper.registerBuiltinResourcePack(
                "heroes".toId(),
                it,
                type
            )
            logger.info("Init Heroes DataPack: $result")
        }
    }

    private fun handleHeroSelectorPacket() {
        Networking.c2sHeroSelectorPacket.receiveOnServer { packet, context ->
            mcCoroutineTask(sync = true, client = false) {
                val hero = HeroManager.getHero(packet) ?: return@mcCoroutineTask
                val event = HeroEvents.HeroSelectEvent(context.player, hero, true)
                HeroEvents.heroSelectEvent.invoke(event)
                if (event.canSelect) {
                    context.player.setHero(hero)
                    Networking.s2cHeroSelectorPacket.send(
                        HeroSelectorPacket(
                            emptyList(),
                            false,
                            KitEditorManager.hasKitWorld
                        ), context.player
                    )
                }
            }
        }
    }
}
