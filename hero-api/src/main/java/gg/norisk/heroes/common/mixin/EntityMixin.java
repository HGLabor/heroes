package gg.norisk.heroes.common.mixin;

import gg.norisk.heroes.common.events.EntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Unique
    private final Map<String, Object> syncedValues = new HashMap<>();

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void injected(TrackedData<?> trackedData, CallbackInfo ci) {
        EntityEvents.INSTANCE.getOnTrackedDataSetEvent().invoke(new EntityEvents.EntityTrackedDataSetEvent((Entity) (Object) this, trackedData));
    }
}
