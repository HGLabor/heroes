package gg.norisk.heroes.common.utils

import gg.norisk.heroes.common.HeroesManager.logger
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Vector3f
import java.io.File
import kotlin.random.Random

fun Vector3f.toVector() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
fun Vec3d.toBlockPos() = BlockPos(x.toInt(), y.toInt(), z.toInt())
fun BlockPos.toVec() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
fun World.hasFreeSide(blockPos: BlockPos) =
    Direction.values().any { direction: Direction -> this.getBlockState(blockPos.offset(direction)).isAir }

fun World.hasFreeHorizontalSide(blockPos: BlockPos): Direction? {
    for (direction in Direction.Type.HORIZONTAL.toList()) {
        if (this.getBlockState(blockPos.offset(direction)).isAir) {
            return direction
        }
    }
    return null
}

fun File.createIfNotExists(): File {
    if (this.exists()) return this
    if (!this.parentFile.mkdirs() && !this.parentFile.exists()) {
        logger.warn("Parent of ${this.name} does not exist and could not be created")
    }
    this.createNewFile()
    return this
}

fun calculateProbability(probabilityInPercent: Double): Boolean {
    require(probabilityInPercent in 0.0..100.0) {
        "Die Wahrscheinlichkeit muss zwischen 0 und 100 Prozent liegen."
    }
    val randomValue = Random.nextDouble(0.0, 100.0)
    return randomValue < probabilityInPercent
}

fun ClosedFloatingPointRange<Double>.random(): Double {
    return Random.nextDouble(this.start, this.endInclusive)
}
