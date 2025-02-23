package gg.norisk.ffa.server.command

import gg.norisk.ffa.server.utils.luckperms.hasPermission
import kotlinx.coroutines.Job
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setCustomName
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.logging.logger
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.igui.*
import net.silkmc.silk.igui.elements.GuiPlaceholder

object InvseeCommand {
    fun init() {
        command("invsee") {
            requires { it.playerOrThrow.hasPermission("hglabor.staff.invsee") }

            argument("player", EntityArgumentType.player()) { playerArg ->
                runs {
                    val sender = source.playerOrThrow
                    val player = playerArg().getPlayer(source)

                    val gui = igui(GuiType.NINE_BY_SIX, literalText("${player.gameProfile.name}'s Inventory") { color = 0x24ADE3 }, 1) {
                        page(1) {  }
                    }
                    sender.openGui(gui)

                    var updateJob: Job? = null
                    updateJob = mcCoroutineTask(sync = false, client = false, howOften = Long.MAX_VALUE, period = 10.ticks) {
                        if (sender.isDisconnected || sender.currentScreenHandler == null || player.isDisconnected) {
                            logger().info("cancelling task")
                            return@mcCoroutineTask updateJob!!.cancel()
                        }
                        gui.loadPage(createPageFromPlayer(player))
                    }
                }
            }
        }
    }

    private fun createPageFromPlayer(player: ServerPlayerEntity): GuiPage {
        val inventory = player.inventory
        val content = buildMap<Int, GuiElement> {
            var index = 0

            val rearrangedItems = buildList {
                addAll(inventory.main.takeLast(27))
                addAll(inventory.main.take(9))
            }
            rearrangedItems.forEach { itemStack ->
                put(index++, GuiPlaceholder(itemStack.copy().apply {
                    setCustomName { text(name) { italic = false } }
                }.guiIcon))
            }

            // Placeholder to split inventory and armor
            repeat(9) {
                put(index++, GuiPlaceholder(itemStack(Items.IRON_BARS) { setCustomName("") }.guiIcon))
            }

            inventory.armor.forEach { itemStack ->
                put(index++, GuiPlaceholder(itemStack.copy().apply {
                    setCustomName { text(name) { italic = false } }
                }.guiIcon))
            }

            inventory.offHand.forEach { itemStack ->
                put(index++, GuiPlaceholder(itemStack.copy().apply {
                    setCustomName { text(name) { italic = false } }
                }.guiIcon))
            }

            put(53, GuiPlaceholder(itemStack(Items.POPPY) {
                setCustomName {
                    text("Health: ") { color = NamedTextColor.GRAY.value(); italic = false }
                    text("${player.health.toInt() / 2.0}") { color = NamedTextColor.RED.value(); italic = false }
                }
            }.guiIcon))
        }
        logger().info("Creating page with content: $content")
        return GuiPage("1", 1, content, null, null)
    }
}
