package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.ext.IDamageTrackerExt;
import gg.norisk.ffa.server.mechanics.CombatTag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin implements IDamageTrackerExt {
    @Shadow
    @Final
    private LivingEntity entity;
    private PlayerEntity lastPlayer;

    @Inject(
            method = "onDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dontApplyKillDamage(DamageSource damageSource, float f, CallbackInfo ci) {
        PlayerEntity attacker = null;
        if (damageSource.getAttacker() instanceof PlayerEntity player && entity != player) {
            lastPlayer = player;
            attacker = player;
        }
        //System.out.println("### APPLYING " + damageSource);
        if (damageSource.isOf(DamageTypes.GENERIC_KILL)) {
            ci.cancel();
        } else if (attacker == null) {
            //only player combat should trigger combat logger
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

    @Override
    public @Nullable PlayerEntity getFfa_lastPlayer() {
        return lastPlayer;
    }

    @Override
    public void setFfa_lastPlayer(@Nullable PlayerEntity player) {
        lastPlayer = player;
    }
}
