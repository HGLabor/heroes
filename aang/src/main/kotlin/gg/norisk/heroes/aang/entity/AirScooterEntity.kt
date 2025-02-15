package gg.norisk.heroes.aang.entity

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.aang.ability.AirBallAbility.getAirBendingPos
import gg.norisk.heroes.aang.ability.AirBallAbility.isAirBending
import gg.norisk.heroes.aang.ability.AirScooterAbility
import gg.norisk.heroes.aang.ability.AirScooterAbility.isAirScooting
import gg.norisk.heroes.aang.ability.LevitationAbility.AIR_LEVITATING_KEY
import gg.norisk.heroes.common.utils.sound
import gg.norisk.utils.Easing
import gg.norisk.utils.OldAnimation
import net.minecraft.entity.*
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.AdvancedExplosionBehavior
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.text.literal
import java.util.*
import java.util.function.Function
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class AirScooterEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world) {
    val startScaleAnimation = OldAnimation(0f, 3f, 1.seconds.toJavaDuration(), Easing.EXPO_OUT)
    var currentScale: Float = 0f
    var wasBended = false
    var isComingBack = false
    val pickedUpEntities = mutableSetOf<UUID>()

    var wasLaunched: Boolean
        get() = this.getSyncedData<Boolean>("AirScooter:WasLaunched") ?: false
        set(value) = this.setSyncedData("AirScooter:WasLaunched", value)

    enum class Type {
        SCOOTER, PROJECTILE
    }

    init {
        this.ignoreCameraFrustum = true
        this.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT)?.baseValue = 2.0
        this.getAttributeInstance(EntityAttributes.GENERIC_SCALE)?.baseValue = 0.0
        this.getAttributeInstance(EntityAttributes.GENERIC_GRAVITY)?.baseValue = 0.02
    }

    // Apply player-controlled movement
    override fun travel(pos: Vec3d) {
        this.setNoDrag(true)
        this.setNoGravity(wasLaunched)
        super.travel(pos)
    }

    override fun canWalkOnFluid(fluidState: FluidState): Boolean {
        return true
    }

    override fun shouldRenderName(): Boolean {
        return false
    }

    override fun onStartedTrackingBy(player: ServerPlayerEntity) {
        super.onStartedTrackingBy(player)
        if (bendingType == Type.SCOOTER) {
            AirScooterAbility.airScooterSoundPacketS2C.send(id, player)
        }
    }

    fun getLerpedScale(f: Float): Float {
        currentScale = MathHelper.lerp(f, currentScale, this.scale)
        return currentScale
    }

    override fun tick() {
        super.tick()
        when (bendingType) {
            Type.SCOOTER -> handleAirScooterType()
            Type.PROJECTILE -> {
                handleProjectileType()
            }
        }
    }

    private fun handleProjectileType() {
        noClip = false

        if (!wasLaunched && !isBoomerang && !world.isClient) {
            val owner = getOwner()
            val targetPos = owner?.getAirBendingPos()
            if (owner != null && targetPos != null) {
                val direction = targetPos.subtract(this.pos).normalize()
                val distance = targetPos.distanceTo(this.pos)

                // Je näher das Projektil am Ziel ist, desto kleiner wird der Multiplikationsfaktor
                val speedMultiplier = distance

                modifyVelocity(direction.multiply(speedMultiplier))
            }
        }

        val player = getOwner()
        if (isBoomerang && player != null && !world.isClient) {
            val distanceToPlayer = this.distanceTo(player)
            pickUpNearbyItems()

            if (isComingBack) {
                modifyVelocity(player.getAirBendingPos().subtract(this.pos).normalize().multiply(2.0))
                if (distanceToPlayer < 4) {
                    isBoomerang = false
                }
            } else if (distanceToPlayer > 25) {
                isComingBack = true
            }
        }

        val distanceFlag = if (player != null) distanceTo(player) > 100 else false

        if (!world.isClient && wasLaunched) {
            if (horizontalCollision || verticalCollision || player == null || distanceFlag) {
                this.discard()
                this.createExplosion(this.blockPos.toCenterPos())
            }
        }
    }

    private fun pickUpNearbyItems() {
        val player = getOwner() ?: return
        world.getOtherEntities(this, this.boundingBox.expand(2.0)) { it is ItemEntity || it is MobEntity }.forEach {
            val direction = this.pos.subtract(it.pos)
            if (it.distanceTo(player) < 8) {
                it.modifyVelocity(direction.normalize().multiply(0.8))
            } else {
                it.modifyVelocity(direction)
            }
            if (!pickedUpEntities.contains(it.uuid)) {
                pickedUpEntities.add(it.uuid)
                it.sound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, Random.nextDouble(1.0, 2.0))
            }
        }
    }

    private fun handleAirScooterType() {
        noClip = true
        this.getAttributeInstance(EntityAttributes.GENERIC_SCALE)?.baseValue = startScaleAnimation.get().toDouble()
        if (getOwner()?.isAirScooting == false) {
            this.discard()
        }
    }

    private fun getOwner(): PlayerEntity? {
        val id = if (ownerId != -1) ownerId else return null
        return world.getEntityById(id) as? PlayerEntity?
    }

    override fun handleFallDamage(f: Float, g: Float, damageSource: DamageSource?): Boolean {
        return false
    }

    override fun damage(damageSource: DamageSource, f: Float): Boolean {
        if (damageSource.isOf(DamageTypes.GENERIC_KILL)) {
            return super.damage(damageSource, f)
        }
        val attacker = damageSource.attacker as? LivingEntity ?: return false
        if (attacker.id == ownerId) {
            if ((attacker as? PlayerEntity?)?.isAirBending == true) return false
            wasLaunched = true
            sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.2f, pitch = 2f)
            setVelocity(attacker, attacker.pitch, attacker.yaw, 0.0f, 2.5f, 1.0f)
            return false
        } else {
            this.discard()
            this.createExplosion(this.pos)
        }
        return false
    }

    fun launchBoomerang() {
        isBoomerang = true
        sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.2f, pitch = 2f)
        val player = getOwner() ?: return
        setVelocity(player, player.pitch, player.yaw, 0.0f, 1.5f, 1.0f)
    }

    var isBoomerang: Boolean
        get() = this.getSyncedData<Boolean>("AirBallIsBoomerang") ?: false
        set(value) {
            if (!value) {
                isComingBack = false
                pickedUpEntities.clear()
            }
            this.setSyncedData("AirBallIsBoomerang", value)
        }

    var ownerId: Int
        get() = this.getSyncedData<Int>("AirBallOwnerId") ?: -1
        set(value) {
            this.setSyncedData("AirBallOwnerId", value)
        }

    var bendingType: Type
        get() = Type.valueOf(this.getSyncedData<String>("BendingType") ?: Type.SCOOTER.name)
        set(value) {
            this.setSyncedData("BendingType", value.name)
        }

    override fun calculateBoundingBox(): Box {
        val f = getDimensions(EntityPose.STANDING).withEyeHeight(0f).width / 2.0f
        val g = getDimensions(EntityPose.STANDING).withEyeHeight(0f).height
        val h = 0.15f * scale
        return Box(
            pos.x - f.toDouble(),
            pos.y - h,
            pos.z - f.toDouble(),
            pos.x + f.toDouble(),
            pos.y - h + g.toDouble(),
            pos.z + f.toDouble()
        )
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

    private fun createExplosion(vec3d: Vec3d) {
        world
            .createExplosion(
                this,
                null,
                AdvancedExplosionBehavior(
                    true,
                    false,
                    Optional.of(1.22f * scale),
                    Registries.BLOCK.getEntryList(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
                ),
                vec3d.getX(),
                vec3d.getY(),
                vec3d.getZ(),
                1.2f * scale,
                false,
                World.ExplosionSourceType.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST
            )
    }

    override fun isCollidable(): Boolean {
        return false
    }

    override fun collidesWith(entity: Entity?): Boolean {
        return false
    }
}
