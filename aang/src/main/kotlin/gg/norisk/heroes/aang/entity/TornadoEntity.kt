package gg.norisk.heroes.aang.entity

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.aang.ability.TornadoAbility
import gg.norisk.heroes.aang.ability.TornadoAbility.isTornadoMode
import gg.norisk.heroes.aang.registry.EntityRegistry
import gg.norisk.heroes.aang.utils.PlayerRotationTracker
import gg.norisk.heroes.common.utils.SphereUtils
import gg.norisk.heroes.common.utils.sound
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

class TornadoEntity(entityType: EntityType<out PathAwareEntity>, world: World) :
    PathAwareEntity(entityType, world) {
    var currentScale: Float = 0f
    var rotationTracker: PlayerRotationTracker? = PlayerRotationTracker()

    init {
        this.getAttributeInstance(EntityAttributes.STEP_HEIGHT)?.baseValue = 2.5
    }

    fun getLerpedScale(f: Float): Float {
        currentScale = MathHelper.lerp(f * 0.05f, currentScale, this.scale)
        return currentScale
    }

    // Apply player-controlled movement
    override fun travel(pos: Vec3d) {
        if (!this.isAlive) return
        if (this.hasPassengers()) {
            val passenger = controllingPassenger ?: return super.travel(pos)
            this.prevYaw = yaw
            this.prevPitch = pitch

            yaw = passenger.yaw
            pitch = passenger.pitch * 0.5f
            setRotation(yaw, pitch)

            this.bodyYaw = this.yaw
            this.headYaw = this.bodyYaw
            val x = passenger.sidewaysSpeed * 0.5f
            val z = 0.6f

            this.movementSpeed = 0.3f
            super.travel(Vec3d(x.toDouble(), pos.y, z.toDouble()))
        } else {
            super.travel(pos)
        }
    }

    fun disappear(entity: Entity?) {
        val player = entity as? PlayerEntity?
        if (player?.isTornadoMode == true) {
            TornadoAbility.Ability.addCooldown(player)
            player.isTornadoMode = false
            player.sound(SoundEvents.ENTITY_BREEZE_IDLE_AIR, 0.4f, 0.7f)
        }
        this.discard()
    }

    override fun removePassenger(entity: Entity?) {
        super.removePassenger(entity)
        disappear(entity)
    }

    override fun getControllingPassenger(): LivingEntity? {
        return if (firstPassenger?.id == ownerId) {
            firstPassenger as? LivingEntity?
        } else {
            null
        }
    }

    override fun onStartedTrackingBy(player: ServerPlayerEntity) {
        super.onStartedTrackingBy(player)
        TornadoAbility.tornadoSoundPacketS2C.send(id, player)
    }

    override fun tick() {
        super.tick()
        rotationTracker?.update(world.getEntityById(ownerId) as? PlayerEntity?)
        if (!world.isClient) {
            serverTick()
        }
    }

    override fun tickMovement() {
        super.tickMovement()
        if (this.world.isClient) return

        val radius = this.scale.toDouble()
        val windStrength = 0.1 * this.scale

        for (entity in this.world.getOtherEntities(this, this.boundingBox.expand(radius)) {
            !it.isSpectator && it !is PlayerEntity || (it is PlayerEntity && !it.isCreative)
        }) {
            if (entity.id == ownerId) continue
            if (entity.type == EntityRegistry.TORNADO) continue
            // Berechnung des Fortschritts nach oben (Höhe)
            val maxY = this.eyeY

            // Tornado-Zentrum auf der Höhe des Entitys
            val effectiveCentre = pos.add(0.0, entity.y - pos.y, 0.0)

            // Entfernung vom Tornado-Zentrum
            val distFromCentre = entity.pos.distanceTo(effectiveCentre)

            // Windstärke basierend auf Entfernung vom Zentrum
            val strength = windStrength / distFromCentre

            // Berechnung der Einwärtsbewegung
            val inwardStrength = min((0.01 + world.random.nextDouble() * 0.5) / radius, strength)

            // Richtung des Tornado-Sogs zur Mitte
            val inwardXDir = pos.x - entity.x
            val inwardZDir = pos.z - entity.z
            val inwardPullX = sign(inwardXDir) * inwardXDir.absoluteValue.coerceAtLeast(radius.toDouble())
            val inwardPullZ = sign(inwardZDir) * inwardZDir.absoluteValue.coerceAtLeast(radius.toDouble())

            // Spiralförmige Bewegung nach oben und Rotation um das Zentrum
            val spiralMovement = effectiveCentre.subtract(entity.pos)
                .normalize()
                .crossProduct(Vec3d(0.0, 1.0, 0.0)) // Rotation um die Y-Achse
                .multiply(strength)
                .add(
                    Vec3d(
                        inwardPullX * inwardStrength,
                        strength * world.random.nextDouble(),
                        inwardPullZ * inwardStrength
                    )
                )

            // Setze die neue Bewegung des Entities
            entity.modifyVelocity(spiralMovement.x, spiralMovement.y, spiralMovement.z)

            // Wenn das Entity die maximale Höhe erreicht hat, fliegt es davon
            if (entity.y >= maxY) {
                val flyAwayDirection = Vec3d(
                    world.random.nextDouble() - 0.5,
                    world.random.nextDouble() * 0.5,
                    world.random.nextDouble() - 0.5
                ).normalize().multiply(1.3) // Geschwindigkeit des Wegfliegens
                entity.modifyVelocity(flyAwayDirection)
            }
        }

        SphereUtils.generateSphere(this.blockPos, 3 + radius.toInt(), false).filter { pos ->
            val blockState = world.getBlockState(pos)
            if (blockState.isAir) return@filter false
            if (Direction.values().any { world.getBlockState(pos.offset(it)).isAir }) {
                return@filter true
            }
            return@filter false
        }.shuffled().take(1).forEach {
            val spawnFromBlock = FallingBlockEntity.spawnFromBlock(world, it, world.getBlockState(it))
            spawnFromBlock.modifyVelocity(0.0, .2, .0)
            spawnFromBlock.dropItem = false
        }
    }

    override fun canWalkOnFluid(fluidState: FluidState): Boolean {
        return true
    }

    private fun serverTick() {
        val owner = (world as ServerWorld).getEntityById(ownerId) as? PlayerEntity? ?: return
        val rotationTracker = this.rotationTracker
        if (rotationTracker != null) {
            this.getAttributeInstance(EntityAttributes.SCALE)?.baseValue =
                rotationTracker.getPercentageBetween(1f, 10f).toDouble()
        }
    }

    var ownerId: Int
        get() = this.getSyncedData<Int>("TornadoOwnerId") ?: -1
        set(value) = this.setSyncedData("TornadoOwnerId", value)

    var isGrowingMode: Boolean
        get() = this.getSyncedData<Boolean>("TornadoIsGrowingMode") ?: false
        set(value) = this.setSyncedData("TornadoIsGrowingMode", value)
}
