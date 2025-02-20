package gg.norisk.heroes.katara.client.render

import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.entity.IceShardEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ProjectileEntityRenderer
import net.minecraft.client.render.entity.state.ArrowEntityRenderState
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class IceShardEntityRenderer(context: EntityRendererFactory.Context) :
    ProjectileEntityRenderer<IceShardEntity, ProjectileEntityRenderState>(context) {
    companion object {
        val TEXTURE: Identifier = "textures/entity/projectiles/ice_shard.png".toId()
    }

    override fun getTexture(state: ProjectileEntityRenderState): Identifier {
        return TEXTURE
    }

    override fun createRenderState(): ProjectileEntityRenderState {
        return ArrowEntityRenderState()
    }
}