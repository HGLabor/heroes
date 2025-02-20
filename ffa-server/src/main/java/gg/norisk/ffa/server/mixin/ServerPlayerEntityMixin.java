package gg.norisk.ffa.server.mixin;

import com.mojang.authlib.GameProfile;
import gg.norisk.ffa.server.mechanics.KitEditor;
import gg.norisk.ffa.server.mechanics.SoupHealing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Inject(
            method = "swingHand",
            at = @At("HEAD")
    )
    public void onSwing(Hand hand, CallbackInfo ci) {
        if (!KitEditor.INSTANCE.isUHC()) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            boolean eatenSoup = SoupHealing.INSTANCE.potentialSoupUse(player, player.getStackInHand(hand).getItem());
            if (eatenSoup) {
                player.setStackInHand(Hand.MAIN_HAND, Items.BOWL.getDefaultStack());
            }
        }
    }
}
