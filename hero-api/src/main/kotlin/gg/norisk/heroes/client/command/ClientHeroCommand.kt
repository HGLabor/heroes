package gg.norisk.heroes.client.command

import gg.norisk.heroes.client.config.ConfigManagerClient
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import kotlinx.serialization.encodeToString
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.text.literalText

object ClientHeroCommand {
    fun init() {
        clientCommand("heroes-client") {
            requires { it.enabledFeatures.contains(HeroesManager.heroesFlag) }
            literal("debug") {
                literal("printdbplayer") {
                    runs {
                        val player = this.source.player
                        player.sendMessage(literalText {
                            text("DB Player:")
                            emptyLine()
                            text(ConfigManagerClient.JSON.encodeToString(player.dbPlayer))
                        })
                    }
                }
            }
        }
    }
}