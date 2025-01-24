package gg.norisk.ffa.server.command

import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literalText

object MeCommand {
    fun init() {
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