package gg.norisk.heroes.katara.client.render

import net.minecraft.block.BlockState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.fluid.FluidState
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockRenderView
import java.awt.Color

interface IFluidRendererExt {
    fun katara_renderFluid(
        matrixStack: MatrixStack,
        blockRenderView: BlockRenderView,
        post: Vec3d,
        vertexConsumer: VertexConsumer,
        blockState: BlockState,
        fluidState: FluidState,
        waterColor: Color?
    )
}