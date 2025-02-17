package gg.norisk.ffa.server.schematic

import kotlinx.coroutines.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.*
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.text.broadcastText
import java.io.File

object SchematicHandler {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    val cachedSchematics = hashMapOf<String, Schematic>()
    private val schematicQueue: ArrayDeque<SchematicJob> = ArrayDeque()
    private var forcePaste: Boolean = true

    suspend fun saveSchematic(world: ServerWorld, start: BlockPos, end: BlockPos, outputFile: File) {
        withContext(Dispatchers.IO) {
            val dimensions = Vec3i(start.x - end.x, start.y - end.y, start.z - end.z)

            val nbt = NbtCompound()
            val dimensionsNbt = NbtCompound().apply {
                putInt("x", dimensions.x)
                putInt("y", dimensions.y)
                putInt("z", dimensions.z)
            }

            val blocksNbt = NbtList().apply {
                BlockPos.iterate(start, end).forEach { pos ->
                    val state = world.getBlockState(pos)
                    val blockNbt = NbtCompound()
                    blockNbt.putInt("x", pos.x - start.x)
                    blockNbt.putInt("y", pos.y - start.y)
                    blockNbt.putInt("z", pos.z - start.z)
                    blockNbt.put("state", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().orElseThrow())
                    add(blockNbt)
                }
            }
            nbt.put("dimensions", dimensionsNbt)
            nbt.put("blocks", blocksNbt)

            File(outputFile.path).outputStream().use { stream ->
                NbtIo.writeCompressed(nbt, stream)
            }
        }
    }

    suspend fun loadSchematic(schematicFile: File): Schematic {
        cachedSchematics[schematicFile.name]?.let {
            return it
        }

        val nbt = withContext(Dispatchers.IO) {
            File(schematicFile.path).inputStream().use { stream ->
                NbtIo.readCompressed(stream, NbtSizeTracker.ofUnlimitedBytes())
            }
        }

        val dimensionsNbt = nbt.getCompound("dimensions")
        val dimensions = Vec3i(
            dimensionsNbt.getInt("x"),
            dimensionsNbt.getInt("y"),
            dimensionsNbt.getInt("z")
        )

        val blocksNbt = nbt.getList("blocks", 10) // 10 = NbtCompound type
        val blocks = blocksNbt.map { element ->
            val blockNbt = element as NbtCompound
            val x = blockNbt.getInt("x")
            val y = blockNbt.getInt("y")
            val z = blockNbt.getInt("z")
            val blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, blockNbt.get("state"))
                .result()
                .orElse(null)

            if (blockState != null) BlockData(Vec3i(x, y, z), blockState) else null
        }.filterNotNull().sortedBy { it.pos.y }

        val schematic = Schematic(blocks, dimensions)
        cachedSchematics[schematicFile.name] = schematic  // Cache the schematic
        return schematic
    }

    fun pasteSchematic(world: ServerWorld, schematic: Schematic) {
        val schematicJob = SchematicJob(
            world,
            schematic,
            BlockPos(0, 50, 0),
        )
        schematicQueue.add(schematicJob)
        if (forcePaste) {
            processNextQueueEntry()
        }
    }

    private fun processNextQueueEntry() {
        fun setBlock(world: ServerWorld, pos: BlockPos, state: BlockState) {
            val chunk = world.getWorldChunk(pos)
            val chunkSection = chunk.sectionArray[chunk.getSectionIndex(pos.y)]
            chunkSection.setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state, false)
        }

        val schematicJob = schematicQueue.firstOrNull() ?: run {
            forcePaste = true
            return
        }
        forcePaste = false
        schematicJob.state = SchematicJob.Companion.State.IN_PROGRESS
        val world = schematicJob.world

        val schematic = schematicJob.schematic
        val totalBlocks = schematicJob.totalBlocks
        val batchSize = 1000
        val coroutineJobs = mutableListOf<Job>()

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        coroutineScope.launch {
            val chunks = mutableMapOf<ChunkPos, MutableList<BlockData>>()

            // Gruppiere Blöcke nach Chunks
            schematic.blocks.forEach { blockData ->
                val blockPos = schematicJob.startPos.add(blockData.pos.x, blockData.pos.y, blockData.pos.z)
                val chunkPos = ChunkPos(blockPos.x shr 4, blockPos.z shr 4)

                minX = minOf(minX, blockPos.x)
                minY = minOf(minY, blockPos.y)
                minZ = minOf(minZ, blockPos.z)
                maxX = maxOf(maxX, blockPos.x)
                maxY = maxOf(maxY, blockPos.y)
                maxZ = maxOf(maxZ, blockPos.z)
                chunks.getOrPut(chunkPos) { mutableListOf() }.add(BlockData(blockPos, blockData.state))
            }

            // Verarbeite Chunks parallel
            chunks.values.chunked(batchSize).forEach { chunkBatch ->
                coroutineJobs += coroutineScope.launch {
                    chunkBatch.forEach { chunkBlocks ->
                        world.server.execute {
                            chunkBlocks.forEach { (pos, state) ->
                                /*setBlock(BlockPos(pos), state)*/
                                println("palced block at ${world.registryKey.value}: ${pos}")
                                world.setBlockState(
                                    BlockPos(pos),
                                    state,
                                    Block.NO_REDRAW
                                )

                                if (schematicJob.progress.incrementAndGet() == totalBlocks) {
                                    schematicJob.state = SchematicJob.Companion.State.FINISHED
                                    onSchematicJobFinished(schematicJob)
                                }
                            }
                        }
                    }
                    delay(1.ticks)
                }
                delay(10.ticks)
            }
        }
    }

    private fun onSchematicJobFinished(schematic: SchematicJob) {
        Silk.serverOrThrow.broadcastText("Schematic finished: ${schematic.world.registryKey.value}")
        schematicQueue.removeIf { it.state == SchematicJob.Companion.State.FINISHED }
        coroutineScope.launch {
            delay(100)
            processNextQueueEntry()
        }
    }
}
