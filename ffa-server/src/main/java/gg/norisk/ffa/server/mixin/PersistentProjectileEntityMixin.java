package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.lootdrop.Lootdrop;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onDestroyedOnLanding(EntityHitResult entityHitResult, CallbackInfo ci) {
        Lootdrop.Companion.projectileHit((PersistentProjectileEntity) (Object) this, entityHitResult.getEntity());
    }
}
