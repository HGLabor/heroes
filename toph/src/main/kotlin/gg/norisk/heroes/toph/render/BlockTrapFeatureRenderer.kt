package gg.norisk.heroes.toph.render

import gg.norisk.heroes.toph.ability.isEarthTrapped
import gg.norisk.heroes.toph.entity.ITrappedEntity
import net.minecraft.block.Blocks
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.item.HeldItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.RotationAxis


//Credits an https://github.com/chyzman/wearThat/blob/master/src/main/java/com/chyzman/wearthat/client/WearThatClient.java
class BlockTrapFeatureRenderer<T : LivingEntity, M : EntityModel<T>>(
    context: FeatureRendererContext<T, M>,
    private val heldItemRenderer: HeldItemRenderer
) : FeatureRenderer<T, M>(context) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        val arrayNames = listOf("tentacles", "rods")

        val trapped = entity as? ITrappedEntity ?: return

        var current: Class<*> = this.contextModel::class.java
        while (current.superclass != null) { // we don't want to process Object.class
            current.declaredFields.forEach { field ->
                if (field.name.contains("leg", true)) {
                    runCatching {
                        field.isAccessible = true
                        field.get(this.contextModel) as ModelPart
                    }.onSuccess {
                        it.renderBlock(matrices, entity, vertexConsumers, light)
                    }
                } else if (arrayNames.any { field.name.contains(it, true) }) {
                    runCatching {
                        field.isAccessible = true
                        field.get(this.contextModel) as Array<ModelPart>
                    }.onSuccess {
                        for (modelPart in it) {
                            modelPart.renderBlock(matrices, entity, vertexConsumers, light)
                        }
                    }
                }
            }
            current = current.superclass
        }
    }

    private fun ModelPart.renderBlock(
        matrices: MatrixStack,
        entity: T,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        val trapped = entity as? ITrappedEntity ?: return
        val earthRotation = trapped.earthRotationAnimation ?: return

        matrices.push()
        rotate(matrices)

        var size = 0.0

        forEachCuboid(matrices) { entry, string, i, cuboid ->
            size = cuboid.maxY.toDouble()
        }

        matrices.translate(0.0, size / 18, 0.0) // Position anpassen
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(earthRotation.get()))

        //val legHeight = it.cuboids.firstOrNull()?.dimensions?.y?.toDouble() ?: 0.0

        val progress = if (entity.isEarthTrapped()) {
            earthRotation.get() / earthRotation.end
        } else {
            earthRotation.get() / earthRotation.start
        }

        val scale = progress
        matrices.scale(scale, scale, scale)
        heldItemRenderer.renderItem(
            entity as LivingEntity,
            Blocks.DIRT.asItem().defaultStack,
            ModelTransformationMode.FIXED,
            false,
            matrices,
            vertexConsumers,
            light
        )
        matrices.pop()
    }
}
