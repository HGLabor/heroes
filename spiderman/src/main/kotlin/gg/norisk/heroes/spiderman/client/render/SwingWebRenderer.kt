package gg.norisk.heroes.spiderman.client.render

import gg.norisk.heroes.spiderman.entity.SwingWebEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.client.util.math.MatrixStack

class SwingWebRenderer(context: EntityRendererFactory.Context) :
    EntityRenderer<SwingWebEntity, ProjectileEntityRenderState>(context) {
    private val itemRenderState = ItemRenderState()

    override fun createRenderState(): ProjectileEntityRenderState? {
        return ProjectileEntityRenderState()
    }

    override fun render(
        state: ProjectileEntityRenderState?,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider?,
        light: Int
    ) {
        matrixStack.push()
        matrixStack.multiply(dispatcher.rotation)
        ItemRenderState().render(
            matrixStack,
            vertexConsumerProvider,
            light,
            OverlayTexture.DEFAULT_UV
        )
        matrixStack.pop()
        super.render(state, matrixStack, vertexConsumerProvider, light)
    }
}