package gg.norisk.heroes.client.animations

import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry
import gg.norisk.heroes.common.player.IAnimatedPlayer
import gg.norisk.heroes.common.animations.IAnimationManager
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.player.NamedKeyframeAnimationPlayer
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier

object AnimationManagerClient: IAnimationManager {
    override fun init() {
        Networking.s2cAnimationStart.receiveOnClient { packet, _ ->
            val worldPlayers = MinecraftClient.getInstance().world?.players ?: return@receiveOnClient
            val player = worldPlayers.firstOrNull {
                it.uuid == packet.playerUuid
            } ?: return@receiveOnClient
            playAnimation(player, packet.animation)
        }
    }

    override fun playAnimation(player: PlayerEntity, animation: Identifier) {
        val animationContainer = (player as IAnimatedPlayer).hero_getModAnimation()
        var anim = PlayerAnimationRegistry.getAnimation(animation)
            ?: error("No animation found for: ${animation}")

        //TODO das als Packet mitsenden
        val builder = anim.mutableCopy()
        //builder.getPart("leftLeg")?.isEnabled = false
        //builder.getPart("rightLeg")?.isEnabled = false
        //builder.getPart("head")?.isEnabled = false
        anim = builder.build()

        animationContainer.animation = NamedKeyframeAnimationPlayer(animation.path, anim)
    }

    override fun resetAnimation(player: PlayerEntity) {
        val animationContainer = (player as IAnimatedPlayer).hero_getModAnimation()
        animationContainer.animation = null
    }
}
