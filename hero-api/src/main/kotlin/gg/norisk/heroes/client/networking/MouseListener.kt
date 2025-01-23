package gg.norisk.heroes.client.networking

import gg.norisk.heroes.common.events.mouseScrollEvent
import gg.norisk.heroes.common.networking.Networking.mousePacket
import gg.norisk.heroes.common.networking.Networking.mouseScrollPacket
import gg.norisk.heroes.common.networking.dto.MouseAction
import gg.norisk.heroes.common.networking.dto.MousePacket
import gg.norisk.heroes.common.networking.dto.MouseType
import gg.norisk.utils.events.MouseEvents.mouseClickEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient

object MouseListener {
    fun initClient() {
        mouseScrollEvent.listen {
            MinecraftClient.getInstance().player ?: return@listen
            mouseScrollPacket.send(it.vertical > 0)
        }
        mouseClickEvent.listen {
            MinecraftClient.getInstance().player ?: return@listen
            if (MinecraftClient.getInstance().options.attackKey.matchesMouse(it.key.code)) {
                mousePacket.send(
                    MousePacket(
                        MouseType.LEFT,
                        if (it.pressed) MouseAction.CLICK else MouseAction.RELEASE
                    )
                )
            } else if (MinecraftClient.getInstance().options.useKey.matchesMouse(it.key.code)) {
                mousePacket.send(
                    MousePacket(
                        MouseType.RIGHT,
                        if (it.pressed) MouseAction.CLICK else MouseAction.RELEASE
                    )
                )
            } else if (MinecraftClient.getInstance().options.pickItemKey.matchesMouse(it.key.code)) {
                mousePacket.send(
                    MousePacket(
                        MouseType.MIDDLE,
                        if (it.pressed) MouseAction.CLICK else MouseAction.RELEASE
                    )
                )
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            MinecraftClient.getInstance().player ?: return@register
            if (MinecraftClient.getInstance().options.attackKey.isPressed) {
                mousePacket.send(MousePacket(MouseType.LEFT, MouseAction.HOLD))
            }
            if (MinecraftClient.getInstance().options.useKey.isPressed) {
                mousePacket.send(MousePacket(MouseType.RIGHT, MouseAction.HOLD))
            }
            if (MinecraftClient.getInstance().options.pickItemKey.isPressed) {
                mousePacket.send(MousePacket(MouseType.MIDDLE, MouseAction.HOLD))
            }
        }
    }
}
