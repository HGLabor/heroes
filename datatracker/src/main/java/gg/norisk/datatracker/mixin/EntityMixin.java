package gg.norisk.datatracker.mixin;

import gg.norisk.datatracker.entity.ISyncedEntity;
import gg.norisk.datatracker.entity.ISyncedEntityKt;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements ISyncedEntity {
    @Unique
    private final Map<String, Object> syncedValues = new HashMap<>();

    @NotNull
    @Override
    public Map<String, Object> getSyncedValuesMap() {
        return syncedValues;
    }

    //this is used for quest-api to trigger nbt condition like isSubwaySurfers
    @Inject(method = "writeNbt", at = @At("HEAD"))
    private void injected(NbtCompound nbtCompound, CallbackInfoReturnable<NbtCompound> cir) {
        ISyncedEntityKt.writeSyncedNbtData((Entity) (Object) this, nbtCompound);
    }
}
