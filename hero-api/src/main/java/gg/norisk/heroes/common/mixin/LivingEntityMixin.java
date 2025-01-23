package gg.norisk.heroes.common.mixin;

import gg.norisk.heroes.common.events.EntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void tickMovementEvent(CallbackInfo ci) {
        EntityEvents.INSTANCE.getLivingEntityTickMovementEvent().invoke(new EntityEvents.LivingEntityEvent((LivingEntity) (Object) this));
    }
}
