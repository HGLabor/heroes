package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.*
import gg.norisk.datatracker.serialization.BlockPosSerializer
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.BlockOutlineRenderer
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.CooldownProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import gg.norisk.heroes.common.hero.isHero
import gg.norisk.heroes.common.networking.Networking.mousePacket
import gg.norisk.heroes.common.networking.Networking.mouseScrollPacket
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.katara.KataraManager.Katara
import gg.norisk.heroes.katara.KataraManager.MOD_ID
import gg.norisk.heroes.katara.KataraManager.toId
import gg.norisk.heroes.katara.ability.HealingAbility.getWaterBendingPos
import gg.norisk.heroes.katara.ability.WaterCircleAbilityV2.waterCircleAmount
import gg.norisk.heroes.katara.ability.WaterFormingAbility.isWaterForming
import gg.norisk.heroes.katara.client.sound.WaterSelectingSoundInstance
import gg.norisk.heroes.katara.entity.WaterBendingEntity
import gg.norisk.heroes.katara.registry.EntityRegistry
import gg.norisk.heroes.katara.registry.SoundRegistry
import kotlinx.serialization.Serializable
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.core.server.players
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.network.packet.s2cPacket
import kotlin.random.Random

object WaterBendingAbility {
    val ability = object : HoldAbility("Water Bending") {
        init {
            HeroesManager.client {
                this.keyBind = HeroKeyBindings.firstKeyBind
            }

            mouseScrollPacket.receiveOnServer { packet, context ->
                mcCoroutineTask(sync = true, client = false) {
                    context.player.scaleBendingDistance(packet)
                }
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
            this.maxDurationProperty =
                buildMaxDuration(5.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))
        }

        override fun onStart(player: PlayerEntity) {
            if (!player.world.isClient) {
                player.isWaterSelecting = true
            }
        }

        override fun onEnd(player: PlayerEntity) {
            if (player is ServerPlayerEntity) {
                player.isWaterSelecting = false
                // player.sendMessage("END".literal)
                var lastPos: BlockPos? = null
                for (block in player.selectedWaterBlocks.blocks) {
                    val state = player.world.getBlockState(block)
                    if (state.isWaterSource
                    ) {
                        lastPos = block
                        player.world.breakBlock(block, false, player)
                        if (state.isOf(Blocks.WATER)) {
                            player.serverWorld.setBlockState(
                                block,
                                Fluids.FLOWING_WATER.getFlowing(1, false).blockState
                            )
                        }
                    }
                }
                if (lastPos != null) {
                    val water = EntityRegistry.WATER_BENDING.create(player.serverWorld) ?: return
                    /*player.serverWorld.setBlockState(
                        clipWithDistance.blockPos,
                        Fluids.FLOWING_WATER.getFlowing(1, false).blockState
                    )*/
                    water.setPosition(lastPos.toCenterPos())
                    //water.setPosition(player.getWaterBendingPos())
                    water.ownerId = player.id
                    water.isInitial = true
                    player.serverWorld.spawnEntity(water)
                } else {
                    // player.sendMessage("Jetzt".literal)
                    if (player.waterCircleAmount == 1) {
                        player.waterCircleAmount -= 1
                        player.sound(SoundRegistry.WATER_CIRCLE_ADD, 0.4f, Random.nextDouble(1.5, 2.0))
                        val water = EntityRegistry.WATER_BENDING.create(player.serverWorld) ?: return
                        water.setPosition(player.getWaterBendingPos())
                        water.ownerId = player.id
                        water.isInitial = false
                        player.serverWorld.spawnEntity(water)
                    }
                }
                player.selectedWaterBlocks = SelectedWaterBlocks(mutableListOf())
            }
        }
    }

    private fun ServerPlayerEntity.scaleBendingDistance(packet: Boolean) {
        if (!isHero(Katara)) return
        if (getCurrentBendingEntity() == null) return
        val world = this.serverWorld

        val scale = 0.5
        val forceStrength = if (packet) scale else -scale


        waterBendingDistance += forceStrength
        if (waterBendingDistance < 3.0) {
            waterBendingDistance = 3.0
        }
        if (waterBendingDistance > 15) {
            waterBendingDistance = 15.0
        }
    }

    val BlockState.isWaterSource
        get() = isOf(Blocks.WATER) || isIn(BlockTags.SNOW) || isIn(BlockTags.ICE) || isIn(
            BlockTags.LEAVES
        ) || isIn(BlockTags.FLOWERS) || isIn(BlockTags.REPLACEABLE_BY_TREES) || isIn(BlockTags.LOGS) || isOf(Blocks.BAMBOO)

    fun initServer() {
        (registeredTypes as MutableMap<Any, Any>).put(
            SelectedWaterBlocks::class,
            SelectedWaterBlocks.serializer()
        )

        ServerTickEvents.END_SERVER_TICK.register {
            for (player in it.players) {
                if (!player.isHero(Katara)) continue
                if (!player.isWaterSelecting) continue
                /*val target = findCrosshairTarget(player,5.0,5.0,1f)
                player.sendMessage("Target: ${target.pos} ${target.type} ${player.world.getBlockState(target.pos.toBlockPos())}".literal)
                player.firstWaterFormingPos = target.pos.toBlockPos()*/
                val pos = (player.raycast(45.0, 0.0f, true) as? BlockHitResult?)?.blockPos?.toImmutable() ?: continue
                val state = player.world.getBlockState(pos)
                if (state.isWaterSource) {
                    if (!player.selectedWaterBlocks.blocks.contains(pos)) {
                        player.selectedWaterBlocks.blocks.add(pos)
                        if (player.selectedWaterBlocks.blocks.size >= 15) {
                            player.selectedWaterBlocks.blocks.removeFirstOrNull()
                        }

                        player.selectedWaterBlocks = SelectedWaterBlocks(buildList {
                            addAll(player.selectedWaterBlocks.blocks)
                        }.toMutableList())
                    }
                }
            }
        }

        mousePacket.receiveOnServer { packet, context ->
            mcCoroutineTask(sync = true, client = false) {
                //TODO automatisieren
                if (!context.player.isHero(Katara)) return@mcCoroutineTask
                if (packet.isClicked() && packet.isRight()) {
                    for (entity in context.player.serverWorld.iterateEntities()) {
                        if (entity is WaterBendingEntity && entity.ownerId == context.player.id) {
                            entity.drop(context.player)
                        }
                    }
                } else if (packet.isLeft() && packet.isClicked()) {
                    for (entity in context.player.serverWorld.iterateEntities()) {
                        if (entity is WaterBendingEntity && entity.ownerId == context.player.id) {
                            entity.launch(context.player)
                        }
                    }
                }
            }
        }

        syncedValueChangeEvent.listen { event ->
            if (event.key != IS_WATER_SELECTING) return@listen
            val player = event.entity as? PlayerEntity? ?: return@listen
            if (player.world.isClient) {
                if (player.isWaterSelecting) {
                    MinecraftClient.getInstance().soundManager.play(WaterSelectingSoundInstance(player) { entity ->
                        ((entity as? PlayerEntity?)?.isWaterSelecting == true)
                    })
                }
            }
        }
    }

    fun initClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register {
            val player = MinecraftClient.getInstance().player ?: return@register
            val matrices = it.matrixStack() ?: return@register
            if (!player.isWaterSelecting) return@register
            for (block in player.selectedWaterBlocks.blocks) {
                BlockOutlineRenderer.drawBlockBox(
                    matrices,
                    it.consumers() ?: return@register,
                    block,
                    1.0f,
                    1.0f,
                    1.0f,
                    0.6f
                )
            }
        }
    }

    @Serializable
    data class SelectedWaterBlocks(
        val blocks: MutableList<@Serializable(with = BlockPosSerializer::class) BlockPos>
    )

    private const val IS_WATER_SELECTING = "WaterBending:IS_WATER_SELECTING"
    private const val WATER_BENDING_DISTANCE = "WaterBending:WATER_BENDING_DISTANCE"

    @Environment(EnvType.CLIENT)
    fun getWaterBendingPose(player: AbstractClientPlayerEntity, hand: Hand): ArmPose? {
        if (player.isWaterSelecting) {
            return ArmPose.BOW_AND_ARROW
        }
        if (player.isWaterForming) {
            return ArmPose.BOW_AND_ARROW
        }
        val currentEntity = player.getCurrentBendingEntity()
        if (currentEntity != null && !currentEntity.wasLaunched) {
            return ArmPose.BOW_AND_ARROW
        }
        return null
    }

    var Entity.selectedWaterBlocks: SelectedWaterBlocks
        get() = this.getSyncedData<SelectedWaterBlocks>("$MOD_ID:SelectedWaterBlocks")
            ?: SelectedWaterBlocks(mutableListOf())
        set(value) {
            this.setSyncedData("$MOD_ID:SelectedWaterBlocks", value)
        }

    var PlayerEntity.waterBendingDistance: Double
        get() = this.getSyncedData<Double>(WATER_BENDING_DISTANCE) ?: 3.0
        set(value) = this.setSyncedData(WATER_BENDING_DISTANCE, value)

    var PlayerEntity.isWaterSelecting: Boolean
        get() = this.getSyncedData<Boolean>(IS_WATER_SELECTING) ?: false
        set(value) = this.setSyncedData(IS_WATER_SELECTING, value)

    fun PlayerEntity.getCurrentBendingEntity(): WaterBendingEntity? {
        val entities = if (!world.isClient) {
            (world as ServerWorld).iterateEntities()
        } else {
            (world as ClientWorld).entities
        }

        return entities.filterIsInstance<WaterBendingEntity>().find { it.ownerId == id }
    }
}