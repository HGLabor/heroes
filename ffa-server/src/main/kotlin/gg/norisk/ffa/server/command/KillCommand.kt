package gg.norisk.ffa.server.command

import gg.norisk.ffa.server.mechanics.CombatTag.isInCombat
import gg.norisk.ffa.server.selector.SelectorServerManager.setSelectorReady
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literalText

object KillCommand {
    fun init() {
        command("kill") {
            alias("spawn")
            requiresPermissionLevel(PermissionLevel.NONE)
            runs {
                val player = this.source.playerOrThrow
                if (player.isInCombat()) {
                    player.kill()
                } else {
                    player.setSelectorReady()
                }
            }
        }

        command("me") {
            argument<String>("action") { action ->
                runs {
                    val player = this.source.playerOrThrow
                    this.source.playerOrThrow.sendMessage(literalText {
                        text(player.name)
                        text(": ")
                        text("Ich bin ein kompletter Versager")
                    })
                }
            }
        }
    }
}