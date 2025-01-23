package gg.norisk.heroes.client.ui

import gg.norisk.heroes.client.ui.screen.HeroSelectorScreen
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.utils.OldAnimation
import me.cortex.nvidium.Nvidium
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration


object OrthoCamera {
    val isEnabled get() = MinecraftClient.getInstance().currentScreen is HeroSelectorScreen
    var yawAnimation = OldAnimation(0f, 360f, 2.minutes.toJavaDuration())

    fun initClient() {
        Networking.s2cHeroSelectorPacket.receiveOnClient { packet, context ->
            if (packet.isActive) {
                openHeroSelectorScreen(packet.heroes.mapNotNull { HeroManager.getHero(it) }, packet)
            } else {
                mcCoroutineTask(sync = true, client = true) {
                    if (MinecraftClient.getInstance().currentScreen is HeroSelectorScreen) {
                        if (FabricLoader.getInstance().isModLoaded("nvidium")) {
                            Nvidium.FORCE_DISABLE = false
                            MinecraftClient.getInstance()?.worldRenderer?.reload()
                        }
                        MinecraftClient.getInstance().setScreen(null)
                    }
                }
            }
        }
    }

    fun openHeroSelectorScreen(
        heroes: List<Hero<*>> = HeroManager.registeredHeroes.values.toList(),
        packet: HeroSelectorPacket
    ) {
        mcCoroutineTask(delay = 1.ticks, sync = true, client = true) {
            MinecraftClient.getInstance().setScreen(HeroSelectorScreen(heroes, packet.isKitEditorEnabled))
        }
    }

    fun createOrthoMatrix(delta: Float, minScale: Float): Matrix4f {
        val client: MinecraftClient = MinecraftClient.getInstance()
        val scale = 100f
        val width = max(minScale, scale * client.window.framebufferWidth / client.window.framebufferHeight)
        val height = max(minScale, scale)
        return Matrix4f().setOrtho(
            -width, width,
            -height, height,
            -1000.0F, 1000.0f
        )
    }

    fun handlePitch(quaternion: Quaternionf, tickDelta: Float): Float {
        return 30f * MathHelper.RADIANS_PER_DEGREE
    }

    fun handleYaw(quaternion: Quaternionf, tickDelta: Float): Float {
        if (yawAnimation.isDone) {
            yawAnimation.reset()
        }
        return yawAnimation.get() * MathHelper.RADIANS_PER_DEGREE
    }
}
