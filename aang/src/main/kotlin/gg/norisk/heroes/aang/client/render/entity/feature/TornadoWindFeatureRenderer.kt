package gg.norisk.heroes.aang.client.render.entity.feature

import gg.norisk.heroes.aang.client.render.entity.TornadoEntityRenderer.Companion.updatePartVisibility
import gg.norisk.heroes.aang.client.render.entity.model.TornadoEntityModel
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class TornadoWindFeatureRenderer(
    context: EntityRendererFactory.Context,
    featureRendererContext: FeatureRendererContext<LivingEntityRenderState, TornadoEntityModel>
) : FeatureRenderer<LivingEntityRenderState, TornadoEntityModel>(featureRendererContext) {
    private val model = TornadoEntityModel(context.getPart(EntityModelLayers.BREEZE_WIND))

    private fun getXOffset(f: Float): Float {
        return f * 0.02f
    }

    companion object {
        private val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/breeze/breeze_wind.png")
    }

    override fun render(
        matrices: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int,
        state: LivingEntityRenderState,
        limbAngle: Float,
        limbDistance: Float
    ) {
        val h = MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
        val m = state.age.toFloat() + h
        val vertexConsumer =
            vertexConsumerProvider.getBuffer(RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(m) % 1.0f, 0.0f))
        model.setAngles(state)
        updatePartVisibility(this.model, model.windBody).render(
            matrices,
            vertexConsumer,
            light,
            OverlayTexture.DEFAULT_UV
        )
    }
}
