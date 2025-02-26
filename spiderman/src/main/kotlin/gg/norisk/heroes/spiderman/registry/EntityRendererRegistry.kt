package gg.norisk.heroes.spiderman.registry

import gg.norisk.heroes.spiderman.client.render.SwingWebRenderer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry

object EntityRendererRegistry {
    fun init() {
        EntityRendererRegistry.register(EntityRegistry.SWING_WEB, ::SwingWebRenderer)
    }
}
