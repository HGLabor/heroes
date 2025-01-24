package gg.norisk.heroes.aang.client.render.entity

import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.client.render.entity.model.AirScooterEntityModel
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.registry.EntityRendererRegistry
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

class AirScooterEntityRenderer(context: EntityRendererFactory.Context) :
    LivingEntityRenderer<AirScooterEntity, AirScooterEntityModel>(
        context,
        AirScooterEntityModel(context.getPart(EntityRendererRegistry.AIR_SCOOTER_LAYER)),
        0f
    ) {
    override fun render(
        abstractWindChargeEntity: AirScooterEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        if (abstractWindChargeEntity.age >= 2 || !(dispatcher.camera.focusedEntity.squaredDistanceTo(
                abstractWindChargeEntity
            ) < RANDOM_MOJANG_FIELD.toDouble())
        ) {
            matrixStack.push()
            val h = abstractWindChargeEntity.age.toFloat() + g
            val vertexConsumer = vertexConsumerProvider.getBuffer(
                RenderLayer.getBreezeWind(
                    TEXTURE,
                    getXOffset(h) % 1.0f, 0.0f
                )
            )
            model.setAngles(abstractWindChargeEntity, 0.0f, 0.0f, h, 0.0f, 0.0f)
            val scale: Float = abstractWindChargeEntity.getLerpedScale(g * 0.05f)
            matrixStack.scale(scale, scale, scale)
            model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV)
            matrixStack.pop()
        }
    }


    override fun getTexture(abstractWindChargeEntity: AirScooterEntity): Identifier {
        return TEXTURE
    }

    companion object {
        fun getXOffset(f: Float): Float {
            return f * 0.03f
        }

        private val RANDOM_MOJANG_FIELD = MathHelper.square(3.5f)
        val TEXTURE: Identifier = "textures/entity/projectiles/air_scooter.png".toId()
    }
}
