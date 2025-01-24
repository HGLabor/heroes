package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.CombatTag;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {
    @Inject(
            method = "onDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dontApplyKillDamage(DamageSource damageSource, float f, CallbackInfo ci) {
        //System.out.println("### APPLYING " + damageSource);
        if (damageSource.isOf(DamageTypes.GENERIC_KILL)) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 300))
    private int modifyCombatTagTime(int constant) {
        return CombatTag.INSTANCE.getTicks();
    }

    @ModifyConstant(method = "update", constant = @Constant(intValue = 100))
    private int modifyCombatTagTime2(int constant) {
        return CombatTag.INSTANCE.getTicks();
    }
}
