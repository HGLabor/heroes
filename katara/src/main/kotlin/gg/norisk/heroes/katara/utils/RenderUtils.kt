package gg.norisk.heroes.katara.utils

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.function.Consumer

object RenderUtils {
    fun renderBlock(
        matrixStack: MatrixStack,
        pos: Vec3d,
        state: BlockState,
        blockPos: BlockPos,
        consumer: Consumer<MatrixStack> = Consumer { }
    ) {
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        val renderer = MinecraftClient.getInstance().blockRenderManager
        val world = MinecraftClient.getInstance().world ?: return
        val vertexConsumer = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
            RenderLayers.getBlockLayer(state)
        )

        matrixStack.push()
        matrixStack.translate(
            pos.x - camera.getPos().x + 0.5,
            pos.y - camera.getPos().y + 0.5,
            pos.z - camera.getPos().z + 0.5
        )
        consumer.accept(matrixStack)
        // Verschiebe den Block um 0.5 in alle Richtungen und führe dann die Skalierung aus
       // matrixStack.scale(0.5f, 0.5f, 0.5f)

        // Rückverschiebung nach der Skalierung, um wieder zum tatsächlichen Mittelpunkt des Blocks zu gelangen
        matrixStack.translate(-0.5, -0.5, -0.5)
        renderer.renderBlock(
            state,
            blockPos,
            world,
            matrixStack,
            vertexConsumer,
            true,
            net.minecraft.util.math.random.Random.create()
        )
        matrixStack.pop()
    }
}
