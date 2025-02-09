package gg.norisk.heroes.katara.entity

import gg.norisk.heroes.katara.registry.SoundRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvent
import net.minecraft.world.World
import kotlin.random.Random

class IceShardEntity(entityType: EntityType<out PersistentProjectileEntity>, world: World) :
    PersistentProjectileEntity(entityType, world) {

    override fun getDefaultItemStack(): ItemStack {
        return ItemStack(Items.ICE);
    }


    override fun tick() {
        super.tick()
        if (!inGround) {
            if (world.isClient) {
                if (Random.nextBoolean() && Random.nextBoolean()) {
                    world.addParticle(ParticleTypes.SNOWFLAKE, this.x, this.y, this.z, 0.0, 0.0, 0.0)
                }
            }
        }
    }

    override fun getHitSound(): SoundEvent {
        return SoundRegistry.ICE_PLACE
    }
}