package gg.norisk.heroes.aang.registry

import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.client.render.entity.AirScooterEntityRenderer
import gg.norisk.heroes.aang.client.render.entity.TornadoEntityRenderer
import gg.norisk.heroes.aang.client.render.entity.model.AirScooterEntityModel
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.model.EntityModelLayer


object EntityRendererRegistry {
    val AIR_SCOOTER_LAYER: EntityModelLayer = EntityModelLayer("air_scooter".toId(), "main")

    fun init() {
        EntityRendererRegistry.register(EntityRegistry.AIR_SCOOTER, ::AirScooterEntityRenderer)
        EntityRendererRegistry.register(EntityRegistry.TORNADO, ::TornadoEntityRenderer)
        EntityModelLayerRegistry.registerModelLayer(
            AIR_SCOOTER_LAYER,
            AirScooterEntityModel.Companion::getTexturedModelData
        );
    }
}
