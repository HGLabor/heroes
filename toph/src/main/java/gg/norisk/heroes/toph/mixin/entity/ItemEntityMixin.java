package gg.norisk.heroes.toph.mixin.entity;

import gg.norisk.heroes.toph.ability.EarthArmorAbilityKt;
import gg.norisk.heroes.toph.entity.IBendingItemEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements IBendingItemEntity {
    private UUID bender;

    @Inject(method = "tick", at = @At("TAIL"))
    private void injected(CallbackInfo ci) {
        EarthArmorAbilityKt.moveToBender((ItemEntity) (Object) this);
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void onPlayerCollisionInjection(PlayerEntity playerEntity, CallbackInfo ci) {
        EarthArmorAbilityKt.handlePlayerCollision((ItemEntity) (Object) this, playerEntity, ci);
    }

    @Override
    public void setBender(UUID bender) {
        this.bender = bender;
    }

    @Nullable
    @Override
    public UUID getBender() {
        return bender;
    }
}
