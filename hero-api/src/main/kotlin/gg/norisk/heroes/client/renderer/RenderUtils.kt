package gg.norisk.heroes.client.renderer

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper

object RenderUtils {
    fun renderOverlay(context: DrawContext, identifier: Identifier, f: Float) {
        val i = ColorHelper.getWhite(f)
        context.drawTexture(
            RenderLayer::getGuiTexturedOverlay,
            identifier,
            0,
            0,
            0.0f,
            0.0f,
            context.scaledWindowWidth,
            context.scaledWindowHeight,
            context.getScaledWindowWidth(),
            context.getScaledWindowHeight(),
            i
        )
    }
}
