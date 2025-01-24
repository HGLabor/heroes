package gg.norisk.heroes.aang.client.render.entity.model

import gg.norisk.heroes.aang.entity.TornadoEntity
import net.minecraft.client.model.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.model.SinglePartEntityModel
import net.minecraft.util.math.MathHelper
import java.util.function.Function

class TornadoEntityModel(private val root: ModelPart) : SinglePartEntityModel<TornadoEntity>(
    Function(RenderLayer::getEntityTranslucent)
) {
    val head: ModelPart
    val eyes: ModelPart
    val windBody: ModelPart = root.getChild("wind_body")
    private val windTop: ModelPart
    private val windMid: ModelPart
    private val windBottom: ModelPart = windBody.getChild("wind_bottom")
    val rods: ModelPart

    init {
        this.windMid = windBottom.getChild("wind_mid")
        this.windTop = windMid.getChild("wind_top")
        this.head = root.getChild("body").getChild("head")
        this.eyes = head.getChild("eyes")
        this.rods = root.getChild("body").getChild("rods")
    }

    override fun setAngles(breezeEntity: TornadoEntity, f: Float, g: Float, h: Float, i: Float, j: Float) {
        this.part.traverse().forEach { obj: ModelPart -> obj.resetTransform() }
        val k = h * 3.1415927f * -0.1f
        windTop.pivotX = MathHelper.cos(k) * 1.0f * 0.6f
        windTop.pivotZ = MathHelper.sin(k) * 1.0f * 0.6f
        windMid.pivotX = MathHelper.sin(k) * 0.5f * 0.8f
        windMid.pivotZ = MathHelper.cos(k) * 0.8f
        windBottom.pivotX = MathHelper.cos(k) * -0.25f * 1.0f
        windBottom.pivotZ = MathHelper.sin(k) * -0.25f * 1.0f
        head.pivotY = 4.0f + MathHelper.cos(k) / 4.0f
        rods.yaw = h * 3.1415927f * 0.1f
    }

    override fun getPart(): ModelPart {
        return this.root
    }

    companion object {
        private const val field_47431 = 0.6f
        private const val field_47432 = 0.8f
        private const val field_47433 = 1.0f
        fun getTexturedModelData(i: Int, j: Int): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root
            val modelPartData2 =
                modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f))
            val modelPartData3 =
                modelPartData2.addChild("rods", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 8.0f, 0.0f))
            modelPartData3.addChild(
                "rod_1",
                ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, Dilation(0.0f)),
                ModelTransform.of(2.5981f, -3.0f, 1.5f, -2.7489f, -1.0472f, 3.1416f)
            )
            modelPartData3.addChild(
                "rod_2",
                ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, Dilation(0.0f)),
                ModelTransform.of(-2.5981f, -3.0f, 1.5f, -2.7489f, 1.0472f, 3.1416f)
            )
            modelPartData3.addChild(
                "rod_3",
                ModelPartBuilder.create().uv(0, 17).cuboid(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, Dilation(0.0f)),
                ModelTransform.of(0.0f, -3.0f, -3.0f, 0.3927f, 0.0f, 0.0f)
            )
            val modelPartData4 = modelPartData2.addChild(
                "head",
                ModelPartBuilder.create().uv(4, 24).cuboid(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, Dilation(0.0f))
                    .uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 4.0f, 0.0f)
            )
            modelPartData4.addChild(
                "eyes",
                ModelPartBuilder.create().uv(4, 24).cuboid(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, Dilation(0.0f))
                    .uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f)
            )
            val modelPartData5 =
                modelPartData.addChild("wind_body", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f))
            val modelPartData6 = modelPartData5.addChild(
                "wind_bottom",
                ModelPartBuilder.create().uv(1, 83).cuboid(-2.5f, -7.0f, -2.5f, 5.0f, 7.0f, 5.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, 24.0f, 0.0f)
            )
            val modelPartData7 = modelPartData6.addChild(
                "wind_mid",
                ModelPartBuilder.create().uv(74, 28).cuboid(-6.0f, -6.0f, -6.0f, 12.0f, 6.0f, 12.0f, Dilation(0.0f))
                    .uv(78, 32).cuboid(-4.0f, -6.0f, -4.0f, 8.0f, 6.0f, 8.0f, Dilation(0.0f)).uv(49, 71)
                    .cuboid(-2.5f, -6.0f, -2.5f, 5.0f, 6.0f, 5.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -7.0f, 0.0f)
            )
            modelPartData7.addChild(
                "wind_top",
                ModelPartBuilder.create().uv(0, 0).cuboid(-9.0f, -8.0f, -9.0f, 18.0f, 8.0f, 18.0f, Dilation(0.0f))
                    .uv(6, 6).cuboid(-6.0f, -8.0f, -6.0f, 12.0f, 8.0f, 12.0f, Dilation(0.0f)).uv(105, 57)
                    .cuboid(-2.5f, -8.0f, -2.5f, 5.0f, 8.0f, 5.0f, Dilation(0.0f)),
                ModelTransform.pivot(0.0f, -6.0f, 0.0f)
            )
            return TexturedModelData.of(modelData, i, j)
        }
    }
}
