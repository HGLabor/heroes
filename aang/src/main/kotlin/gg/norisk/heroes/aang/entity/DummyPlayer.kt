package gg.norisk.heroes.aang.entity

import com.mojang.authlib.GameProfile
import gg.norisk.emote.network.EmoteNetworking.emoteS2CPacket
import gg.norisk.emote.network.EmoteSync
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility.cancelProjection
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.apache.commons.lang3.RandomStringUtils
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import java.util.*

class DummyPlayer(
    world: World,
    blockPos: BlockPos,
    f: Float,
    gameProfile: GameProfile
) : PlayerEntity(
    world,
    blockPos,
    f,
    gameProfile
) {
    override fun isSpectator(): Boolean = false
    override fun isCreative(): Boolean = false

    override fun tick() {
        super.tick()
    }

    override fun handleAttack(entity: Entity): Boolean {
        cancelProjection(entity)
        return super.handleAttack(entity)
    }

    override fun interactAt(playerEntity: PlayerEntity, vec3d: Vec3d, hand: Hand): ActionResult {
        if (hand == Hand.MAIN_HAND) {
            cancelProjection(playerEntity)
        }
        return super.interactAt(playerEntity, vec3d, hand)
    }

    fun playEmote(emote: Identifier) {
        emoteS2CPacket.sendToAll(EmoteSync(this.id, emote, EmoteSync.State.PLAY))
    }

    fun stopEmote(emote: Identifier) {
        emoteS2CPacket.sendToAll(EmoteSync(this.id, emote, EmoteSync.State.STOP))
    }

    companion object {
        fun UUID.isFakeUUID(): Boolean {
            return this.toString().startsWith("00000000-0000-0000")
        }

        fun handleDummyPlayerSpawn(
            packet: EntitySpawnS2CPacket,
            callback: CallbackInfoReturnable<Entity>,
            world: ClientWorld
        ) {
            if (packet.uuid.isFakeUUID()) {
                val player = OtherClientPlayerEntity(
                    world,
                    GameProfile(packet.uuid, RandomStringUtils.randomAlphabetic(16))
                )
                callback.setReturnValue(player)
            }
        }
    }
}
