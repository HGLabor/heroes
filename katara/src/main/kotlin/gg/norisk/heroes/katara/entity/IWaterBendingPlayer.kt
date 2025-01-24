package gg.norisk.heroes.katara.entity

import net.minecraft.util.math.BlockPos

interface IWaterBendingPlayer {
    val katara_waterPillarBlocks: MutableSet<BlockPos>
    var katara_waterPillarOrigin: BlockPos?
}