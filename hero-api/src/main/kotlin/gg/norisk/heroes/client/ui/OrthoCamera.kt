package gg.norisk.heroes.client.ui

import gg.norisk.heroes.client.ui.screen.HeroSelectorScreen
import gg.norisk.heroes.client.ui.skilltree.HeroSelectorScreenV2
import gg.norisk.heroes.client.ui.skilltree.SkillTreeScreen
import gg.norisk.heroes.common.hero.Hero
import gg.norisk.heroes.common.hero.HeroManager
import gg.norisk.heroes.common.networking.Networking
import gg.norisk.heroes.common.networking.dto.HeroSelectorPacket
import gg.norisk.utils.OldAnimation
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration


object OrthoCamera {
    val isEnabled get() = MinecraftClient.getInstance().currentScreen is HeroSelectorScreen || MinecraftClient.getInstance().currentScreen is HeroSelectorScreenV2
    var yawAnimation = OldAnimation(0f, 360f, 2.minutes.toJavaDuration())

    fun initClient() {
        Networking.s2cHeroSelectorPacket.receiveOnClient { packet, context ->
            if (packet.isActive) {
                openHeroSelectorScreen(packet.heroes.mapNotNull { HeroManager.getHero(it) }, packet)
            } else {
                mcCoroutineTask(sync = true, client = true) {
                    if (isEnabled) {
                        if (FabricLoader.getInstance().isModLoaded("nvidium")) {
                            //me.cortex.nvidium.Nvidium.FORCE_DISABLE = false
                            MinecraftClient.getInstance()?.worldRenderer?.reload()
                        }
                        MinecraftClient.getInstance().setScreen(null)
                    }
                }
            }
        }

        clientCommand("heroselector") {
            runs {
                mcCoroutineTask(delay = 1.ticks, sync = true, client = true) {
                    MinecraftClient.getInstance().setScreen(HeroSelectorScreen(HeroManager.registeredHeroes.values.toList(), false))
                }
            }
        }

        clientCommand("skilltree") {
            runs {
                mcCoroutineTask(sync = true, client = true, delay = 1.ticks) {
                    MinecraftClient.getInstance().setScreen(SkillTreeScreen())
                }
            }
        }
    }

    fun openHeroSelectorScreen(
        heroes: List<Hero> = HeroManager.registeredHeroes.values.toList(),
        packet: HeroSelectorPacket
    ) {
        mcCoroutineTask(delay = 1.ticks, sync = true, client = true) {
            MinecraftClient.getInstance().setScreen(HeroSelectorScreenV2(heroes, packet.isKitEditorEnabled))
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
