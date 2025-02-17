package gg.norisk.ffa.server.schematic

import net.minecraft.util.math.Vec3i

data class Schematic(val blocks: List<BlockData>, val dimensions: Vec3i)
