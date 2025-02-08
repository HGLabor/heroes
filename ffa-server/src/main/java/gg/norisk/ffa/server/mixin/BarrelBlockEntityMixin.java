package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.lootdrop.Lootdrop;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BarrelBlockEntity.class)
public class BarrelBlockEntityMixin {
    @Inject(method = "onOpen", at = @At("HEAD"))
    private void onBarrelOpen(PlayerEntity playerEntity, CallbackInfo ci) {
        Lootdrop.Companion.barrelOpened((BarrelBlockEntity) (Object) this, playerEntity);
    }
}
