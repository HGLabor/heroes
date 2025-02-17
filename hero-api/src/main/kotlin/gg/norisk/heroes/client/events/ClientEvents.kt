package gg.norisk.heroes.client.events

import net.silkmc.silk.core.event.Cancellable
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventScopeProperty

object ClientEvents {
    data class CameraClipToSpaceEvent(var value: Double)

    val cameraClipToSpaceEvent = Event.onlySync<CameraClipToSpaceEvent>()

    class PreHotbarScrollEvent: Cancellable {
        override val isCancelled: EventScopeProperty<Boolean> = EventScopeProperty(false)
    }

    val preHotbarScrollEvent = Event.onlySync<PreHotbarScrollEvent>()
}
