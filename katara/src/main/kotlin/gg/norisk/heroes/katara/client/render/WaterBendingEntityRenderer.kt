package gg.norisk.heroes.katara.client.render

import gg.norisk.heroes.katara.ability.WaterPillarAbility
import gg.norisk.heroes.katara.entity.WaterBendingEntity
import gg.norisk.utils.OldAnimation
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import java.awt.Color
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class WaterBendingEntityRenderer(context: EntityRendererFactory.Context) :
    LivingEntityRenderer<WaterBendingEntity, WaterBendingEntityModel>(
        context,
        //doesnt matter
        WaterBendingEntityModel(context.getPart(EntityModelLayers.PIG)),
        0f
    ) {

    override fun render(
        livingEntity: WaterBendingEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        val owner = livingEntity.getOwner()
        renderWater(
            matrixStack,
            livingEntity.pos,
            Fluids.FLOWING_WATER.defaultState.blockState,
            OldAnimation(0.5f, 1f, 1.seconds.toJavaDuration()),
            0,
            owner,
            f,
            livingEntity
        )
        for (position in livingEntity.positions) {
            matrixStack.push()
            val difference = position.pos.subtract(livingEntity.getLerpedPos(g))
            matrixStack.translate(difference.x, difference.y, difference.z)
            renderWater(
                matrixStack,
                position.pos,
                Fluids.FLOWING_WATER.defaultState.blockState,
                position.animation,
                position.startTime,
                owner, f, livingEntity
            )
            matrixStack.pop()
        }
        // super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i)
    }

    fun renderWater(
        matrixStack: MatrixStack,
        pos: Vec3d,
        state: BlockState,
        animation: OldAnimation,
        index: Int,
        owner: PlayerEntity?,
        tickDelta: Float,
        waterBendingEntity: WaterBendingEntity,
    ) {
        val renderer = MinecraftClient.getInstance().blockRenderManager
        val world = MinecraftClient.getInstance().world ?: return
        val vertexConsumer = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
            RenderLayers.getFluidLayer(state.fluidState)
        )

        matrixStack.push()
        // Berechne den animierten Sinus-Offset basierend auf tickDelta und index
        // Berechne den animierten Sinus-Offset basierend auf tickDelta und index
        val timeFactor = (System.currentTimeMillis() % 10000L) / 1000.0 // Zeit in Sekunden (loop alle 10 Sekunden)
        val sineOffset = Math.sin((timeFactor + index * 0.5) * Math.PI) * 0.05 // Wellenbewegung mit tickDelta animiert

        var scale = 0.5f
        matrixStack.scale(scale, scale, scale)
        matrixStack.scale(animation.get(), animation.get(), animation.get())
        owner?.apply {
            matrixStack.multiply(
                WaterPillarAbility.rotateTowards(
                    pos, this.getLerpedPos(tickDelta),
                    Quaternionf()
                )
            )
        }
        //matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f))
        matrixStack.translate(-0.5, -0.5 + sineOffset, -0.5)

        (renderer.fluidRenderer as IFluidRendererExt).katara_renderFluid(
            matrixStack,
            world,
            pos,
            vertexConsumer,
            state,
            state.fluidState,
            if (waterBendingEntity.isHealing) {
                Color.decode("#8aefff")
            } else {
                null
            }
        )

        matrixStack.pop()
    }

    override fun getTexture(entity: WaterBendingEntity): Identifier {
        return MinecraftClient.getInstance().player!!.skinTextures.texture
    }
}