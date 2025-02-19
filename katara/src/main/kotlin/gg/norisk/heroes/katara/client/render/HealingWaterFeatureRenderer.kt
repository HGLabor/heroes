package gg.norisk.heroes.katara.client.render

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.ability.HealingAbility.isReceivingWaterHealing
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import org.joml.Matrix4f

/*
@Environment(value = EnvType.CLIENT)
class HealingWaterFeatureRenderer<T : Entity, M : EntityModel<T>>(featureRendererContext: FeatureRendererContext<T, M>) :
    FeatureRenderer<T, M>(featureRendererContext) {
    companion object {
        val healingWaterTexture = "textures/overlay/healing_water.png".toId()

        val LAYER: RenderLayer = RenderLayer.of(
            "katara_healing_overlay",
            VertexFormats.POSITION_TEXTURE,
            VertexFormat.DrawMode.QUADS,
            1536,
            RenderLayer.MultiPhaseParameters.builder()
                .program(RenderPhase.ENTITY_GLINT_PROGRAM)
                .texture(
                    RenderPhase.Texture(
                        Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png"),
                        true,
                        false
                    )
                )
                .writeMaskState(RenderPhase.COLOR_MASK)
                .cull(RenderPhase.DISABLE_CULLING)
                .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
                .transparency(RenderPhase.GLINT_TRANSPARENCY)
                .target(RenderPhase.ITEM_ENTITY_TARGET)
                .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
                .build(false)
        )

        private fun setupOverlayTexture() {
            val l = (Util.getMeasuringTimeMs()
                .toDouble() * MinecraftClient.getInstance().options.glintSpeed.value * 8.0).toLong()
            val f = (l % 110000L).toFloat() / 110000.0f
            val g = (l % 30000L).toFloat() / 30000.0f
            RenderSystem.setTextureMatrix(Matrix4f().translation(f, g, 0.0f))
        }
    }

    override fun render(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        entity: T,
        f: Float,
        g: Float,
        h: Float,
        j: Float,
        k: Float,
        l: Float
    ) {
        val livingEntity = entity as? LivingEntity? ?: return
        if (livingEntity.isReceivingWaterHealing) {
            val m = (entity as Entity).age.toFloat() + h
            val entityModel: EntityModel<T> = this.contextModel
            entityModel.animateModel(entity, f, g, h)
            this.contextModel.copyStateTo(entityModel)
            val vertexConsumer = vertexConsumerProvider.getBuffer(LAYER)
            entityModel.setAngles(entity, f, g, j, k, l)
            entityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV)
        }
    }
}
*/