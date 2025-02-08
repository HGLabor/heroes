package gg.norisk.heroes.toph.entity

import kotlinx.coroutines.Job
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.*

interface ITophPlayer {
    val seismicBlocks: MutableSet<Pair<Long, BlockPos>>
    val seismicEntities: MutableSet<Pair<Long, UUID>>
    val toph_seismicTasks: MutableList<Job>
}

val PlayerEntity.toph get() = this as ITophPlayer
