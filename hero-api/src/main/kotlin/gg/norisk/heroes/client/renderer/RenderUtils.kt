package gg.norisk.heroes.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier

object RenderUtils {
    fun renderOverlay(drawContext: DrawContext, identifier: Identifier, f: Float) {
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.enableBlend()
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, f)
        drawContext.drawTexture(
            identifier,
            0,
            0,
            -90,
            0.0f,
            0.0f,
            drawContext.scaledWindowWidth,
            drawContext.scaledWindowHeight,
            drawContext.scaledWindowWidth,
            drawContext.scaledWindowHeight
        )
        RenderSystem.disableBlend()
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }
}
