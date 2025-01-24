package gg.norisk.ffa.server.event

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.silkmc.silk.core.event.Event

object FFAEvents {
    open class EntityKilledOtherEntityEvent(val killer: Entity, val killed: Entity, val source: DamageSource)

    val entityKilledOtherEntityEvent = Event.onlySync<EntityKilledOtherEntityEvent>()
}