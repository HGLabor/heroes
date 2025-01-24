package gg.norisk.heroes.aang.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.aang.ability.AirScooterAbility;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyReturnValue(
            method = "calculateBoundingBox",
            at = @At("RETURN")
    )
    private Box airScooterBoxInjection(Box original) {
        return AirScooterAbility.INSTANCE.handleBox((Entity) (Object) this, original);
    }
}
