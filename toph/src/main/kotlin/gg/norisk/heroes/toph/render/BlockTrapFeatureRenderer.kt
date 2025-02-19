package gg.norisk.heroes.toph.render

/* TODO 1.21.4
import gg.norisk.heroes.toph.ability.isEarthTrapped
import gg.norisk.heroes.toph.entity.ITrappedEntity
import gg.norisk.heroes.toph.mixin.ModelPartAccessor
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
import net.minecraft.util.math.random.Random


//Credits an https://github.com/chyzman/wearThat/blob/master/src/main/java/com/chyzman/wearthat/client/WearThatClient.java
class BlockTrapFeatureRenderer<T : LivingEntity, M : EntityModel<T>>(
    context: FeatureRendererContext<T, M>,
    private val heldItemRenderer: HeldItemRenderer,
    val root: ModelPart,
) : FeatureRenderer<T, M>(context) {
    val legs = (root as ModelPartAccessor).children.filter {
        it.key.contains("leg") || it.key.contains("tentacle") || it.key.contains("rod")
    }.map { it.value }

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
        val trapped = entity as? ITrappedEntity? ?: return
        if (!entity.isEarthTrapped()) {
            if (trapped.earthRotationAnimation == null || trapped.earthRotationAnimation?.isDone == true) {
                return
            }
        }

        var current: Class<*> = this.contextModel::class.java
        while (current.superclass != null) { // we don't want to process Object.class
            current.declaredFields.forEach { field ->
                runCatching {
                    field.isAccessible = true
                    field.get(this.contextModel) as ModelPart
                }.onSuccess {
                    val random = Random.create()
                    if (it.isEmpty) return@onSuccess
                    if (legs.any { leg -> compareCuboids(leg.getRandomCuboid(random), it.getRandomCuboid(random)) }) {
                        it.renderBlock(matrices, entity, vertexConsumers, light)
                    }
                }
                runCatching {
                    field.isAccessible = true
                    field.get(this.contextModel) as Array<ModelPart>
                }.onSuccess { modelParts ->
                    for (it in modelParts) {
                        if (it.isEmpty) return@onSuccess
                        val random = Random.create()
                        if (legs.any { leg ->
                                compareCuboids(
                                    leg.getRandomCuboid(random),
                                    it.getRandomCuboid(random)
                                )
                            }) {
                            it.renderBlock(matrices, entity, vertexConsumers, light)
                        }
                    }
                }
            }
            current = current.superclass
        }
    }

    fun compareCuboids(cuboid1: ModelPart.Cuboid, cuboid2: ModelPart.Cuboid): Boolean {
        return cuboid1.minX == cuboid2.minX &&
                cuboid1.minY == cuboid2.minY &&
                cuboid1.minZ == cuboid2.minZ &&
                cuboid1.maxX == cuboid2.maxX &&
                cuboid1.maxY == cuboid2.maxY &&
                cuboid1.maxZ == cuboid2.maxZ
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
*/