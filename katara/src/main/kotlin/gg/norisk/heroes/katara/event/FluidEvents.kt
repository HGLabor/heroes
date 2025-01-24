package gg.norisk.heroes.katara.event

import net.minecraft.fluid.FluidState
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.silkmc.silk.core.event.Cancellable
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventScopeProperty

object FluidEvents {
    val static: BooleanProperty = BooleanProperty.of("static")


    class FluidEvent(val world: World, val blockPos: BlockPos, val fluidState: FluidState) : Cancellable {
        override val isCancelled = EventScopeProperty(false)
    }

    val fluidTickEvent = Event.onlySync<FluidEvent>()
}