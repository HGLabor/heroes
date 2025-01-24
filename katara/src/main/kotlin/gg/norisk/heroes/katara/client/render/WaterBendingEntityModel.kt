package gg.norisk.heroes.katara.client.render

import gg.norisk.heroes.katara.entity.WaterBendingEntity
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.model.SinglePartEntityModel

class WaterBendingEntityModel(val modelPart: ModelPart) :
    SinglePartEntityModel<WaterBendingEntity>(RenderLayer::getEntityTranslucent) {
    override fun setAngles(entity: WaterBendingEntity?, f: Float, g: Float, h: Float, i: Float, j: Float) {
    }

    companion object {
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            return TexturedModelData.of(modelData, 64, 32)
        }
    }

    override fun getPart(): ModelPart {
        return modelPart
    }
}
