package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.lootdrop.Lootdrop;
import net.minecraft.block.Block;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    @Inject(method = "onDestroyedOnLanding", at = @At("HEAD"))
    private void onDestroyedOnLanding(Block block, BlockPos blockPos, CallbackInfo ci) {
        Lootdrop.Companion.fallingBlockLanded((FallingBlockEntity) (Object) this);
    }
}
