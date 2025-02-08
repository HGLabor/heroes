package gg.norisk.ffa.server.command

import gg.norisk.ffa.server.mechanics.lootdrop.Lootdrop
import net.minecraft.server.world.ServerWorld
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command

object LootdropCommand {
    fun init() {
        command("lootdrop") {
            requires { it.playerOrThrow.hasPermissionLevel(PermissionLevel.OWNER.level) }

            runs {
                Lootdrop(source.playerOrThrow.world as ServerWorld, source.playerOrThrow.blockPos).drop()
            }
        }
    }
}
