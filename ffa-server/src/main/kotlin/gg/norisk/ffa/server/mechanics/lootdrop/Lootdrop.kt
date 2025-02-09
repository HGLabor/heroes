package gg.norisk.ffa.server.mechanics.lootdrop

import gg.norisk.ffa.server.mechanics.KitEditor
import gg.norisk.ffa.server.mechanics.lootdrop.loottable.ExperienceLootdropItem
import gg.norisk.ffa.server.mechanics.lootdrop.loottable.ItemStackLootdropItem
import gg.norisk.ffa.server.mechanics.lootdrop.loottable.SoupLootdropLoottable
import gg.norisk.ffa.server.mechanics.lootdrop.loottable.UHCLootdropLoottable
import gg.norisk.heroes.common.ffa.experience.ExperienceReason
import gg.norisk.heroes.common.ffa.experience.addXp
import kotlinx.coroutines.*
import net.minecraft.block.BarrelBlock
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.passive.ChickenEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.AffineTransformation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.Silk
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import org.joml.Vector3f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Lootdrop(private val world: ServerWorld, private val blockPos: BlockPos) {
    companion object {
        private val mainColor = Color(36, 173, 227).rgb
        private val secondaryColor = Color(150, 198, 207).rgb

        private val EXPIRATION_TIME = 3.minutes
        private val ITEMS_PER_AIR_DROP = 4..8
        private val BARREL_SLOTS = 0..26

        private val entityIdLootdropMap = HashMap<Int, Lootdrop>()
        private val posLootdropMap = HashMap<BlockPos, Lootdrop>()

        private val lootTable = if (KitEditor.isUHC()) UHCLootdropLoottable().init() else SoupLootdropLoottable().init()

        fun fallingBlockLanded(fallingBlock: FallingBlockEntity) {
            val lootdrop = entityIdLootdropMap[fallingBlock.id] ?: return
            lootdrop.onFallingBlockLanding(fallingBlock)
        }

        fun barrelOpened(barrelBlockEntity: BarrelBlockEntity, player: PlayerEntity) {
            val lootdrop = posLootdropMap[barrelBlockEntity.pos] ?: return
            lootdrop.onBarrelOpen(player)
        }

        fun projectileHit(projectile: PersistentProjectileEntity, entity: Entity) {
            val lootdrop = entityIdLootdropMap[entity.id] ?: return
            lootdrop.onProjectileHit(projectile, entity)
        }
    }

    private val lootdropCoroutine = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private var state: LootdropState = LootdropState.SPAWNING

    private val allEntities = mutableListOf<Entity>()
    private var barrelEntity = createBarrelEntity()
    private var balloonEntity = createBalloon()
    private var leashEntities = createLeashChickens()
    private var timerTextEntity = createTimerTextEntity()

    private var xpReward = 0

    private var landingTime: Long? = null
    private lateinit var landingPos: BlockPos

    fun drop() {
        startFallingAnimation()
    }

    private fun end() {
        mcCoroutineTask(sync = true, client = false) {
            allEntities.toList().forEach(::unregisterEntity)
            posLootdropMap.remove(landingPos)
        }
    }

    private fun startFallingAnimation() {
        allEntities.onEach { entity ->
            entity.setNoGravity(false)
            world.spawnEntity(entity)
        }

        leashEntities.first().attachLeash(leashEntities.last(), true)
        state = LootdropState.GLIDING

        lootdropCoroutine.launch {
            while (state == LootdropState.GLIDING) {
                val time = System.currentTimeMillis() / 500.0
                val swayX = sin(time) * 0.05
                val swayZ = cos(time) * 0.05

                allEntities.forEach {
                    val a = if (it is ChickenEntity) -0.12 else -0.08
                    it.velocity = Vec3d(swayX, a, swayZ)
                    it.velocityDirty = true
                }

                val particleLocation = barrelEntity.blockPos.toCenterPos()
                world.spawnParticles(
                    ParticleTypes.CLOUD,
                    particleLocation.x,
                    particleLocation.y,
                    particleLocation.z,
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.05
                )

                delay(50.milliseconds)
            }
        }

        lootdropCoroutine.launch {
            while (state == LootdropState.GLIDING || state == LootdropState.FREE_FALL) {
                barrelEntity.playSound(SoundEvents.ENTITY_PHANTOM_FLAP, 1.3f, 1.4f)

                if (System.currentTimeMillis().milliseconds.inWholeSeconds % 3 == 0L) {
                    world.server.executeSync {
                        val firework = FireworkRocketEntity(EntityType.FIREWORK_ROCKET, world)
                        firework.setPosition(barrelEntity.pos)
                        world.spawnEntity(firework)
                    }
                }

                delay(1.seconds)
            }
        }
    }

    fun onFallingBlockLanding(fallingBlock: FallingBlockEntity) {
        when (fallingBlock.id) {
            barrelEntity.id -> onBarrelLanding()
            balloonEntity.id -> onBalloonLanding()
        }
    }

    private fun onBarrelLanding() {
        val hardFall = state == LootdropState.FREE_FALL
        state = LootdropState.LANDED
        landingPos = barrelEntity.blockPos
        landingTime = System.currentTimeMillis()
        posLootdropMap[landingPos] = this

        playLandingSoundsAndParticles(hardFall)
        setBarrelAndContents()
        startExpirationTimer()
    }

    private fun onBalloonLanding() {
        leashEntities.forEach(::unregisterEntity)
        if (!balloonEntity.isRemoved) {
            balloonEntity.discard()
        }
    }

    fun onProjectileHit(projectile: PersistentProjectileEntity, entity: Entity) {
        if (entity.id == balloonEntity.id) {
            state = LootdropState.FREE_FALL
            projectile.onLanding()
            onBalloonLanding()
        }
    }

    fun onBarrelOpen(player: PlayerEntity) {
        state = LootdropState.OPENED
        if (xpReward > 0) {
            player.sendMessage(Text.translatable("ffa.mechanic.lootdrop.found_xp", xpReward))
            player.addXp(ExperienceReason("lootdrop_secured", xpReward))
        }
        end()
    }

    private fun onExpired() {
        state = LootdropState.EXPIRED
        world.setBlockState(landingPos, Blocks.AIR.defaultState)
        end()
    }

    private fun playLandingSoundsAndParticles(hardFall: Boolean) {
        val landingPos = landingPos

        world.spawnParticles(
            ParticleTypes.CAMPFIRE_COSY_SMOKE,
            landingPos.x + 0.5,
            landingPos.y + 1.5,
            landingPos.z + 0.5,
            20,
            0.5,
            1.0,
            0.5,
            0.02
        )

        if (hardFall) {
            world.playSound(
                null,
                landingPos,
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            )

            world.spawnParticles(
                ParticleTypes.EXPLOSION,
                landingPos.x + 0.5,
                landingPos.y + 1.0,
                landingPos.z + 0.5,
                10,
                0.5,
                0.5,
                0.5,
                0.1
            )
        } else {
            world.playSound(
                null,
                landingPos,
                SoundEvents.BLOCK_SNOW_FALL,
                SoundCategory.BLOCKS,
                0.9f,
                1.2f
            )
        }

        lootdropCoroutine.launch {
            while (state == LootdropState.LANDED) {
                val particleLocation = landingPos.toCenterPos()
                world.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    particleLocation.x,
                    particleLocation.y,
                    particleLocation.z,
                    10,
                    0.25,
                    0.25,
                    0.25,
                    0.005
                )
                delay(50.milliseconds)
            }
        }

        lootdropCoroutine.launch {
            while (state == LootdropState.LANDED) {
                val pitch = (3..12).random() / 10f

                world.playSound(
                    null,
                    landingPos,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    SoundCategory.BLOCKS,
                    2.5f,
                    pitch
                )
                delay((1500..3000).random().milliseconds)
            }
        }
    }

    private fun setBarrelAndContents() {
        world.setBlockState(landingPos, Blocks.BARREL.defaultState.with(BarrelBlock.FACING, Direction.UP))
        val barrel = world.getBlockEntity(landingPos) as? BarrelBlockEntity
        val loot = lootTable.generateLoot(ITEMS_PER_AIR_DROP.random())

        loot.forEach { item ->
            val amount = item.amountRange.random()
            when (item) {
                is ItemStackLootdropItem -> {
                    barrel?.setStack(BARREL_SLOTS.random(), item.itemStack.copyWithCount(amount))
                }

                is ExperienceLootdropItem -> xpReward += amount
            }
        }
        startShimmerEffect(world, landingPos)
    }

    private fun startShimmerEffect(world: ServerWorld, blockPos: BlockPos) {
        val entities = mutableListOf<TextDisplayEntity>()

        fun addSide(facing: Direction) {
            val pos = Vec3d(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
            val displayPos = when (facing) {
                Direction.NORTH -> pos.add(0.6, 0.0, -0.0001)
                Direction.SOUTH -> pos.add(0.4, 0.0, 1.0001)
                Direction.WEST -> pos.add(-0.0001, 0.0, 0.4)
                Direction.EAST -> pos.add(1.0001, 0.0, 0.6)
                Direction.UP -> pos.add(0.4, 1.0001, 1.0)
                Direction.DOWN -> pos.add(0.4, -0.0001, 0.0)
            }
            val yawRotation = when (facing) {
                Direction.NORTH -> 180f
                Direction.SOUTH -> 0f
                Direction.WEST -> 90f
                Direction.EAST -> -90f
                else -> 0f
            }
            val pitchRotation = when (facing) {
                Direction.UP -> -90f
                Direction.DOWN -> 90f
                else -> 0f
            }

            val textDisplay = TextDisplayEntity(EntityType.TEXT_DISPLAY, Silk.serverOrThrow.overworld).apply {
                setPosition(displayPos.x, displayPos.y, displayPos.z)
                yaw = yawRotation
                pitch = pitchRotation

                setText(" ".literal)
                setBackground(mainColor)

                val scale = Vector3f(8f, 3.625f, 8f)
                setTransformation(AffineTransformation(null, null, scale, null))
            }
            world.spawnEntity(textDisplay)
            entities.add(textDisplay)
            registerEntity(textDisplay)
        }

        listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN).forEach {
            addSide(it)
        }

        CoroutineScope(Dispatchers.IO).launch {
            shimmerEffect(entities)
        }
    }

    private suspend fun shimmerEffect(entities: List<TextDisplayEntity>) {
        var alpha = 0.0f
        var increasing = true
        val delayTime = 200L
        val maxAlpha = 0.66f
        val stepSize = 0.01f
        while (true) {
            entities.forEach { entity ->
                alpha = if (increasing) {
                    alpha + stepSize
                } else {
                    alpha - stepSize
                }

                if (alpha >= maxAlpha) {
                    alpha = maxAlpha
                    increasing = false
                } else if (alpha <= 0.0f) {
                    alpha = 0.0f
                    increasing = true
                    delay(delayTime)
                }
                val alphaInt = (alpha * 255).toInt()
                val rgbaColor = Color(36, 173, 227, alphaInt)
                entity.setBackground(rgbaColor.rgb)
            }
            delay(40)
        }
    }

    private fun createBarrelEntity(): FallingBlockEntity {
        val pos = blockPos.toCenterPos()
        return FallingBlockEntity(
            world,
            pos.x + 0.5,
            pos.y + 50.0,
            pos.z + 0.5,
            Blocks.BARREL.defaultState.with(BarrelBlock.FACING, Direction.UP)
        ).apply {
            setNoGravity(true)
            setDestroyedOnLanding()
            registerEntity(this)
            isGlowing = true
        }
    }

    private fun createBalloon(): FallingBlockEntity {
        val pos = barrelEntity.pos
        return FallingBlockEntity(
            world,
            pos.x,
            pos.y + 3.0,
            pos.z,
            Blocks.LIGHT_BLUE_CONCRETE.defaultState
        ).apply {
            setNoGravity(true)
            setDestroyedOnLanding()
            registerEntity(this)
        }
    }

    private fun createLeashChickens(): List<ChickenEntity> {
        val barrelChicken = ChickenEntity(EntityType.CHICKEN, world).apply {
            setPosition(barrelEntity.x, barrelEntity.y - 0.75, barrelEntity.z)
        }

        val balloonChicken = ChickenEntity(EntityType.CHICKEN, world).apply {
            setPosition(balloonEntity.x, balloonEntity.y, balloonEntity.z)
        }

        return listOf(barrelChicken, balloonChicken).onEach {
            it.apply {
                setNoGravity(true)
                isInvisible = true
                isSilent = true
                isInvulnerable = true
                addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, Int.MAX_VALUE, 0, true, false))
                addStatusEffect(StatusEffectInstance(StatusEffects.SLOWNESS, Int.MAX_VALUE, 200, true, false))
                attributes.getCustomInstance(EntityAttributes.GENERIC_SCALE)?.baseValue = 0.2
                goalSelector.goals.clear()
                registerEntity(this)
            }
        }
    }

    private fun createTimerTextEntity(): TextDisplayEntity {
        return TextDisplayEntity(EntityType.TEXT_DISPLAY, world).apply {
            setBillboardMode(DisplayEntity.BillboardMode.CENTER)
            registerEntity(this)
        }
    }

    private fun startExpirationTimer() {
        val pos = landingPos.toCenterPos().add(0.0, 1.0, 0.0)
        timerTextEntity.requestTeleport(pos.x, pos.y, pos.z)
        world.spawnEntity(timerTextEntity)

        lootdropCoroutine.launch {
            while (state == LootdropState.LANDED) {
                updateTimerText()
                delay(200.milliseconds)
            }
        }
    }

    private fun updateTimerText() {
        timerTextEntity.setText(literalText {
            text("Lootdrop") { color = mainColor; bold = true }
            newLine()
            val timeRemaining = (landingTime!! + EXPIRATION_TIME.inWholeMilliseconds - System.currentTimeMillis())

            if (timeRemaining <= 0 || world.getBlockState(landingPos).block != Blocks.BARREL) {
                onExpired()
                return
            }

            timeRemaining.milliseconds.toComponents { min, sec, _ ->
                text {
                    text(min.toString().padStart(2, '0')) { color = secondaryColor }
                    text(":")
                    text(sec.toString().padStart(2, '0')) { color = secondaryColor }
                }
            }
        })
    }

    private fun registerEntity(entity: Entity) {
        allEntities.add(entity)
        entityIdLootdropMap[entity.id] = this
    }

    private fun unregisterEntity(entity: Entity) {
        allEntities.remove(entity)
        entity.discard()
        entityIdLootdropMap.remove(entity.id)
    }
}

enum class LootdropState {
    SPAWNING, GLIDING, FREE_FALL, LANDED, OPENED, EXPIRED
}
