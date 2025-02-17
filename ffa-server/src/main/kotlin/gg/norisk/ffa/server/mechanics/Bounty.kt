package gg.norisk.ffa.server.mechanics

import com.mojang.brigadier.arguments.IntegerArgumentType
import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.heroes.common.player.ffaPlayer
import gg.norisk.heroes.common.player.ffaBounty
import gg.norisk.heroes.server.database.player.PlayerProvider
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.server.players
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText

object Bounty {
    fun init() {
        command("bounty") {
            alias("kopfgeld")
            argument("player", EntityArgumentType.player()) {
                runsAsync {
                    val player = EntityArgumentType.getPlayer(this, "player")
                    val source = this.source.playerOrThrow
                    val ffaPlayer = PlayerProvider.get(player.uuid)
                    source.sendMessage(Text.translatable("ffa.mechanic.bounty.info", player.name, ffaPlayer.bounty))
                }
                argument<Int>("bounty", IntegerArgumentType.integer(100)) { bountyToGive ->
                    runsAsync {
                        val player = EntityArgumentType.getPlayer(this, "player")
                        val source = this.source.playerOrThrow
                        val ffaPlayer = PlayerProvider.get(player.uuid)
                        val sourceFfaPlayer = PlayerProvider.get(source.uuid)

                        println("Player: $sourceFfaPlayer")

                        if (bountyToGive() > sourceFfaPlayer.xp) {
                            source.sendMessage(Text.translatable("ffa.mechanic.bounty.not_enough_xp"))
                            return@runsAsync
                        }

                        sourceFfaPlayer.xp -= bountyToGive()
                        source.ffaPlayer = sourceFfaPlayer

                        ffaPlayer.bounty += bountyToGive()
                        player.ffaPlayer = ffaPlayer

                        PlayerProvider.save(sourceFfaPlayer)
                        PlayerProvider.save(ffaPlayer)

                        this.source.server.broadcastText(Text.translatable("ffa.mechanic.bounty.placed", source.name, bountyToGive().toString(), player.name))
                    }
                }
            }
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            mcCoroutineTask(sync = false, client = false) {
                for (player in server.players) {
                    if (!player.isFFA) continue
                    updateBountyScoreboard(player)
                }
            }
        }
    }

    suspend fun receiveBounty(receiver: ServerPlayerEntity, target: ServerPlayerEntity) {
        val targetDb = PlayerProvider.get(target.uuid)
        val receiverDb = PlayerProvider.get(receiver.uuid)

        if (targetDb.bounty > 0) {
            val bounty = targetDb.bounty
            targetDb.bounty = 0
            receiverDb.xp += bounty
            receiver.server.broadcastText(Text.translatable("ffa.mechanic.bounty.claimed", receiver.name, bounty, target.name))
            receiver.ffaPlayer = receiverDb
            target.ffaPlayer = targetDb
            PlayerProvider.save(receiverDb)
            PlayerProvider.save(targetDb)
        }
    }

    private suspend fun updateBountyScoreboard(player: ServerPlayerEntity) {
        val ffaPlayer = PlayerProvider.get(player.uuid)
        if (ffaPlayer.bounty != player.ffaBounty) {
            player.ffaBounty = ffaPlayer.bounty
        }
    }
}
