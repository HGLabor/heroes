package gg.norisk.heroes.toph.entity

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.toVector
import gg.norisk.heroes.toph.ability.earthPushDamage
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.BlockStateParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import net.silkmc.silk.core.entity.modifyVelocity
import org.joml.Vector3f
import java.util.*

class BendingBlockEntity(world: World, x: Double, y: Double, z: Double, blockState: BlockState) :
    FallingBlockEntity(EntityType.FALLING_BLOCK, world) {
    var canAttack: Boolean = false


    companion object {
        val NULL = Vector3f()

        private val OWNER_KEY = "BendingBlock:Owner"
        private val TARGET_KEY = "BendingBlock:TargetPos"
        var FallingBlockEntity.owner: UUID?
            get() = this.getSyncedData<UUID>(OWNER_KEY)
            set(value) = this.setSyncedData(OWNER_KEY, value)

        var FallingBlockEntity.targetPos: Vector3f
            get() = this.getSyncedData<Vector3f>(TARGET_KEY) ?: NULL
            set(value) = this.setSyncedData(TARGET_KEY, value)
    }


    init {
        block = blockState
        intersectionChecked = true
        setPosition(x, y, z)
        velocity = Vec3d.ZERO
        prevX = x
        prevY = y
        prevZ = z
    }

    override fun isImmuneToExplosion(explosion: Explosion): Boolean {
        return true
    }

    override fun isCollidable(): Boolean {
        return true
    }

    fun forcePush(player: PlayerEntity) {
        val target = player.raycast(64.0, 0f, false)
        targetPos = target.pos.toVector3f()
        canAttack = true

        var direction = targetPos.toVector().subtract(pos)
        direction = direction.normalize().multiply(2.0)

        modifyVelocity(direction)
    }


    override fun tick() {
        if (!world.isClient) {
            if (block.isAir) {
                discard()
                return
            }
            val block = block.block
            ++timeFalling
            if (!hasNoGravity()) {
                velocity = velocity.add(0.0, -0.04, 0.0)
            }
            move(MovementType.SELF, velocity)


            velocity = velocity.multiply(0.98)

            if (pos.distanceTo(targetPos.toVector()) <= 1) {
                targetPos = NULL
                velocity = velocity.multiply(0.01)
            }

            if (velocity.y <= 0) {
                setNoGravity(true)
            }

            if (world is ServerWorld && (velocity.lengthSquared() > 0.1)) {
                (world as ServerWorld).spawnParticles(
                    BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                    this.x,
                    this.y,
                    this.z,
                    if (canAttack) 7 else 2,
                    (this.width / 4.0f).toDouble(),
                    (this.height / 4.0f).toDouble(),
                    (this.width / 4.0f).toDouble(),
                    0.05
                )
            }

            if (canAttack) {
                explode(horizontalCollision)
            }
        }
    }

    private fun explode(force: Boolean = false) {
        var flag = force
        val owner = world.getPlayerByUuid(owner ?: return) ?: return
        for (enemy in this.world.getEntitiesByClass(
            LivingEntity::class.java,
            this.boundingBox.expand(1.1)
        ) {
            it.isAlive && it.uuid != this.owner
        }) {
            flag = true
            enemy.damage(this.damageSources.playerAttack(owner), earthPushDamage.getValue(owner.uuid).toFloat())
        }

        if (flag) {
            world.playSound(null, blockPos, SoundRegistry.EARTH_ARMOR, SoundCategory.BLOCKS, 1f, 1f)
            this.discard()
            repeat(10) {
                (world as ServerWorld).spawnParticles(
                    BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                    this.x,
                    this.y,
                    this.z,
                    if (canAttack) 7 else 2,
                    (this.width / 4.0f).toDouble(),
                    (this.height / 4.0f).toDouble(),
                    (this.width / 4.0f).toDouble(),
                    0.05
                )
            }
            (world as? ServerWorld?)?.spawnParticles(
                ParticleRegistry.EARTH_DUST,
                this.x,
                this.y,
                this.z,
                7,
                (this.width / 4.0f).toDouble(),
                (this.height / 4.0f).toDouble(),
                (this.width / 4.0f).toDouble(),
                (0.01..0.04).random()
            )
        }
    }
}
