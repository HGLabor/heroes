package gg.norisk.datatracker

import gg.norisk.datatracker.entity.initSyncedEntitiesClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object DataTracker : ModInitializer, ClientModInitializer {
    private const val MOD_ID = "datatracker"
    fun String.toId() = Identifier.of(MOD_ID, this)
    val logger = LogManager.getLogger(MOD_ID)
    override fun onInitialize() {
    }

    override fun onInitializeClient() {
        initSyncedEntitiesClient()
    }
}
