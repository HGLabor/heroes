package gg.norisk.heroes.aang.utils

import net.minecraft.entity.player.PlayerEntity

class PlayerRotationTracker {
    var lastYaw: Float? = null // Zu Beginn noch null, um den ersten Tick zu vermeiden
    var lastPitch: Float? = null
    var movementScale = 0f
    var maxMovementScale = 100f // Maximale Skala, auf die hochgezählt werden kann
    var movementDecayRate = 0.1f // Geschwindigkeit des Decays, wenn der Spieler sich wenig bewegt
    var movementIncreaseRate = 0.005f // Geschwindigkeit, mit der die Skala ansteigt bei Bewegung
    var movementThreshold = 5f // Minimale Änderung in Yaw oder Pitch, um als "Bewegung" zu gelten
    var onlyDecay = false

    // Diese Methode sollte pro Frame oder Tick aufgerufen werden
    fun update(player: PlayerEntity?) {
        if (player == null) return
        // Aktuelle Yaw- und Pitch-Werte des Spielers
        val currentYaw = player.yaw
        val currentPitch = player.pitch

        // Wenn dies das erste Mal ist, dass die Methode aufgerufen wird, setze die initialen Werte
        if (lastYaw == null || lastPitch == null) {
            lastYaw = currentYaw
            lastPitch = currentPitch
            return // Beim ersten Durchlauf kein Update der Skala
        }

        // Berechne die Änderungen in Yaw und Pitch
        val deltaYaw = Math.abs(currentYaw - lastYaw!!)
        val deltaPitch = Math.abs(currentPitch - lastPitch!!)

        // Überprüfe, ob die Änderung größer als der Schwellwert ist
        if ((deltaYaw > movementThreshold || deltaPitch > movementThreshold) && !onlyDecay) {
            // Erhöhe die Skala basierend auf der Bewegungsmenge
            movementScale += (deltaYaw + deltaPitch) * movementIncreaseRate
            if (movementScale > maxMovementScale) {
                movementScale = maxMovementScale
            }
        } else {
            // Verringere die Skala langsam, wenn keine signifikante Bewegung stattfindet
            movementScale -= movementDecayRate
            if (movementScale < 0) {
                movementScale = 0f
            }
        }

        // Speichere die aktuellen Werte für den nächsten Tick
        lastYaw = currentYaw
        lastPitch = currentPitch
    }

    // Neue Methode: Prozentsatz zwischen zwei Werten basierend auf der movementScale berechnen
    fun getPercentageBetween(minValue: Float, maxValue: Float): Float {
        // Normalisiere die movementScale zwischen 0 und 1
        val normalizedScale = movementScale / maxMovementScale

        // Berechne den interpolierten Wert zwischen minValue und maxValue
        return minValue + (maxValue - minValue) * normalizedScale
    }
}
