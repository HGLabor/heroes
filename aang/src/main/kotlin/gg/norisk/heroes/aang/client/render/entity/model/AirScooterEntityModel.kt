package gg.norisk.heroes.aang.client.render.entity.model

import gg.norisk.heroes.aang.entity.AirScooterEntity
import net.minecraft.client.model.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.model.SinglePartEntityModel

class AirScooterEntityModel(modelPart: ModelPart) : SinglePartEntityModel<AirScooterEntity>(
    RenderLayer::getEntityTranslucent
) {
    private val bone: ModelPart = modelPart.getChild("bone")
    private val windCharge: ModelPart = bone.getChild("wind_charge")
    private val wind: ModelPart = bone.getChild("wind")

    override fun setAngles(
        abstractWindChargeEntity: AirScooterEntity?, f: Float, g: Float, h: Float, i: Float, j: Float
    ) {
        windCharge.yaw = -h * 16.0f * ((Math.PI / 180.0).toFloat())
        wind.yaw = h * 16.0f * ((Math.PI / 180.0).toFloat())
    }

    override fun getPart(): ModelPart {
        return this.bone
    }

    companion object {
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root
            val modelPartData2 =
                modelPartData.addChild("bone", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f))
            modelPartData2.addChild(
                "wind",
                ModelPartBuilder.create()
                    .uv(15, 20).cuboid(-4.0f, -1.0f, -4.0f, 8.0f, 2.0f, 8.0f, Dilation(0.0f))
                    .uv(0, 9).cuboid(-3.0f, -2.0f, -3.0f, 6.0f, 4.0f, 6.0f, Dilation(0.0f)),
                ModelTransform.of(0.0f, 0.0f, 0.0f, 0.0f, -0.7854f, 0.0f)
            )
            modelPartData2.addChild(
                "wind_charge",
                ModelPartBuilder.create().uv(0, 0).cuboid(-2.0f, -2.0f, -2.0f, 4.0f, 4.0f, 4.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
            )
            return TexturedModelData.of(modelData, 64, 32)
        }
    }
}
