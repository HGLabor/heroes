package gg.norisk.heroes.common.utils

import net.silkmc.silk.core.logging.logger

enum class PlayStyle(val displayName: String) {
    SOUP("Soup"), UHC("UHC");

    companion object {
        val current by lazy {
            /*(runCatching {
                PlayStyle.entries.first {
                    it.displayName.equals(
                        System.getProperty(
                            "ffa_mode", "UHC"
                        ), true
                    )
                }
            }.getOrNull() ?: UHC).also {
                logger().info("Current play style: $it")
            }*/
            UHC
        }
    }
}
