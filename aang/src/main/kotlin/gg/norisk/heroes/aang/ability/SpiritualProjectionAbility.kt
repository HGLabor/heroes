package gg.norisk.heroes.aang.ability

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.mojang.authlib.GameProfile
import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.emote.network.EmoteNetworking.stopEmote
import gg.norisk.heroes.aang.AangManager.Aang
import gg.norisk.heroes.aang.AangManager.toId
import gg.norisk.heroes.aang.ability.AirBallAbility.isAirBending
import gg.norisk.heroes.aang.ability.AirScooterAbility.isAirScooting
import gg.norisk.heroes.aang.ability.LevitationAbility.isAirLevitating
import gg.norisk.heroes.aang.client.sound.AirBendingLevitationSoundInstance
import gg.norisk.heroes.aang.client.sound.VelocityBasedFlyingSoundInstance
import gg.norisk.heroes.aang.entity.DummyPlayer
import gg.norisk.heroes.aang.entity.aang
import gg.norisk.heroes.aang.registry.EmoteRegistry
import gg.norisk.heroes.aang.registry.EmoteRegistry.toEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.client.renderer.RenderUtils
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.hero.ability.task.abilityCoroutineTask
import gg.norisk.heroes.common.hero.setHero
import gg.norisk.heroes.common.utils.sound
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.util.SkinTextures
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.world.TeleportTarget
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import org.apache.commons.lang3.RandomStringUtils
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

object SpiritualProjectionAbility {
    val LEVITATION_KEY = "IsSpiritualLevitating"
    val OVERLAY = "textures/misc/spiritual_vignette.png".toId()

    fun init() {
        syncedValueChangeEvent.listen { event ->
            if (!event.entity.world.isClient) return@listen
            if (LEVITATION_KEY == event.key) {
                val player = event.entity as? PlayerEntity? ?: return@listen
                if (player.isSpiritualLevitating) {
                    MinecraftClient.getInstance().soundManager.play(AirBendingLevitationSoundInstance(player))
                }
            }
            if (event.key == "IsSpiritualTransparent") {
                val player = event.entity as? PlayerEntity? ?: return@listen
                if (player.isSpiritualTransparent) {
                    MinecraftClient.getInstance().soundManager.play(VelocityBasedFlyingSoundInstance(player) {
                        (it as? PlayerEntity?)?.isSpiritualTransparent == true
                    })
                }
            }
        }

        UseBlockCallback.EVENT.register(UseBlockCallback { player, world, hand, hitResult ->
            if (player.isSpiritualTransparent && !world.isClient) {
                player.cancelSpiritMode()
                //TODO
                return@UseBlockCallback ActionResult.FAIL
            }
            return@UseBlockCallback ActionResult.PASS
        })

        UseItemCallback.EVENT.register(UseItemCallback { player, world, hand ->
            if (player.isSpiritualTransparent && !world.isClient) {
                player.cancelSpiritMode()
                //TODO
                return@UseItemCallback ActionResult.FAIL
            }
            return@UseItemCallback ActionResult.PASS
        })

        AttackEntityCallback.EVENT.register(AttackEntityCallback { player, world, hand, entity, hitResult ->
            if (player.isSpiritualTransparent && !world.isClient) {
                player.cancelSpiritMode()
                //TODO
                return@AttackEntityCallback ActionResult.FAIL
            }
            return@AttackEntityCallback ActionResult.PASS
        })

        PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { world, player, pos, state, blockEntity ->
            if (player.isSpiritualTransparent) {
                return@Before !player.cancelSpiritMode()
            }
            return@Before true
        })

        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
        command("aang") {
            literal("togglespiritualtransparency") {
                runs {
                    val player = this.source.playerOrThrow
                    player.isSpiritualTransparent = !player.isSpiritualTransparent
                }
            }
            literal("togglespirituallevitating") {
                runs {
                    val player = this.source.playerOrThrow
                    player.isSpiritualLevitating = !player.isSpiritualLevitating
                }
            }
        }
    }

    fun LivingEntity.getAlpha(): Float {
        val pulseSpeed = 10.0 // Bestimmt, wie schnell das Overlay pulsiert (höherer Wert = langsameres Pulsieren)
        return (Math.sin(age / pulseSpeed) * 0.25 + 0.75).toFloat() // Wert zwischen 0.5 und 1.0
    }

    fun initClient() {
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext, tickCounter ->
            val player = MinecraftClient.getInstance().player ?: return@HudRenderCallback
            if (player.isSpiritualLevitating || player.isSpiritualTransparent) {
                // Entity age verwenden, um einen kontinuierlichen Wert zu erhalten
                val pulseSpeed =
                    10.0 // Bestimmt, wie schnell das Overlay pulsiert (höherer Wert = langsameres Pulsieren)
                val alpha = (Math.sin(player.age / pulseSpeed) * 0.25 + 0.75).toFloat() // Wert zwischen 0.5 und 1.0
                RenderUtils.renderOverlay(drawContext, OVERLAY, alpha)
            }
        })
    }

    fun PlayerEntity.isUsingSpiritualProjection(): Boolean {
        return isSpiritualLevitating || isSpiritualTransparent
    }

    fun PlayerEntity.replaceNameWithOwner(args: Args) {
        val owner = world.getEntityById(spiritualOwner) as? PlayerEntity? ?: return
        args.set(1, owner.gameProfile.name.literal)
    }

    fun AbstractClientPlayerEntity.replaceSkinWithOwner(original: SkinTextures): SkinTextures {
        val owner = world.getEntityById(spiritualOwner) as? AbstractClientPlayerEntity? ?: return original
        return owner.skinTextures
    }

    fun PlayerEntity.replaceDataTrackerWithOwner(original: Operation<DataTracker>): DataTracker {
        val owner = world.getEntityById(spiritualOwner) as? PlayerEntity? ?: return original.call(this)
        return owner.dataTracker
    }

    fun DummyPlayer.cancelProjection(reason: Entity?) {
        val owner = world.getEntityById(spiritualOwner) as? ServerPlayerEntity?
        owner?.isSpiritualLevitating = false
        owner?.isSpiritualTransparent = false
        owner?.teleportTo(
            TeleportTarget(
                world as ServerWorld,
                this.pos, velocity, yaw, pitch, TeleportTarget.NO_OP
            )
        )
        owner?.abilities?.flying = false
        owner?.abilities?.allowFlying = false
        owner?.sendAbilitiesUpdate()
        owner?.sound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.2f, 2f)
        discard()
    }

    private fun ServerPlayerEntity.spawnFakePlayer() {
        val fakePlayer = DummyPlayer(
            world, blockPos, pitch, GameProfile(UUID(0, Random.nextLong()), RandomStringUtils.randomAlphabetic(16))
        )
        fakePlayer.updatePositionAndAngles(this.pos.x, this.pos.y, this.pos.z, this.yaw, this.pitch)
        fakePlayer.setNoGravity(true)
        fakePlayer.spiritualOwner = this.id
        fakePlayer.isSpiritualLevitating = true
        fakePlayer.setHero(Aang)
        world.spawnEntity(fakePlayer)
        mcCoroutineTask(sync = true, client = false, delay = 1.ticks) {
            fakePlayer.playEmote("spiritual_projection_loop".toEmote())
        }
    }

    fun PlayerEntity.handleTick() {
        if (isSpiritualTransparent) {
            noClip = isSpiritualTransparent

            if (!world.isClient) {
                val body = (this as ServerPlayerEntity).serverWorld
                    .iterateEntities()
                    .filterIsInstance<DummyPlayer>()
                    .filter { it.spiritualOwner == this.id }
                    .randomOrNull()
                if (body != null) {
                    val distance = body.distanceTo(this)
                    if (distance > projectionMaxDistance.getValue(this.uuid).toFloat()) {
                        sendMessage(Text.translatable("heroes.katara.ability.spiritual_projection.too_far_away"))
                        cancelSpiritMode(body)
                    }
                }
            }
        }
        if (isSpiritualTransparent) {
            world.addParticle(
                StatusEffects.LEVITATION.value().createParticle(StatusEffectInstance(StatusEffects.LEVITATION)),
                this.getParticleX(0.5),
                this.randomBodyY,
                this.getParticleZ(0.5),
                1.0,
                1.0,
                1.0
            )
        }
    }

    private fun PlayerEntity.cancelSpiritMode(toClear: DummyPlayer? = null): Boolean {
        if (this.world.isClient) return false
        val body = toClear ?: (this as ServerPlayerEntity).serverWorld
            .iterateEntities()
            .filterIsInstance<DummyPlayer>()
            .filter { it.spiritualOwner == this.id }
            .randomOrNull()
        if (body != null) {
            Ability.addCooldown(this)
            body.cancelProjection(null)
            return true
        }
        return false
    }

    var PlayerEntity.isSpiritualTransparent: Boolean
        get() = this.getSyncedData<Boolean>("IsSpiritualTransparent") ?: false
        set(value) = this.setSyncedData("IsSpiritualTransparent", value)

    var PlayerEntity.spiritualOwner: Int
        get() = this.getSyncedData<Int>("SpiritualOwnerId") ?: -1
        set(value) = this.setSyncedData("SpiritualOwnerId", value)

    var PlayerEntity.isSpiritualLevitating: Boolean
        get() = this.getSyncedData<Boolean>(LEVITATION_KEY) ?: false
        set(value) = this.setSyncedData(LEVITATION_KEY, value)

    val projectionMaxDistance = NumberProperty(
        25.0, 5,
        "Spiritual Projection Max Distance",
        AddValueTotal(10.0, 10.0, 10.0, 10.0, 10.0)
    ).apply {
        icon = {
            Components.item(Items.SPYGLASS.defaultStack)
        }
    }

    val Ability = object : PressAbility("Spiritual Projection") {
        init {
            client {
                this.keyBind = HeroKeyBindings.thirdKeyBind
            }

            this.cooldownProperty =
                buildCooldown(10.0, 5, AddValueTotal(-1.0, -1.0, -1.0, -1.0, -1.0))

            this.properties = listOf(projectionMaxDistance)
        }

        override fun canUse(player: ServerPlayerEntity): Boolean {
            if (player.isAirScooting) {
                return false
            }

            if (player.hasVehicle()) {
                return false
            }

            if (player.isAirBending) {
                return false
            }

            if (player.isAirLevitating) {
                return false
            }

            return super.canUse(player)
        }

        override fun getIconComponent(): Component {
            return Components.item(Items.BLUE_STAINED_GLASS.defaultStack)
        }

        override fun getUnlockCondition(): Text {
            return literalText {
                text(Text.translatable("heroes.ability.$internalKey.unlock_condition"))
            }
        }

        override fun hasUnlocked(player: PlayerEntity): Boolean {
            return player.isCreative || (LevitationAbility.Ability.cooldownProperty.isMaxed(player.uuid) && LevitationAbility.Ability.maxDurationProperty.isMaxed(
                player.uuid
            ))
        }

        override fun getBackgroundTexture(): Identifier {
            return Identifier.of("textures/block/quartz_block_bottom.png")
        }

        override fun onDisable(player: PlayerEntity) {
            super.onDisable(player)
            player.cancelSpiritMode()
            player.stopLevitation()
        }

        private fun PlayerEntity.stopLevitation() {
            aang.aang_spiritualProjectionsTasks.forEach { it.cancel() }
            isSpiritualLevitating = false
            isSpiritualTransparent = false
            removeStatusEffect(StatusEffects.LEVITATION)
            (this as? ServerPlayerEntity)?.stopEmote(EmoteRegistry.SPIRITUAL_PROJECTION_START)
        }

        override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
            super.onStart(player, abilityScope)
            if (player is ServerPlayerEntity) {
                abilityScope.cancelCooldown()
                if (player.cancelSpiritMode()) {
                    return
                }
                if (!player.isSpiritualLevitating) {
                    player.isSpiritualTransparent = false
                    player.isSpiritualLevitating = true
                    player.abilities.flying = false
                    player.abilities.allowFlying = false
                    player.addStatusEffect(
                        StatusEffectInstance(
                            StatusEffects.LEVITATION,
                            2.12.seconds.inWholeMilliseconds.toInt() / 50
                        )
                    )
                    player.playEmote(EmoteRegistry.SPIRITUAL_PROJECTION_START)
                    player.aang.aang_spiritualProjectionsTasks += abilityCoroutineTask(
                        sync = true,
                        client = false,
                        delay = 2.12.seconds,
                        executingPlayer = player
                    ) {
                        player.isSpiritualTransparent = true
                        player.isSpiritualLevitating = false
                        player.abilities.flying = true
                        player.abilities.allowFlying = true
                        player.sendAbilitiesUpdate()
                        player.spawnFakePlayer()
                        player.sound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.2, 2f)
                        player.modifyVelocity(0.0, 1.0, 0.0)
                    }
                } else {
                    abilityScope.applyCooldown()
                    player.stopLevitation()
                }
            }
        }
    }
}
