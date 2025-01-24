package gg.norisk.heroes.aang.client.render.entity.feature

import gg.norisk.heroes.aang.client.render.entity.TornadoEntityRenderer.Companion.updatePartVisibility
import gg.norisk.heroes.aang.client.render.entity.model.TornadoEntityModel
import gg.norisk.heroes.aang.entity.TornadoEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class TornadoWindFeatureRenderer(
    context: EntityRendererFactory.Context,
    featureRendererContext: FeatureRendererContext<TornadoEntity?, TornadoEntityModel?>?
) : FeatureRenderer<TornadoEntity, TornadoEntityModel?>(featureRendererContext) {
    private val model = TornadoEntityModel(context.getPart(EntityModelLayers.BREEZE_WIND))

    override fun render(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        breezeEntity: TornadoEntity,
        f: Float,
        g: Float,
        h: Float,
        j: Float,
        k: Float,
        l: Float
    ) {
        val m = breezeEntity.age.toFloat() + h
        val vertexConsumer =
            vertexConsumerProvider.getBuffer(RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(m) % 1.0f, 0.0f))
        model.setAngles(breezeEntity, f, g, j, k, l)
        updatePartVisibility(this.model, model.windBody).render(
            matrixStack,
            vertexConsumer,
            i,
            OverlayTexture.DEFAULT_UV
        )
    }

    private fun getXOffset(f: Float): Float {
        return f * 0.02f
    }

    companion object {
        private val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/breeze/breeze_wind.png")
    }
}
