package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.lootdrop.Lootdrop;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BarrelBlockEntity.class)
public class BarrelBlockEntityMixin {

    @Inject(method = "setOpen", at = @At("HEAD"))
    private void onBarrelOpen(BlockState blockState, boolean open, CallbackInfo ci) {
        if (open) {
            Lootdrop.Companion.barrelOpened((BarrelBlockEntity) (Object) this);
        }
    }
}
