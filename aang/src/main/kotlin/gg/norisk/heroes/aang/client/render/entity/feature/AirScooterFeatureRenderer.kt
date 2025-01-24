package gg.norisk.heroes.aang.client.render.entity.feature

import gg.norisk.heroes.aang.client.render.entity.model.AirScooterEntityModel
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity

class AirScooterFeatureRenderer<T : Entity, M : EntityModel<T>>(featureRendererContext: FeatureRendererContext<T, M>) :
    FeatureRenderer<T, M>(featureRendererContext) {
    val airBall = AirScooterEntityModel(AirScooterEntityModel.getTexturedModelData().createModel())

    override fun render(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        entity: T,
        f: Float,
        g: Float,
        h: Float,
        j: Float,
        k: Float,
        l: Float
    ) {
        /*val player = entity as? PlayerEntity? ?: return
        if (!player.isAirScooting) return
        matrixStack.push()
        val age = entity.age.toFloat() + h
        val vertexConsumer = vertexConsumerProvider.getBuffer(
            RenderLayer.getBreezeWind(
                TEXTURE,
                getXOffset(age) % 1.0f, 0.0f
            )
        )
        val scale: Float = 3.0f
        matrixStack.translate(0.0, entity.getEyeHeight(entity.pose).toDouble(), 0.0)
        matrixStack.scale(scale, scale, scale)
        airBall.setAngles(null, 0.0f, 0.0f, age, 0.0f, 0.0f)
        airBall.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV)
        matrixStack.pop()*/

    }
}
