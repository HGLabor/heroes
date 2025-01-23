package gg.norisk.heroes.common.events

import net.minecraft.client.input.Input
import net.silkmc.silk.core.event.Event

open class AfterTickInputEvent(val input: Input)
open class MouseScrollEvent(val window: Long, val horizontal: Double, val vertical: Double)

val mouseScrollEvent = Event.onlySync<MouseScrollEvent>()

val afterTickInputEvent = Event.onlySync<AfterTickInputEvent>()

