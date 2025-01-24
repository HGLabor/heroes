//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package gg.norisk.heroes.aang.client.render.entity

import gg.norisk.heroes.aang.client.render.entity.feature.TornadoWindFeatureRenderer
import gg.norisk.heroes.aang.client.render.entity.model.TornadoEntityModel
import gg.norisk.heroes.aang.entity.TornadoEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis

@Environment(EnvType.CLIENT)
class TornadoEntityRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<TornadoEntity, TornadoEntityModel>(
        context, TornadoEntityModel(context.getPart(EntityModelLayers.BREEZE)), 0f
    ) {
    init {
        addFeature(TornadoWindFeatureRenderer(context, this))
    }

    override fun getTexture(entity: TornadoEntity): Identifier {
        return TEXTURE
    }

    override fun render(
        breezeEntity: TornadoEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        matrixStack.push()
        val model: TornadoEntityModel = getModel()
        updatePartVisibility(model, model.head, model.rods)
        model.head.visible = false
        model.rods.visible = false
        // Hier wird die neue Rotation berechnet
        val m = breezeEntity.age.toFloat() + g
        val rotationAngle = this.getRotationAngle(m)

        // Rotiert die Entität kontinuierlich um die Y-Achse (vertikale Achse)
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationAngle))
        super.render(breezeEntity, f, g, matrixStack, vertexConsumerProvider, i)
        matrixStack.pop()
    }

    private fun getRotationAngle(ageInTicks: Float): Float {
        val rotationSpeed = 20.0f // Passt die Rotationsgeschwindigkeit an (Winkel pro Tick)
        return (ageInTicks * rotationSpeed) % 360.0f // Vollständige Rotation (0 bis 360 Grad)
    }

    override fun setupTransforms(
        livingEntity: TornadoEntity?,
        matrixStack: MatrixStack?,
        f: Float,
        g: Float,
        h: Float,
        i: Float
    ) {

    }

    companion object {
        private val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/breeze/breeze.png")

        fun updatePartVisibility(
            breezeEntityModel: TornadoEntityModel, vararg modelParts: ModelPart
        ): TornadoEntityModel {
            breezeEntityModel.head.visible = false
            breezeEntityModel.eyes.visible = false
            breezeEntityModel.rods.visible = false
            breezeEntityModel.windBody.visible = false
            val var2: Array<out ModelPart> = modelParts
            val var3 = modelParts.size

            for (var4 in 0 until var3) {
                val modelPart = var2[var4]
                modelPart.visible = true
            }

            return breezeEntityModel
        }
    }
}
