package gg.norisk.heroes.toph.ability

import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.heroes.client.option.HeroKeyBindings
import gg.norisk.heroes.common.HeroesManager.client
import gg.norisk.heroes.common.ability.NumberProperty
import gg.norisk.heroes.common.ability.operation.AddValueTotal
import gg.norisk.heroes.common.hero.ability.AbilityScope
import gg.norisk.heroes.common.hero.ability.implementation.PressAbility
import gg.norisk.heroes.common.networking.BoomShake
import gg.norisk.heroes.common.networking.Networking.mousePacket
import gg.norisk.heroes.common.networking.cameraShakePacket
import gg.norisk.heroes.common.utils.RaycastUtils
import gg.norisk.heroes.common.utils.random
import gg.norisk.heroes.common.utils.sound
import gg.norisk.heroes.toph.TophManager.isEarthBlock
import gg.norisk.heroes.toph.TophManager.toEmote
import gg.norisk.heroes.toph.entity.BendingBlockEntity
import gg.norisk.heroes.toph.entity.BendingBlockEntity.Companion.owner
import gg.norisk.heroes.toph.registry.ParticleRegistry
import gg.norisk.heroes.toph.registry.SoundRegistry
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.core.Component
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.entity.modifyVelocity
import kotlin.random.Random

val earthPushDamage = NumberProperty(2.0, 3, "Damage", AddValueTotal(1.25, 1.25, 2.0)).apply {
    icon = {
        Components.item(Items.STONE_SWORD.defaultStack)
    }
}
val EarthPushAbility = object : PressAbility("Earth Push") {

    init {
        client {
            this.keyBind = HeroKeyBindings.pickItemKeyBinding
        }

        this.properties = listOf(earthPushDamage)

        this.cooldownProperty =
            buildCooldown(10.0, 5, AddValueTotal(-0.1, -0.4, -0.2, -0.8, -1.5, -1.0))

        mousePacket.receiveOnServer { packet, context ->
            val player = context.player
            val world = player.serverWorld
            if (packet.isLeft() && packet.isClicked()) {
                val entity = player.getForcedBlocks().filterIsInstance<BendingBlockEntity>().filter {
                    it.distanceTo(player) < 6.0
                }.randomOrNull()
                    ?: return@receiveOnServer

                cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
                player.sound(SoundRegistry.STONE_SMASH)
                player.sound(SoundRegistry.EARTH_COLUMN_1)
                entity.forcePush(player)
                player.playEmote("earth-kick".toEmote())
            }
        }
    }

    override fun getIconComponent(): Component {
        return Components.item(Items.DIRT.defaultStack)
    }

    override fun getBackgroundTexture(): Identifier {
        return Identifier.of("textures/block/packed_mud.png")
    }

    override fun onStart(player: PlayerEntity, abilityScope: AbilityScope) {
        super.onStart(player, abilityScope)
        if (player is ServerPlayerEntity) {
            val world = player.world as ServerWorld
            val clipWithDistance = RaycastUtils.clipWithDistance(player, player.world, 4.5) ?: return
            val pos = clipWithDistance.blockPos.toCenterPos()
            val state = world.getBlockState(clipWithDistance.blockPos)
            abilityScope.cancelCooldown()
            if (!state.isEarthBlock) return
            abilityScope.applyCooldown()
            val entity = BendingBlockEntity(world, pos.x, pos.y, pos.z, state)
            cameraShakePacket.send(BoomShake(0.1, 0.2, 0.4), player as ServerPlayerEntity)
            entity.canAttack = true
            entity.owner = player.uuid
            world.spawnEntity(entity)
            world.playSound(
                null,
                clipWithDistance.blockPos,
                state.soundGroup.breakSound,
                SoundCategory.BLOCKS,
                0.3f,
                Random.nextDouble().toFloat() * 0.8f
            )
            player.sound(SoundRegistry.EARTH_COLUMN_1)
            world.breakBlock(clipWithDistance.blockPos, false, player)
            entity.modifyVelocity(Vec3d(0.0, 0.5, 0.0))
            (world as? ServerWorld?)?.spawnParticles(
                ParticleRegistry.EARTH_DUST,
                pos.x,
                pos.y + 1,
                pos.z,
                7,
                (0.01..0.04).random(),
                (0.01..0.04).random(),
                (0.01..0.04).random(),
                (0.01..0.04).random()
            )
        }
    }
}

fun ServerPlayerEntity.getForcedBlocks(): List<FallingBlockEntity> {
    return serverWorld.iterateEntities()
        .filterIsInstance<BendingBlockEntity>()
        .filter { it.owner == this.uuid }
        .toList()
}
