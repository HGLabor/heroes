package gg.norisk.heroes.server.animations

import gg.norisk.heroes.common.animations.AnimationPacket
import gg.norisk.heroes.common.animations.IAnimationManager
import gg.norisk.heroes.common.networking.Networking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

object AnimationManagerServer: IAnimationManager {
    override fun init() {
        Networking.c2sAnimationStart.receiveOnServer { packet, context ->
            if (context.player.uuid != packet.playerUuid) return@receiveOnServer
            Networking.s2cAnimationStart.sendToAll(packet)
        }
    }

    override fun playAnimation(player: PlayerEntity, animation: Identifier) {
        val packet = AnimationPacket(player.uuid, animation)
        Networking.s2cAnimationStart.sendToAll(packet)
    }

    override fun resetAnimation(player: PlayerEntity) {

    }
}
