package gg.norisk.heroes.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.common.events.EntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void tickMovementEvent(CallbackInfo ci) {
        EntityEvents.INSTANCE.getLivingEntityTickMovementEvent().invoke(new EntityEvents.LivingEntityEvent((LivingEntity) (Object) this));
    }


    @ModifyReturnValue(method = "computeFallDamage", at = @At("RETURN"))
    private int injected(int original, float fallDistance, float damageMultiplier) {
        var event = new EntityEvents.ComputeFallDamageEvent(fallDistance, damageMultiplier, original, (LivingEntity) (Object) this);
        EntityEvents.INSTANCE.getComputeFallDamageEvent().invoke(event);
        if (event.getFallDamage() != null) {
            return event.getFallDamage();
        } else {
            return original;
        }
    }
}
