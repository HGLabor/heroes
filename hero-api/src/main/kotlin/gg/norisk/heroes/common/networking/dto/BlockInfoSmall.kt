package gg.norisk.heroes.common.networking.dto

import gg.norisk.heroes.common.serialization.BlockPosSerializer
import gg.norisk.heroes.common.serialization.BlockStateSerializer
import kotlinx.serialization.Serializable
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

@Serializable
data class BlockInfoSmall(
    @Serializable(with = BlockStateSerializer::class) val state: BlockState,
    @Serializable(with = BlockPosSerializer::class) val pos: BlockPos
)
