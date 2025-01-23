package gg.norisk.heroes.client.events

import net.silkmc.silk.core.event.Event

object ClientEvents {
    data class CameraClipToSpaceEvent(var value: Double)

    val cameraClipToSpaceEvent = Event.onlySync<CameraClipToSpaceEvent>()
}
