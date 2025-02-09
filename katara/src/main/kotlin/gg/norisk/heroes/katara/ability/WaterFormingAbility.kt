package gg.norisk.heroes.katara.ability

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.RenderUtils
import gg.norisk.heroes.common.HeroesManager
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.HoldAbility
import gg.norisk.heroes.common.hero.isHero
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.common.utils.toBlockPos
import gg.norisk.heroes.katara.KataraManager
import gg.norisk.heroes.katara.ability.IceShardAbility.isIceShooting
import gg.norisk.heroes.katara.ability.WaterBendingAbility.getCurrentBendingEntity
import gg.norisk.heroes.katara.client.render.IFluidRendererExt
import gg.norisk.heroes.katara.registry.SoundRegistry
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.math.geometry.filledSpherePositionSet
import net.silkmc.silk.core.server.players
import kotlin.math.abs
import kotlin.math.sign
import kotlin.time.Duration.Companion.seconds

object WaterFormingAbility {

    val waterFormingMaxDistance = NumberProperty(
        10.0, 3,
        "Water Forming Max Blocks",
        AddValueTotal(5.0, 5.0, 5.0)
    ).apply {
        icon = {
            Components.item(itemStack(Items.ICE) {})
        }
    }

    val ability = object : HoldAbility("Water Forming") {
        init {
            HeroesManager.client {
                this.keyBind = HeroKeyBindings.secondKeyBind
            }
            this.condition = {
                val pos = (it.raycast(20.0, 0.0f, false) as? BlockHitResult?)?.blockPos?.toImmutable()
                checkForEnoughWater(pos, it.world) || it.getCurrentBendingEntity() != null
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))
            this.maxDurationProperty =
                buildMaxDuration(10.0, 5, AddValueTotal(0.1, 0.4, 0.2, 0.8, 1.5, 1.0))

            this.properties = listOf(waterFormingMaxDistance)
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                val pos = (player.raycast(20.0, 0.0f, false) as? BlockHitResult?)?.blockPos?.toImmutable()
                player.isWaterForming = true
                player.firstWaterFormingPos = pos
            }
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.BLUE_ICE.defaultStack)
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/packed_ice.png")
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            cleanUp(player)
        }

        private fun cleanUp(player: PlayerEntity) {
            if (player is ServerPlayerEntity) {
                player.isWaterForming = false
                player.firstWaterFormingPos = null
                player.secondWaterFormingPos = null
            }
        }

        override fun onEnd(player: PlayerEntity, abilityEndInformation: AbilityEndInformation) {
            super.onEnd(player, abilityEndInformation)
            if (player is ServerPlayerEntity) {
                placeIceSelection(
                    player.serverWorld,
                    player,
                    player.firstWaterFormingPos,
                    player.secondWaterFormingPos
                )
                cleanUp(player)
            }
        }
    }

    private var startTime: Long? = null
    private var endTime: Long? = null // Endzeit speichern
    private var lastAlpha: Float = 0f

    fun initServer() {
        ServerTickEvents.END_SERVER_TICK.register {
            for (player in it.players) {
                if (!player.isHero(KataraManager.Katara)) continue
                /*val target = findCrosshairTarget(player,5.0,5.0,1f)
                player.sendMessage("Target: ${target.pos} ${target.type} ${player.world.getBlockState(target.pos.toBlockPos())}".literal)
                player.firstWaterFormingPos = target.pos.toBlockPos()*/
                if (player.firstWaterFormingPos != null) {
                    val pos = (player.raycast(20.0, 0.0f, false) as? BlockHitResult?)?.blockPos?.toImmutable()
                    if (pos != player.firstWaterFormingPos && pos?.isWithinDistance(
                            player.firstWaterFormingPos,
                            waterFormingMaxDistance.getValue(player.uuid)
                        ) == true
                    ) {
                        player.secondWaterFormingPos = pos
                    }
                }
            }
        }
    }

    fun initClient() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register {
            val player = MinecraftClient.getInstance().player ?: return@register
            val tickDelta = it.tickCounter().getTickDelta(false)
            val matrixStack = it.matrixStack() ?: return@register
            /*renderBlock(
                matrixStack,
                Vec3d.of(player.firstWaterFormingPos ?: return@register),
                Blocks.ICE.defaultState,
                false
            )*/
            for (blockPos in createStaircasePath(
                player.firstWaterFormingPos ?: return@register,
                player.secondWaterFormingPos ?: player.firstWaterFormingPos ?: return@register
            )) {
                renderBlock(
                    matrixStack,
                    Vec3d.of(blockPos),
                    Blocks.ICE.defaultState,
                    false
                )
            }
        }

        val overlay = Identifier.ofVanilla("textures/misc/powder_snow_outline.png")
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, tickCounter ->
            val player = MinecraftClient.getInstance().player

            if (player?.isWaterForming == true || player?.isIceShooting == true) {
                if (startTime == null) {
                    startTime = System.currentTimeMillis() // Startzeit initialisieren
                }
                endTime = null // Endzeit zurücksetzen, da der Effekt aktiv ist

                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - startTime!! // Verstrichene Zeit in Millisekunden

                val fadeInDuration = 1000.0 // Fade-In-Dauer in Millisekunden (1 Sekunde)
                lastAlpha = (elapsedTime / fadeInDuration).coerceAtMost(1.0).toFloat() // Begrenze Alpha auf maximal 1

                RenderUtils.renderOverlay(drawContext, overlay, lastAlpha)
            } else if (endTime == null && startTime != null) {
                // Effekt ist beendet, Endzeit setzen
                endTime = System.currentTimeMillis()
            } else if (endTime != null) {
                // Ausfade-Phase
                val currentTime = System.currentTimeMillis()
                val fadeOutDuration = 200.0 // Ausfade-Dauer in Millisekunden (1 Sekunde)
                val elapsedFadeOutTime = currentTime - endTime!! // Verstrichene Zeit seit Ende

                if (elapsedFadeOutTime < fadeOutDuration) {
                    // Linearer Fade-Out von 1 bis 0
                    val alpha = (lastAlpha - (elapsedFadeOutTime / fadeOutDuration)).toFloat()
                    RenderUtils.renderOverlay(drawContext, overlay, alpha)
                } else {
                    // Vollständig ausgeblendet, alles zurücksetzen
                    startTime = null
                    endTime = null
                }
            }
        })
    }

    @Environment(EnvType.CLIENT)
    fun renderBlock(
        matrixStack: MatrixStack,
        pos: Vec3d,
        state: BlockState,
        fluid: Boolean = false,
    ) {
        val blockPos = pos.toBlockPos()
        //println("Pos: $blockPos")
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        val renderer = MinecraftClient.getInstance().blockRenderManager
        val world = MinecraftClient.getInstance().world ?: return
        val blockState = world.getBlockState(blockPos)
        if (!(blockState.isLiquid || blockState.isAir || !blockState.isSolid)) {
            return
        }
        val vertexConsumer = if (fluid) {
            MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
                RenderLayers.getFluidLayer(state.fluidState)
            )
        } else {
            MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers.getBuffer(
                RenderLayers.getBlockLayer(state)
            )
        }

        matrixStack.push()
        matrixStack.translate(
            blockPos.x - camera.pos.x,
            blockPos.y - camera.pos.y,
            blockPos.z - camera.pos.z
        )

        val scale = 1f
        matrixStack.scale(scale, scale, scale)


        //matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))

        // Rotation um die Y-Achse

        //matrixStack.translate(-0.5, 0.5, -0.5)

        if (fluid) {
            (renderer.fluidRenderer as IFluidRendererExt).katara_renderFluid(
                matrixStack,
                world,
                blockPos.toCenterPos(),
                vertexConsumer,
                state,
                state.fluidState,
                null
            )
        } else {
            renderer.renderBlock(state, blockPos, world, matrixStack, vertexConsumer, true, Random.create())
        }

        matrixStack.pop()
    }


    fun createStaircasePath(start: BlockPos, end: BlockPos): List<BlockPos> {
        val path = mutableSetOf<BlockPos>()
        path.add(start)
        path.add(end)

        var currentX = start.x
        var currentY = start.y
        var currentZ = start.z

        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val deltaZ = end.z - start.z

        val steps = maxOf(abs(deltaX), abs(deltaY), abs(deltaZ))

        val stepX = deltaX.sign
        val stepY = deltaY.sign
        val stepZ = deltaZ.sign

        var accumulatedY = 0.0
        val yStepIncrement = abs(deltaY.toDouble() / steps)

        for (i in 0..steps) {
            path.add(BlockPos(currentX, currentY, currentZ))

            if (currentX != end.x) currentX += stepX
            if (currentZ != end.z) currentZ += stepZ

            accumulatedY += yStepIncrement
            if (accumulatedY >= 1) {
                currentY += stepY
                accumulatedY -= 1
            }
        }

        return path.toList()
    }

    private fun placeIceSelection(
        world: ServerWorld,
        player: ServerPlayerEntity,
        firstPos: BlockPos?,
        secondPos: BlockPos?
    ) {
        if (firstPos == null) return
        if (secondPos == null) return

        player.sound(SoundRegistry.ICE_PLACE, pitch = kotlin.random.Random.nextDouble(1.0, 1.5))
        //player.sound(SoundEvents.ENTITY_PLAYER_HURT_FREEZE, volume = 0.5f, pitch = kotlin.random.Random.nextDouble(1.0, 1.5))
        player.getCurrentBendingEntity()?.discard()

        for (blockPos in createStaircasePath(
            firstPos,
            secondPos
        )) {
            val currentState = world.getBlockState(blockPos)
            if (currentState.isLiquid || currentState.isAir || !currentState.isSolid) {
                world.setBlockState(blockPos, Blocks.ICE.defaultState)
                val pos = blockPos.toCenterPos()
                world.spawnParticles(
                    ParticleTypes.CLOUD,
                    pos.x + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    pos.y + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    pos.z + kotlin.random.Random.nextDouble(-1.0, 1.0),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
                )
            }
        }
    }

    private fun checkForEnoughWater(pos: BlockPos?, world: World): Boolean {
        if (pos == null) return false
        for (blockPos in pos.filledSpherePositionSet(3)) {
            val state = world.getBlockState(blockPos)
            if (world.getBlockState(blockPos)
                    .isOf(Blocks.WATER) || state.isIn(BlockTags.SNOW) || state.isIn(BlockTags.ICE)
            ) {
                return true
            }
        }
        return false
    }

    private const val IS_WATER_FORMING = "WaterBending:IS_WATER_FORMING"
    private const val WATER_FORMING_FIRST = "WaterBending:FirstWaterFormingPos"
    private const val WATER_FORMING_SECOND = "WaterBending:SecondWaterFormingPos"

    var PlayerEntity.isWaterForming: Boolean
        get() = this.getSyncedData<Boolean>(IS_WATER_FORMING) ?: false
        set(value) = this.setSyncedData(IS_WATER_FORMING, value)

    var PlayerEntity.firstWaterFormingPos: BlockPos?
        get() = this.getSyncedData<BlockPos>(WATER_FORMING_FIRST)
        set(value) = this.setSyncedData(WATER_FORMING_FIRST, value)

    var PlayerEntity.secondWaterFormingPos: BlockPos?
        get() = this.getSyncedData<BlockPos>(WATER_FORMING_SECOND)
        set(value) = this.setSyncedData(WATER_FORMING_SECOND, value)
}