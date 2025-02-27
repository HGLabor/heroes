package gg.norisk.heroes.common.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate
import kotlin.math.cos
import kotlin.math.sin


/**
 * Credits to [Source](https://github.com/vini2003/Maven)
 */
object RaycastUtils {
    fun raycastEntity(source: PlayerEntity, delta: Float, maxDist: Float): Entity? {
        val pos = source.getCameraPosVec(delta)
        val rot = source.getCameraPosVec(1.0f)
        val end = pos.add(rot.x * maxDist.toDouble(), rot.y * maxDist.toDouble(), rot.z * maxDist.toDouble())
        val sourceBox = source.boundingBox.stretch(rot.multiply(maxDist.toDouble())).expand(2.5, 2.5, 2.5)
        val entityHitResult = raycastEntity(
            source,
            pos,
            end,
            sourceBox,
            { entity: Entity -> !entity.isSpectator && entity.canHit() },
            (maxDist * maxDist).toDouble()
        )
        return entityHitResult?.entity
    }

    fun raycastEntity(entity: Entity, i: Int): Optional<Entity> {
        return run {
            val vec3d = entity.eyePos
            val vec3d2 = entity.getRotationVec(1.0f).multiply(i.toDouble())
            val vec3d3 = vec3d.add(vec3d2)
            val box = entity.boundingBox.stretch(vec3d2).expand(1.0)
            val j = i * i
            val predicate =
                Predicate { entityx: Entity -> !entityx.isSpectator && entityx.canHit() }
            val entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, predicate, j.toDouble())
            if (entityHitResult == null) {
                Optional.empty()
            } else {
                if (vec3d.squaredDistanceTo(entityHitResult.pos) > j.toDouble()) Optional.empty() else Optional.of(
                    entityHitResult.entity
                )
            }
        }
    }

    fun clipWithDistance(player: PlayerEntity, level: World, clipDistance: Double, fluidHandling: FluidHandling = FluidHandling.NONE): BlockHitResult? {
        val vecX = sin(-player.yaw * (Math.PI / 180.0) - Math.PI) * -cos(-player.pitch * (Math.PI / 180.0))
        val vecY = sin(-player.pitch * (Math.PI / 180.0))
        val vecZ = cos(-player.yaw * (Math.PI / 180.0) - Math.PI) * -cos(-player.pitch * (Math.PI / 180.0))
        return level.raycast(
            RaycastContext(
                player.eyePos,
                player.eyePos.add(vecX * clipDistance, vecY * clipDistance, vecZ * clipDistance),
                RaycastContext.ShapeType.OUTLINE,
                fluidHandling,
                player
            )
        )
    }

    fun raycastEntity(
        source: PlayerEntity,
        pos: Vec3d,
        end: Vec3d?,
        box: Box?,
        predicate: Predicate<Entity>?,
        sqrMaxDist: Double
    ): EntityHitResult? {
        val world = source.world
        var distToGo = sqrMaxDist
        var target: Entity? = null
        var targetPos: Vec3d? = null
        for (entity in world.getOtherEntities(source, box, predicate)) {
            val entityBox = entity.boundingBox.expand(entity.targetingMargin.toDouble())
            val optPos = entityBox.raycast(pos, end)
            if (entityBox.contains(pos)) {
                if (distToGo >= 0.0) {
                    target = entity
                    targetPos = optPos.orElse(pos)
                    distToGo = 0.0
                }
            } else if (optPos.isPresent) {
                val entityPos = optPos.get()
                val sqrDist = pos.squaredDistanceTo(entityPos)
                if (sqrDist < distToGo || distToGo == 0.0) {
                    if (entity.rootVehicle === source.rootVehicle) {
                        if (distToGo == 0.0) {
                            target = entity
                            targetPos = entityPos
                        }
                    } else {
                        target = entity
                        targetPos = entityPos
                        distToGo = sqrDist
                    }
                }
            }
        }
        return target?.let { EntityHitResult(it, targetPos) }
    }
}
