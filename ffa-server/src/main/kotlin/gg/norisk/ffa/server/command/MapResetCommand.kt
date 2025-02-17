package gg.norisk.ffa.server.command

import gg.norisk.ffa.server.schematic.SchematicHandler
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literalText

object MapResetCommand {
    fun init() {
        command("mapreset") {
            requires { it.hasPermissionLevel(PermissionLevel.OWNER.level) }

            literal("schematic") {
                literal("create") {
                    argument("startPos", BlockPosArgumentType.blockPos()) { startPosArg ->
                        argument("endPos", BlockPosArgumentType.blockPos()) { endPosArg ->
                            argument<String>("name") { nameArg ->
                                runsAsync {
                                    val player = source.playerOrThrow
                                    val world = player.world as ServerWorld
                                    val startPos = startPosArg().toAbsoluteBlockPos(source)
                                    val endPos = endPosArg().toAbsoluteBlockPos(source)
                                    val name = nameArg()

                                    val schematicDir =
                                        player.server.runDirectory.resolve("schematics/").toFile().apply {
                                            if (!exists()) mkdirs()
                                        }
                                    val schematicFile = schematicDir.resolve("$name.nbt")
                                    if (schematicFile.exists()) {
                                        player.sendMessage(literalText("A schematic ${schematicFile.name} already exists!") {
                                            color = 0xFF0000
                                        })
                                        return@runsAsync
                                    }
                                    schematicFile.createNewFile()
                                    SchematicHandler.saveSchematic(world, startPos, endPos, schematicFile)
                                    player.sendMessage(literalText("Schematic ${schematicFile.name} created!") {
                                        color = 0x00FF00
                                        clickEvent =
                                            ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, schematicFile.absolutePath)
                                    })
                                }
                            }
                        }
                    }
                }

                literal("paste") {
                    argument<String>("name") { nameArg ->
                        runsAsync {
                            val player = source.playerOrThrow
                            val world = player.world as ServerWorld
                            val name = nameArg()

                            val schematicDir =
                                player.server.runDirectory.resolve("schematics/").toFile().apply {
                                    if (!exists()) mkdirs()
                                }
                            val schematicFile = schematicDir.resolve("$name.nbt")

                            val schematic = SchematicHandler.loadSchematic(schematicFile)
                            SchematicHandler.pasteSchematic(world, schematic)
                        }
                    }
                }
            }
        }
    }
}
