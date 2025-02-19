package gg.norisk.heroes.aang.client.render.entity

import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.client.render.entity.model.AirScooterEntityModel
import gg.norisk.heroes.aang.entity.AirScooterEntity
import gg.norisk.heroes.aang.registry.EntityRendererRegistry
import gg.norisk.utils.ext.EntityRenderStateExt
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class AirScooterEntityRenderer(context: EntityRendererFactory.Context) :
    LivingEntityRenderer<AirScooterEntity, LivingEntityRenderState, AirScooterEntityModel>(
        context,
        AirScooterEntityModel(context.getPart(EntityRendererRegistry.AIR_SCOOTER_LAYER)),
        0f
    ) {

    override fun render(
        state: LivingEntityRenderState,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        val entity = (state as EntityRenderStateExt).nrc_entity as? AirScooterEntity? ?: return
        if (state.age >= 2 || !(dispatcher.camera.focusedEntity.squaredDistanceTo(
                Vec3d(state.x, state.y, state.z)
            ) < RANDOM_MOJANG_FIELD.toDouble())
        ) {
            val g = MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
            matrixStack.push()
            val h = state.age + g
            val vertexConsumer = vertexConsumerProvider.getBuffer(
                RenderLayer.getBreezeWind(
                    TEXTURE,
                    getXOffset(h) % 1.0f, 0.0f
                )
            )
            model.setAngles(state)
            val scale: Float = entity.getLerpedScale(g * 0.05f)
            matrixStack.scale(scale, scale, scale)
            model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV)
            matrixStack.pop()
        }
    }


    companion object {
        fun getXOffset(f: Float): Float {
            return f * 0.03f
        }

        private val RANDOM_MOJANG_FIELD = MathHelper.square(3.5f)
        val TEXTURE: Identifier = "textures/entity/projectiles/air_scooter.png".toId()
    }

    override fun createRenderState(): LivingEntityRenderState {
        return LivingEntityRenderState()
    }

    override fun getTexture(state: LivingEntityRenderState): Identifier {
        return TEXTURE
    }
}
