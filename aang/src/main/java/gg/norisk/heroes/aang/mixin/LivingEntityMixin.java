package gg.norisk.heroes.aang.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.aang.ability.AirScooterAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(
            method = "travel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", ordinal = 2)
    )
    private boolean isOnGroundInjection(boolean original) {
        return AirScooterAbility.INSTANCE.handleDrag((Entity) (Object) this) || original;
    }

    @ModifyExpressionValue(
            method = "getMovementSpeed(F)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z")
    )
    private boolean isOnGroundInjection2(boolean original) {
        return AirScooterAbility.INSTANCE.handleDrag((Entity) (Object) this) || original;
    }

    @ModifyReturnValue(
            method = "canWalkOnFluid",
            at = @At(value = "RETURN")
    )
    private boolean canWalkOnFluidInjection(boolean original) {
        return AirScooterAbility.INSTANCE.handleDrag((Entity) (Object) this) || original;
    }
}
