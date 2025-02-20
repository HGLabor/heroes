package gg.norisk.heroes.katara.entity

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.common.utils.toBlockPos
import gg.norisk.heroes.common.utils.toVec
import gg.norisk.heroes.katara.ability.HealingAbility.WaterRender
import gg.norisk.heroes.katara.ability.HealingAbility.counter
import gg.norisk.heroes.katara.ability.HealingAbility.getWaterBendingPos
import gg.norisk.heroes.katara.ability.HealingAbility.handleWaterHealing
import gg.norisk.heroes.katara.ability.WaterBendingAbility.waterBendingDistance
import gg.norisk.heroes.katara.ability.WaterFormingAbility.firstWaterFormingPos
import gg.norisk.heroes.katara.ability.WaterFormingAbility.secondWaterFormingPos
import gg.norisk.utils.Easing
import gg.norisk.utils.OldAnimation
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class WaterBendingEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world) {
    init {
        //this.ignoreCameraFrustum = true
        //this.getAttributeInstance(EntityAttributes.GRAVITY)?.baseValue = 0.02
    }

    var positions: MutableList<WaterRender> = mutableListOf<WaterRender>()
    var wasLaunched: Boolean
        get() = this.getSyncedData<Boolean>("WaterBendingEntityWasLaunched") ?: false
        set(value) {
            this.setSyncedData("WaterBendingEntityWasLaunched", value)
        }
    var isGettingRemoved = false
    var wasDropped: Boolean
        get() = this.getSyncedData<Boolean>("WaterBendingEntityWasDropped") ?: false
        set(value) {
            this.setSyncedData("WaterBendingEntityWasDropped", value)
        }

    var isInitial: Boolean
        get() = this.getSyncedData<Boolean>("WaterBendingIsInitial") ?: false
        set(value) {
            this.setSyncedData("WaterBendingIsInitial", value)
        }
    var initialCounter = 0;

    var isHealing: Boolean
        get() = this.getSyncedData<Boolean>("WaterBendingIsHealing") ?: false
        set(value) {
            this.setSyncedData("WaterBendingIsHealing", value)
        }

    var ownerId: Int
        get() = this.getSyncedData<Int>("WaterBendingEntityOwnerId") ?: -1
        set(value) {
            this.setSyncedData("WaterBendingEntityOwnerId", value)
        }

    fun getOwner(): PlayerEntity? {
        val id = if (ownerId != -1) ownerId else return null
        return world.getEntityById(id) as? PlayerEntity?
    }

    override fun tick() {
        super.tick()
        val owner = getOwner()


        if (!world.isClient) {
            if ((wasLaunched || wasDropped) && (horizontalCollision || verticalCollision || isTouchingWater || world.getOtherEntities(
                    this,
                    this.boundingBox
                ) { it.isAlive && !it.isSpectator }.isNotEmpty()) && !isGettingRemoved
            ) {
                isGettingRemoved = true

                if (isHealing) {
                    for (otherEntity in world.getOtherEntities(
                        this,
                        boundingBox.expand(2.0)
                    ) { it.isAlive && !it.isSpectator }) {
                        if (owner != null) {
                            otherEntity.handleWaterHealing(owner)
                        }
                    }
                } else {
                    for (otherEntity in world.getOtherEntities(
                        this,
                        boundingBox.expand(2.89)
                    ) { it.isAlive && !it.isSpectator && it.canHit() && it != owner && it !is WaterBendingEntity }) {
                        otherEntity.damage(world as ServerWorld, this.damageSources.playerAttack(owner), 4f)
                        (otherEntity as? LivingEntity?)?.takeKnockback(1.1, Random.nextDouble(), Random.nextDouble())
                    }
                }

                if (wasDropped) {
                    val pos = blockPos
                    world.setBlockState(pos, Fluids.WATER.getStill(false).blockState)
                    mcCoroutineTask(sync = true, delay = 6.ticks) {
                        if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                            world.setBlockState(pos, Blocks.AIR.defaultState)
                        }
                    }
                }

                repeat(20) {
                    (world as ServerWorld).spawnParticles(
                        ParticleTypes.SPLASH,
                        getParticleX(0.5) + Random.nextDouble(-1.0, 1.0),
                        randomBodyY + Random.nextDouble(-1.0, 1.0),
                        getParticleZ(0.5) + Random.nextDouble(-1.0, 1.0),
                        50,
                        0.001,
                        0.001,
                        0.001,
                        0.0
                    )
                }
                world.playSound(
                    null,
                    pos.x,
                    pos.y,
                    pos.z,
                    SoundEvents.ENTITY_GENERIC_SPLASH,
                    SoundCategory.BLOCKS,
                    1f, Random.nextDouble(1.5, 2.0).toFloat(),
                )
                mcCoroutineTask(delay = 2.ticks, sync = true) {
                    discard()
                }
            }
            sound(
                SoundEvents.ENTITY_BOAT_PADDLE_WATER,
                0.1f,
                Random.nextDouble(1.9, 2.0).toFloat(),
            )
        }

        if (isInitial && owner != null) {
            val distanceToPlayer = this.squaredDistanceTo(owner)
            val distance = owner.waterBendingDistance

            if (distanceToPlayer >= (distance * distance) + sqrt(distance) * 2) {
                modifyVelocity(owner.getWaterBendingPos(distance).subtract(this.pos).normalize().multiply(1.0))
            } else {
                initialCounter++
                if (initialCounter >= 5) {
                    isInitial = false
                }
            }
        }

        if (!(wasLaunched || wasDropped) && !isInitial) {
            owner?.apply {
                val waterFormingPos = owner.secondWaterFormingPos ?: owner.firstWaterFormingPos
                if (waterFormingPos != null) {
                    this@WaterBendingEntity.modifyVelocity(
                        waterFormingPos.toVec().subtract(this@WaterBendingEntity.pos).normalize().multiply(1.0)
                    )
                } else {
                    this@WaterBendingEntity.setPosition(this.getWaterBendingPos())
                }
            }
        }

        tickTrail(owner)
    }

    fun freeze() {
        for (position in positions.takeLast(10)) {
            val pos = position.pos.toBlockPos()
            val currentState = world.getBlockState(pos)
            if (world.getBlockState(pos).isOf(Blocks.WATER) || world.getBlockState(pos).isAir) {
                world.setBlockState(position.pos.toBlockPos(), Blocks.ICE.defaultState)
                (world as? ServerWorld?)?.spawnParticles(
                    ParticleTypes.CLOUD,
                    pos.x + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    pos.y + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    pos.z + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
                )
            }
        }
        discard()
    }

    fun tickTrail(owner: PlayerEntity?, tickDelta: Float = 1f) {
        if (owner != null || wasLaunched || wasDropped) {
            //println("Was Launched $wasLaunched")
            if (positions.size > 30) {
                positions.removeFirstOrNull()
                /* val first = positions.getOrNull(0)
                 if (first?.animation?.end != 0f) {
                     first?.animation = OldAnimation(1f, 0.0f, 0.1.seconds.toJavaDuration())
                 }
                 if (first?.animation?.isDone == true) {
                 } */
            }

            val lastPos = positions.lastOrNull()

            val pos = if (wasLaunched || wasDropped || isInitial || (owner?.secondWaterFormingPos
                    ?: owner?.firstWaterFormingPos) != null
            ) getLerpedPos(
                tickDelta
            ) else owner!!.getWaterBendingPos()

            //TODO der check könnte für probleme sorgen
            for (otherEntity in world.getOtherEntities(owner, this.boundingBox.expand(1.5)) {
                it !is PlayerEntity && it !is WaterBendingEntity
            }) {
                val distance = otherEntity.distanceTo(this)
                val speed = if (distance <= 0.43) {
                    0.05
                } else {
                    0.2
                }
                otherEntity.modifyVelocity(this.pos.subtract(otherEntity.pos).normalize().multiply(speed))

                /*otherEntity.teleport(
                    otherEntity.world as ServerWorld,
                    this.x,
                    this.y,
                    this.z,
                    PositionFlag.VALUES,
                    otherEntity.yaw,
                    otherEntity.pitch
                )*/
            }

            //println("${lastPos?.distanceTo(pos)}")
            if ((lastPos?.pos?.distanceTo(pos) ?: 10000.0) >= 0.2) {
                positions += WaterRender(
                    pos,
                    OldAnimation(0.7f, 1f, 0.1.seconds.toJavaDuration(), Easing.LINEAR),
                    counter++
                )
                if (world.isClient) {
                    world.addParticle(
                        ParticleTypes.SPLASH,
                        pos.x,
                        pos.y,
                        pos.z,
                        0.0,
                        0.0,
                        0.0
                    )
                } else {
                    world.playSound(
                        null,
                        pos.x,
                        pos.y,
                        pos.z,
                        SoundEvents.ENTITY_BOAT_PADDLE_WATER,
                        SoundCategory.BLOCKS,
                        0.3f, Random.nextDouble(1.5, 2.0).toFloat(),
                    )
                }
            }
        }
    }

    // Apply player-controlled movement
    override fun travel(pos: Vec3d) {
        if (!wasDropped) {
            this.setNoDrag(true)
            this.setNoGravity(true)
        }
        super.travel(pos)
    }

    fun launch(owner: LivingEntity) {
        if (!wasLaunched && !isInitial && !wasDropped) {
            wasLaunched = true
            owner.sound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.6f, Random.nextDouble(1.5, 2.0))
            sound(SoundEvents.ENTITY_GENERIC_SPLASH, 1f, Random.nextDouble(1.5, 2.0))
            setVelocity(owner, owner.pitch, owner.yaw, 0.0f, 2f, 1.0f)
        }
    }

    fun drop(owner: LivingEntity) {
        if (!wasDropped && !isInitial) {
            wasDropped = true
            owner.sound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.6f, Random.nextDouble(1.5, 2.0))
            sound(SoundEvents.ENTITY_GENERIC_SPLASH, 1f, Random.nextDouble(1.5, 2.0))
            modifyVelocity(Vec3d(0.0, -0.6, 0.0))
        }
    }

    override fun damage(world: ServerWorld, damageSource: DamageSource, f: Float): Boolean {
        if (damageSource.isOf(DamageTypes.GENERIC_KILL)) {
            return super.damage(world, damageSource, f)
        }
        val attacker = damageSource.attacker as? LivingEntity ?: return false
        if (attacker.id == ownerId) {
            launch(attacker)
            return false
        } else {
            this.discard()
            //TODO
        }
        return false
    }

    fun calculateVelocity(d: Double, e: Double, f: Double, g: Float, h: Float): Vec3d {
        return Vec3d(d, e, f)
            .normalize()
            .add(
                random.nextTriangular(0.0, 0.0172275 * h.toDouble()),
                random.nextTriangular(0.0, 0.0172275 * h.toDouble()),
                random.nextTriangular(0.0, 0.0172275 * h.toDouble())
            )
            .multiply(g.toDouble())
    }

    fun setVelocity(entity: Entity, f: Float, g: Float, h: Float, i: Float, j: Float) {
        val k = -MathHelper.sin(g * (Math.PI / 180.0).toFloat()) * MathHelper.cos(f * (Math.PI / 180.0).toFloat())
        val l = -MathHelper.sin((f + h) * (Math.PI / 180.0).toFloat())
        val m = MathHelper.cos(g * (Math.PI / 180.0).toFloat()) * MathHelper.cos(f * (Math.PI / 180.0).toFloat())
        this.setVelocity(k.toDouble(), l.toDouble(), m.toDouble(), i, j)
        val vec3d = entity.movement
        this.velocity = velocity.add(vec3d.x, if (entity.isOnGround) 0.0 else vec3d.y, vec3d.z)
    }

    fun setVelocity(d: Double, e: Double, f: Double, g: Float, h: Float) {
        val vec3d: Vec3d = this.calculateVelocity(d, e, f, g, h)
        this.velocity = vec3d
        this.velocityDirty = true
        val i = vec3d.horizontalLength()
        this.yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 180.0f / Math.PI.toFloat()).toFloat()
        this.pitch = (MathHelper.atan2(vec3d.y, i) * 180.0f / Math.PI.toFloat()).toFloat()
        this.prevYaw = this.yaw
        this.prevPitch = this.pitch
    }

    override fun handleFallDamage(f: Float, g: Float, damageSource: DamageSource?): Boolean {
        return false
    }
}