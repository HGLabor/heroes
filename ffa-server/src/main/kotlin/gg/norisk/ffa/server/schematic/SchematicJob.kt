package gg.norisk.ffa.server.schematic

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.concurrent.atomic.AtomicInteger

data class SchematicJob(
    val world: ServerWorld,
    val schematic: Schematic,
    val startPos: BlockPos,
    var state: State = State.WAITING,
    var progress: AtomicInteger = AtomicInteger(0),
) {
    val totalBlocks get() = schematic.blocks.size

    companion object {
        enum class State {
            WAITING, IN_PROGRESS, FINISHED
        }
    }
}
