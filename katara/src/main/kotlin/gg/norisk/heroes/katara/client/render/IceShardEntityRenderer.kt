package gg.norisk.heroes.katara.client.render

import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.entity.IceShardEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ProjectileEntityRenderer
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class IceShardEntityRenderer(context: EntityRendererFactory.Context) :
    ProjectileEntityRenderer<IceShardEntity>(context) {
    override fun getTexture(entity: IceShardEntity): Identifier {
        return TEXTURE
    }

    companion object {
        val TEXTURE: Identifier = "textures/entity/projectiles/ice_shard.png".toId()
    }
}