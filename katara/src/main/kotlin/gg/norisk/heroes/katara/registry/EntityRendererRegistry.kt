package gg.norisk.heroes.katara.registry

import gg.norisk.heroes.katara.client.render.WaterBendingEntityRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry


object EntityRendererRegistry {
    fun init() {
        EntityRendererRegistry.register(EntityRegistry.WATER_BENDING, ::WaterBendingEntityRenderer)
    }
}
