package gg.norisk.heroes.aang.utils

import net.minecraft.entity.Entity
import java.util.*
import kotlin.math.abs

class EntitySpinTracker {
    private val yawHistory: Deque<Float> = ArrayDeque()
    private val maxHistorySize = 60 // Anzahl der Ticks, die wir überwachen (z. B. 1 Sekunde bei 20 Ticks pro Sekunde)
    private val spinThreshold = 360.0f // Mindestens 720° Änderung für einen "wilden Spin" (z. B. 2 volle Umdrehungen)

    fun update(entity: Entity) {
        // Aktuelle Yaw-Rotation der Entity holen
        val currentYaw = normalizeYaw(entity.yaw)

        // Letzten Wert speichern
        if (yawHistory.size >= maxHistorySize) {
            yawHistory.pollFirst()
        }
        yawHistory.addLast(currentYaw)

        // Optional: Debug-Log für Rotation
    }

    fun clear() {
        yawHistory.clear()
    }

    fun getSpinProgress(): Float {
        if (yawHistory.size < 2) {
            return 0.0f // Nicht genug Daten
        }

        var totalChange = 0.0f
        var previousYaw: Float? = null

        for (yaw in yawHistory) {
            if (previousYaw != null) {
                val delta = calculateYawDifference(previousYaw, yaw)
                totalChange += delta
            }
            previousYaw = yaw
        }

        // Berechne den Fortschritt als Prozentsatz
        return (totalChange / spinThreshold).coerceAtMost(1.0f) * 100.0f
    }

    fun hasSpunWildly(): Boolean {
        if (yawHistory.size < 2) {
            return false // Nicht genug Daten
        }

        var totalChange = 0.0f
        var previousYaw: Float? = null

        for (yaw in yawHistory) {
            if (previousYaw != null) {
                val delta = calculateYawDifference(previousYaw, yaw)
                totalChange += delta
            }
            previousYaw = yaw
        }

        // Wenn die gesamte Änderung den Schwellenwert überschreitet
        return totalChange >= spinThreshold
    }

    private fun calculateYawDifference(previous: Float, current: Float): Float {
        var diff = current - previous
        while (diff < -180.0f) {
            diff += 360.0f
        }
        while (diff > 180.0f) {
            diff -= 360.0f
        }
        return abs(diff.toDouble()).toFloat()
    }

    private fun normalizeYaw(yaw: Float): Float {
        // Yaw auf den Bereich [0, 360) normalisieren
        return (yaw % 360.0f + 360.0f) % 360.0f
    }
}
