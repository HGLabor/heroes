package gg.norisk.heroes.common.networking

import gg.norisk.heroes.common.HeroesManager.toId
import kotlinx.serialization.Serializable
import net.silkmc.silk.network.packet.s2cPacket

interface CameraShakeEvent {
    fun isValid(t: Double): Boolean
    fun getCameraShakeMagnitude(t: Double): Double
}

@Serializable
data class BoomShake(private var magnitude: Double, private var sustain: Double, private var fade: Double) :
    CameraShakeEvent {
    override fun isValid(t: Double): Boolean = t < sustain + fade
    override fun getCameraShakeMagnitude(t: Double): Double {
        return when {
            t <= sustain -> magnitude
            else -> magnitude * (1 - (t - sustain) / fade)
        }
    }
}

val cameraShakePacket = s2cPacket<BoomShake>("camera-shake".toId())
