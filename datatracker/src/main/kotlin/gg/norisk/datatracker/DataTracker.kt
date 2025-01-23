package gg.norisk.datatracker

import gg.norisk.datatracker.entity.*
import gg.norisk.datatracker.serialization.BlockPosSerializer
import kotlinx.serialization.KSerializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.command
import net.silkmc.silk.commands.player
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KClass

object DataTracker : ModInitializer, ClientModInitializer {
    private const val MOD_ID = "datatracker"
    fun String.toId() = Identifier.of(MOD_ID, this)
    val logger = LogManager.getLogger(MOD_ID)
    override fun onInitialize() {
        command("testcommand") {
            runs {
                val player = this.source.playerOrThrow
                player.isHealing = !player.isHealing

                player.derTest = DerTest("HIiii")
            }
        }

        clientCommand("testcommandclient") {
            runs {
                println(this.source.player.isHealing)
                println(this.source.player.derTest)
            }
        }

        registeredTypes.put(DerTest::class as KClass<out Comparable<*>>, DerTest.serializer() as KSerializer<out Comparable<*>>)
    }

    override fun onInitializeClient() {
        initSyncedEntitiesClient()
    }

    private var PlayerEntity.derTest: DerTest?
        get() = this.getSyncedData<DerTest>("xd")
        set(value) = this.setSyncedData("xd", value)

    private var PlayerEntity.isHealing: Boolean
        get() = this.getSyncedData<Boolean>("Yooo") ?: false
        set(value) = this.setSyncedData("Yooo", value)
}
