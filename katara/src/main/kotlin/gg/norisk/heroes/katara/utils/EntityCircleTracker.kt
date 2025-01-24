package gg.norisk.heroes.katara.utils

import net.minecraft.entity.Entity
import java.util.*
import kotlin.math.abs

class EntityCircleTracker {
    private val rotationHistory: Deque<Vec2f> = ArrayDeque()
    private val maxHistorySize = 40 // Über 2 Sekunden (20 Ticks pro Sekunde)
    private val minCircleThreshold = 360.0f // Mindestens 360° Yaw+Pitch-Änderung

    fun update(entity: Entity) {
        val yaw = normalizeAngle(entity.yaw)
        val pitch = normalizeAngle(entity.pitch)

        // Aktuelle Werte speichern
        if (rotationHistory.size >= maxHistorySize) {
            rotationHistory.pollFirst()
        }
        rotationHistory.addLast(Vec2f(yaw, pitch))
    }

    fun clear() {
        rotationHistory.clear()
    }

    val isDrawingCircle: Boolean
        get() {
            if (rotationHistory.size < 2) {
                return false // Nicht genug Daten
            }

            var yawChange = 0.0f
            var pitchChange = 0.0f
            var previous: Vec2f? = null

            for (current in rotationHistory) {
                if (previous != null) {
                    yawChange += calculateAngleDifference(previous.x, current.x)
                    pitchChange += calculateAngleDifference(previous.y, current.y)
                }
                previous = current
            }

            // Prüfen, ob Yaw und Pitch zusammen mindestens 360°-Bewegung ergeben
            return (yawChange + pitchChange) >= minCircleThreshold
        }

    private fun calculateAngleDifference(previous: Float, current: Float): Float {
        var diff = current - previous
        while (diff < -180.0f) {
            diff += 360.0f
        }
        while (diff > 180.0f) {
            diff -= 360.0f
        }
        return abs(diff.toDouble()).toFloat()
    }

    private fun normalizeAngle(angle: Float): Float {
        return (angle % 360.0f + 360.0f) % 360.0f
    }

    // Hilfsklasse für 2D-Werte
    private data class Vec2f(// Yaw
        val x: Float, // Pitch
        val y: Float
    )
}
