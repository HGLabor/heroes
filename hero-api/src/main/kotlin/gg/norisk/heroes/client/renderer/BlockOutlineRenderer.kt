package gg.norisk.heroes.client.renderer

import gg.norisk.heroes.common.utils.toVec
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box


object BlockOutlineRenderer {

    fun drawBlockBox(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        blockPos: BlockPos,
        f: Float,
        g: Float,
        h: Float,
        i: Float
    ) {
        drawBox(matrixStack, vertexConsumerProvider, blockPos, blockPos.add(1, 1, 1), f, g, h, i)
    }

    fun drawBox(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        blockPos: BlockPos,
        blockPos2: BlockPos,
        f: Float,
        g: Float,
        h: Float,
        i: Float
    ) {
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        if (camera.isReady) {
            val vec3d = camera.pos.negate()
            val box = Box.from(blockPos.toVec()).offset(vec3d)
            drawBox(matrixStack, vertexConsumerProvider, box, f, g, h, i)
        }
    }

    fun drawBox(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        box: Box,
        f: Float,
        g: Float,
        h: Float,
        i: Float
    ) {
        drawBox(
            matrixStack,
            vertexConsumerProvider,
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            f,
            g,
            h,
            i
        )
    }

    fun drawBox(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        d: Double,
        e: Double,
        f: Double,
        g: Double,
        h: Double,
        i: Double,
        j: Float,
        k: Float,
        l: Float,
        m: Float
    ) {
        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getDebugFilledBox())
        WorldRenderer.renderFilledBox(matrixStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m)
    }
}
