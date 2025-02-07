package gg.norisk.heroes.common.events

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.chunk.SectionBuilder
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.silkmc.silk.core.event.Cancellable
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventScopeProperty

object EntityEvents {
    class EntityTrackedDataSetEvent(val entity: Entity, val data: TrackedData<*>)
    open class LivingEntityEvent(val livingEntity: LivingEntity)
    class InitDataTrackerEvent(livingEntity: LivingEntity, val dataTracker: DataTracker) :
        LivingEntityEvent(livingEntity)

    open class EntityRendererEvent(
        val entity: Entity,
        val f: Float,
        val g: Float,
        val matrixStack: MatrixStack,
        val vertexConsumerProvider: VertexConsumerProvider,
        val light: Int
    ) : Cancellable {
        override val isCancelled = EventScopeProperty(false)
    }

    open class ComputeFallDamageEvent(
        val fallDistance: Float,
        val damageMultiplier: Float,
        val originalFallDamage: Int,
        livingEntity: LivingEntity
    ) : LivingEntityEvent(
        livingEntity
    ) {
        var fallDamage: Int? = null
    }

    val computeFallDamageEvent = Event.onlySync<ComputeFallDamageEvent>()
    val onTrackedDataSetEvent = Event.onlySync<EntityTrackedDataSetEvent>()
    val entityRendererEvent = Event.onlySync<EntityRendererEvent>()
    val livingEntityTickMovementEvent = Event.onlySync<LivingEntityEvent>()
}
