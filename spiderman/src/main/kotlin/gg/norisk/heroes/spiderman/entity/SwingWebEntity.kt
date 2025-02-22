package gg.norisk.heroes.spiderman.entity

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.TypeFilter
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World
import kotlin.math.abs

class SwingWebEntity(entityType: EntityType<SwingWebEntity>, world: World) : PersistentProjectileEntity(entityType, world) {
    var ropeLength: Double
        get() = this.getSyncedData<Double>("RopeLength") ?: 0.0
        set(value) = this.setSyncedData("RopeLength", value)
    var currentLength = -1.0

    override fun getDefaultItemStack(): ItemStack? = ItemStack(Items.COBWEB)

    override fun tick() {
        super.tick()

        val player = owner as? PlayerEntity? ?: return
        val distance = distanceTo(player)
        if (player.isDead || distance > ropeLength || player.isSneaking) {
            discard()
        }

        if (isInGround && distance > currentLength) {
            if (world.isClient && player is ClientPlayerEntity) {
                if (player.input.playerInput.jump) {
                    val vec = player.pos.subtract(player.pos).multiply(0.5)
                    player.addVelocity(vec.horizontal)
                }
            }
            val vec = pos.subtract(player.pos).multiply(0.05)
            player.addVelocity(vec.multiply(abs(vec.x) * 2, 1.0, abs(vec.z) * 2))
        }
    }

    private fun onHit() {
        if (owner == null) {
            return
        }
        currentLength = distanceTo(owner).toDouble() * 0.85
        if (!world.isClient) {
            for (entity in (world as ServerWorld).getEntitiesByType(TypeFilter.instanceOf(SwingWebEntity::class.java)) { true }) {
                if (entity.owner == owner && entity != this) {
                    entity.discard()
                }
            }
        }
        owner?.addVelocity(pos.subtract(owner?.pos).multiply(0.1, 0.001, 0.1))
    }

    override fun onEntityHit(entityHitResult: EntityHitResult?) = onHit()

    override fun onBlockHit(blockHitResult: BlockHitResult?) {
        super.onBlockHit(blockHitResult)
        onHit()
    }

    override fun getGravity(): Double = 0.01

    override fun canHit(entity: Entity?): Boolean = entity != owner
}
