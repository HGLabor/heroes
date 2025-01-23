package gg.norisk.heroes.common.hero.utils

object ColorUtils {
    fun hexAsRgb(hexColor: Int): Triple<Int, Int, Int> {
        val red = (hexColor shr 16) and 0xFF
        val green = (hexColor shr 8) and 0xFF
        val blue = hexColor and 0xFF
        return Triple(red, green, blue)
    }

    fun isLightColor(hexColor: Int): Boolean {
        val (red, green, blue) = hexAsRgb(hexColor)

        // Calculate luminance
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255

        // Determine if color is light or dark
        return luminance > 0.5
    }

    fun isDarkColor(hexColor: Int) = !isLightColor(hexColor)

    fun darkenHexColor(hexColor: Int, factor: Double): Int {
        // Extract RGB components
        val (red, green, blue) = hexAsRgb(hexColor)

        // Darken each component
        val darkenedRed = (red * (1 - factor)).toInt()
        val darkenedGreen = (green * (1 - factor)).toInt()
        val darkenedBlue = (blue * (1 - factor)).toInt()

        // Combine components and return the darkened color
        return (darkenedRed shl 16) or (darkenedGreen shl 8) or darkenedBlue
    }

    fun lightenHexColor(hexColor: Int, factor: Double): Int {
        // Extract RGB components
        val (red, green, blue) = hexAsRgb(hexColor)

        // Darken each component
        val lightenedRed = (red + (255 - red) * factor).toInt()
        val lightenedGreen = (green + (255 - green) * factor).toInt()
        val lightenedBlue = (blue + (255 - blue) * factor).toInt()

        // Combine components and return the darkened color
        return (lightenedRed shl 16) or (lightenedGreen shl 8) or lightenedBlue
    }

    fun contrast(hexColor: Int): Int {
        return if (isDarkColor(hexColor)) lightenHexColor(hexColor, 0.33)
        else darkenHexColor(hexColor, 0.33)
    }
}