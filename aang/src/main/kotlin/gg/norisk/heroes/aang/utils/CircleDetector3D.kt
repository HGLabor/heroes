package gg.norisk.heroes.aang.utils

import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.sqrt

class CircleDetector3D {

    // Liste zur Speicherung der Mausbewegungen im 3D-Raum
    private val mouseMovements = mutableSetOf<Vec3d>()

    // Methode, um Mausbewegungen aufzuzeichnen
    fun addMouseMovement(x: Double, y: Double, z: Double): Boolean {
       return mouseMovements.add(Vec3d(x, y, z))
    }

    // Methode zur Berechnung des Kreisähnlichkeitsprozentsatzes im 3D-Raum
    fun calculateCircleAccuracy(): Double {
        if (mouseMovements.size < 3) return 0.0  // Nicht genug Punkte, um einen Kreis zu erkennen

        // Berechnung des Mittelpunkts (Schwerpunkt der Bewegung)
        val center = calculateCenter(mouseMovements)

        // Berechnung des durchschnittlichen Radius
        val averageRadius = calculateAverageRadius(mouseMovements, center)

        // Berechnung des Fehlerwerts für jeden Punkt
        var totalError = 0.0
        for (point in mouseMovements) {
            val distanceToCenter = calculateDistance(center, point)
            totalError += abs(distanceToCenter - averageRadius)
        }

        // Normierung des Fehlers und Umwandlung in Prozent (100% bedeutet perfekter Kreis)
        val maxError = averageRadius * mouseMovements.size
        val accuracy = 100.0f - (totalError / maxError * 100.0f)

        return accuracy.coerceIn(0.0, 100.0)  // Beschränkung auf 0-100%
    }

    // Hilfsmethode zur Berechnung des Mittelpunkts im 3D-Raum
    private fun calculateCenter(points: Collection<Vec3d>): Vec3d {
        var sumX = 0.0
        var sumY = 0.0
        var sumZ = 0.0

        for (point in points) {
            sumX += point.x
            sumY += point.y
            sumZ += point.z
        }

        val centerX = sumX / points.size
        val centerY = sumY / points.size
        val centerZ = sumZ / points.size

        return Vec3d(centerX, centerY, centerZ)
    }

    // Hilfsmethode zur Berechnung des durchschnittlichen Radius
    private fun calculateAverageRadius(points: Collection<Vec3d>, center: Vec3d): Double {
        var totalRadius = 0.0
        for (point in points) {
            totalRadius += calculateDistance(center, point)
        }
        return totalRadius / points.size
    }

    // Hilfsmethode zur Berechnung der Distanz zwischen zwei Punkten im 3D-Raum
    private fun calculateDistance(p1: Vec3d, p2: Vec3d): Double {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val dz = p1.z - p2.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
