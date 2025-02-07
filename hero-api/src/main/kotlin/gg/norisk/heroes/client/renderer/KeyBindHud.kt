package gg.norisk.heroes.client.renderer

import gg.norisk.heroes.common.hero.getHero
import gg.norisk.heroes.common.hero.utils.ColorUtils
import gg.norisk.ui.api.value.key
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.core.world.pos.Pos2i
import java.awt.Color

object KeyBindHud {
    fun init() {
        HudRenderCallback.EVENT.register(::render)
    }

    fun render(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val player = MinecraftClient.getInstance().player ?: return
        val hero = player.getHero() ?: return
        val offset = 2
        val scale = 0.75f

        drawContext.matrices.push()
        drawContext.matrices.scale(scale, scale, scale)


        hero.getUsableAbilities(player).map { ability ->
            val keyBind = ability.keyBind
            var text = literalText {
                text {
                    //if (keyBind.condition != null) text("${keyBind.condition.hudText} + ")
                    val deactivatedColor = 0x4A4A4A
                    text(
                        keyBind?.boundKeyLocalizedText ?: keyBind?.defaultKey?.localizedText
                        ?: ability.getCustomActivation()
                    ) {
                        color = if (ability.hasCooldown(player)) {
                            deactivatedColor
                        } else {
                            hero.color
                        }
                    }
                    if (ability.condition != null) {
                        text(" + ")
                        text(Text.translatable("heroes.ability.condition.short.${ability.internalKey}")) {
                            color = if (ability.condition?.invoke(player) == true || ability.condition == null) {
                                hero.color
                            } else {
                                deactivatedColor
                            }
                        }
                    }
                }

                text(" - ") { color = 0x919191 }
                text(ability.name)
            }

            ability.getCooldown(player)?.let { cooldownInfo ->
                text = literalText {
                    text(text)
                    cooldownInfo.durationString?.let { extension ->
                        text(" ")
                        text(extension) { color = ColorUtils.contrast(0x248223) }
                    }
                }
            }

            text to ability
        }.sortedByDescending { MinecraftClient.getInstance().textRenderer.getWidth(it.first) }
            .forEachIndexed { index, (text, ability) ->
                val pos = Pos2i(5, 5 + (text.height + offset * 2) * index)
                drawContext.fill(
                    RenderLayer.getGuiOverlay(),
                    pos.x - offset,
                    pos.z - offset,
                    pos.x + text.width + offset,
                    pos.z + text.height + offset,
                    -1873784752
                )
                drawContext.drawText(
                    MinecraftClient.getInstance().textRenderer, literalText {
                        if (!ability.hasUnlocked(player)) {
                            text(text.string)
                            strikethrough = true
                            color = Colors.LIGHT_GRAY
                        } else {
                            text(text)
                        }
                    }, pos.x, pos.z, 14737632, true
                )
            }

        drawContext.matrices.pop()
    }

    val Text.width
        get() = MinecraftClient.getInstance().textRenderer.getWidth(this)

    val Text.height
        get() = MinecraftClient.getInstance().textRenderer.fontHeight
}
