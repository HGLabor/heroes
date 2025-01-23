package gg.norisk.heroes.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.HeroesManager.toId
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlUniform
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

object Speedlines {
    var client: MinecraftClient = MinecraftClient.getInstance()
    var lerpedSpeed: Double = 0.0

    lateinit var edge: GlUniform
    lateinit var speedlinesRenderTypeProgram: ShaderProgram

    private const val SPEEDLINES_KEY = "speedlines"

    var PlayerEntity.showSpeedlines: Boolean
        get() = this.getSyncedData<Boolean>(SPEEDLINES_KEY) == true
        set(value) {
            this.setSyncedData(SPEEDLINES_KEY, value)
        }

    fun initClient() {
        CoreShaderRegistrationCallback.EVENT.register(CoreShaderRegistrationCallback { context: CoreShaderRegistrationCallback.RegistrationContext ->
            context.register(
                "speedlines".toId(), VertexFormats.POSITION
            ) { shaderProgram: ShaderProgram ->
                speedlinesRenderTypeProgram = shaderProgram
                edge = shaderProgram.getUniform("Edge")!!
            }
        })

        HudRenderCallback.EVENT.register(HudRenderCallback { context: DrawContext, tickCounter: RenderTickCounter ->
            val player = MinecraftClient.getInstance().player ?: return@HudRenderCallback
            if (player.showSpeedlines) {
                val width = client.getWindow().width.toFloat()
                val height = client.getWindow().height.toFloat()
                val delta = tickCounter.getTickDelta(false)
                lerpedSpeed =
                    MathHelper.lerp((delta * 0.05f).toDouble(), lerpedSpeed, client.player!!.velocity.length())

                var speed = max(0.0, (lerpedSpeed - 0.2) / 2f)
                speed = min(speed, 0.2)
                edge.set((0.5f - speed).toFloat())

                val positionMatrix = context.matrices.peek().positionMatrix
                val tessellator = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
                tessellator.vertex(positionMatrix, 0f, height, 0f)
                tessellator.vertex(positionMatrix, 0f, 0f, 0f)
                tessellator.vertex(positionMatrix, width, 0f, 0f)
                tessellator.vertex(positionMatrix, width, height, 0f)
                RenderSystem.setShader { speedlinesRenderTypeProgram }
                setupRender()
                BufferRenderer.drawWithGlobalProgram(tessellator.end())
                endRender()
            }
        })
    }

    private fun setupRender() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.disableCull()
        RenderSystem.depthFunc(GL11.GL_ALWAYS)
    }

    private fun endRender() {
        RenderSystem.disableBlend()
    }
}
