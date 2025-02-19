package gg.norisk.heroes.katara.client.render

import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.state.LivingEntityRenderState

class WaterBendingEntityModel(val modelPart: ModelPart) :
    EntityModel<LivingEntityRenderState>(modelPart, RenderLayer::getEntityTranslucent) {

    companion object {
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            return TexturedModelData.of(modelData, 64, 32)
        }
    }

    override fun setAngles(state: LivingEntityRenderState?) {
        super.setAngles(state)
    }
}
