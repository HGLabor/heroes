package gg.norisk.heroes.toph.render


import gg.norisk.utils.ext.EntityRenderStateExt
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.render.item.HeldItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ModelTransformationMode
import net.minecraft.util.math.RotationAxis

//Credits an https://github.com/chyzman/wearThat/blob/master/src/main/java/com/chyzman/wearthat/client/WearThatClient.java
class ChestItemFeatureRenderer(
    context: FeatureRendererContext<EntityRenderState, EntityModel<EntityRenderState>>,
    private val heldItemRenderer: HeldItemRenderer
) : FeatureRenderer<EntityRenderState, EntityModel<EntityRenderState>>(context) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        state: EntityRenderState,
        limbAngle: Float,
        limbDistance: Float
    ) {
        val mode = ModelTransformationMode.FIXED
        val entity = (state as EntityRenderStateExt).nrc_entity ?: return
        val chestStack = (entity as LivingEntity).getEquippedStack(EquipmentSlot.CHEST)
        if (!chestStack.isEmpty) {
            if (chestStack.get(DataComponentTypes.EQUIPPABLE)?.slot != EquipmentSlot.CHEST) {
                matrices.push()
                (this.contextModel as PlayerEntityModel).body.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.translate(0f, -1 / 4f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(1.01f, 1.01f, 1.01f)
                matrices.translate(0f, -1 / 4f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).rightArm.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(-1 / 12f, 0f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(0.99f, 0.99f, 0.99f)
                matrices.translate(0f, -1 / 2f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).rightArm.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(-1 / 12f, 0f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(1.25f, 1.25f, 1.25f)
                matrices.translate(0f, -0.75f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).leftArm.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(1 / 12f, 0f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(0.99f, 0.99f, 0.99f)
                matrices.translate(0f, -1 / 2f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).leftArm.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(1 / 12f, 0f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(1.2f, 1.2f, 1.2f)
                matrices.translate(0f, -0.75f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    chestStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
            }
        }
        val legsStack = (entity as LivingEntity).getEquippedStack(EquipmentSlot.LEGS)
        if (!legsStack.isEmpty) {
            if (legsStack.get(DataComponentTypes.EQUIPPABLE)?.slot != EquipmentSlot.LEGS) {
                matrices.push()
                (this.contextModel as PlayerEntityModel).rightLeg.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(0f, -1 / 6f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    legsStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(1.01f, 1.01f, 1.01f)
                matrices.translate(0f, -1 / 3f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    legsStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).leftLeg.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(2 / 3f, 2 / 3f, 2 / 3f)
                matrices.translate(0f, -1 / 6f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    legsStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.scale(1.01f, 1.01f, 1.01f)
                matrices.translate(0f, -1 / 3f, 0f)
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    legsStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
            }
        }
        val feetStack = (entity as LivingEntity).getEquippedStack(EquipmentSlot.FEET)
        if (!feetStack.isEmpty) {
            if (feetStack.get(DataComponentTypes.EQUIPPABLE)?.slot != EquipmentSlot.FEET) {
                matrices.push()
                (this.contextModel as PlayerEntityModel).rightLeg.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(0.75f, 0.75f, 0.75f)
                matrices.translate(0f, -0.8f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    feetStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
                matrices.push()
                (this.contextModel as PlayerEntityModel).leftLeg.rotate(matrices)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f))
                matrices.scale(0.75f, 0.75f, 0.75f)
                matrices.translate(0f, -0.8f, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
                heldItemRenderer.renderItem(
                    entity as LivingEntity,
                    feetStack,
                    mode,
                    false,
                    matrices,
                    vertexConsumers,
                    light
                )
                matrices.pop()
            }
        }
    }
}
