package gg.norisk.ffa.server.mechanics

import com.mojang.brigadier.arguments.IntegerArgumentType
import gg.norisk.ffa.server.FFAServer.isFFA
import gg.norisk.heroes.common.db.DatabaseManager
import gg.norisk.heroes.common.db.DatabaseManager.dbPlayer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.number.BlankNumberFormat
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.server.players
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.broadcastText
import net.silkmc.silk.core.text.literalText
import java.awt.Color

object Bounty {
    fun init() {
        command("bounty") {
            alias("kopfgeld")
            argument("player", EntityArgumentType.player()) {
                runsAsync {
                    val player = EntityArgumentType.getPlayer(this, "player")
                    val source = this.source.playerOrThrow
                    val dbPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
                    source.sendMessage(literalText {
                        text("${player.name} hat ein Kopfgeld von ${dbPlayer.bounty}")
                    })
                }
                argument<Int>("bounty", IntegerArgumentType.integer(0)) { bountyToGive ->
                    runsAsync {
                        val player = EntityArgumentType.getPlayer(this, "player")
                        val source = this.source.playerOrThrow
                        val dbPlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
                        val sourceDbPlayer = DatabaseManager.provider.getCachedPlayer(source.uuid)

                        println("Player: $sourceDbPlayer")

                        if (bountyToGive() > sourceDbPlayer.xp) {
                            source.sendMessage(literalText {
                                text("Du hast zu wenig Xp für dieses Kopfgeld")
                            })
                            return@runsAsync
                        }

                        sourceDbPlayer.xp -= bountyToGive()
                        source.dbPlayer = sourceDbPlayer

                        dbPlayer.bounty += bountyToGive()
                        player.dbPlayer = dbPlayer

                        DatabaseManager.provider.save(sourceDbPlayer)
                        DatabaseManager.provider.save(dbPlayer)

                        this.source.server.broadcastText {
                            text(source.name)
                            text(" hat das Kopfgeld von ") {
                                color = Color.YELLOW.rgb
                            }
                            text(bountyToGive().toString()) {
                                color = Color.GREEN.rgb
                            }
                            text(" auf ") {
                                color = Color.YELLOW.rgb
                            }
                            text(player.name)
                            text(" erteilt") {
                                color = Color.YELLOW.rgb
                            }
                        }
                    }
                }
            }
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            for (player in server.players) {
                if (!player.isFFA) continue
                updateBountyScoreboard(player)
            }
        }
    }

    fun receiveBounty(receiver: ServerPlayerEntity, target: ServerPlayerEntity) {
        val targetDb = DatabaseManager.provider.getCachedPlayer(target.uuid)
        val receiverDb = DatabaseManager.provider.getCachedPlayer(receiver.uuid)

        if (targetDb.bounty > 0) {
            val bounty = targetDb.bounty
            targetDb.bounty = 0
            receiverDb.xp += bounty
            receiver.server.broadcastText {
                text(receiver.name)
                text(" hat das Kopfgeld von ") {
                    color = Color.YELLOW.rgb
                }
                text(bounty.toString()) {
                    color = Color.GREEN.rgb
                }
                text(" für") {
                    color = Color.YELLOW.rgb
                }
                text(target.name)
                text(" erhalten") {
                    color = Color.YELLOW.rgb
                }
            }

            receiver.dbPlayer = receiverDb
            target.dbPlayer = targetDb

            mcCoroutineTask(sync = false, client = false) {
                DatabaseManager.provider.save(receiverDb)
                DatabaseManager.provider.save(targetDb)
            }
        }
    }

    private fun updateBountyScoreboard(player: ServerPlayerEntity) {
        val databasePlayer = DatabaseManager.provider.getCachedPlayer(player.uuid)
        val scoreboard = player.scoreboard
        if (databasePlayer.bounty > 0) {
            player.scoreboard.setObjectiveSlot(
                ScoreboardDisplaySlot.BELOW_NAME, ScoreboardObjective(
                    scoreboard,
                    "bounty",
                    ScoreboardCriterion.create("bounty"),
                    literalText {
                        text("Bounty: ")
                        text(databasePlayer.bounty.toString())
                    },
                    ScoreboardCriterion.RenderType.HEARTS,
                    true,
                    BlankNumberFormat.INSTANCE
                )
            )
        } else {
            player.scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.BELOW_NAME, null)
        }
    }
}