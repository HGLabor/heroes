package gg.norisk.heroes.katara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.katara.entity.IKataraEntity;
import gg.norisk.heroes.katara.ability.WaterCircleAbilityV2;
import gg.norisk.heroes.katara.utils.EntityCircleTracker;
import gg.norisk.heroes.katara.utils.EntitySpinTracker;
import kotlinx.coroutines.Job;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin implements IKataraEntity {
    @Unique
    private Job waterHealingJob;
    @Unique
    private EntitySpinTracker entitySpinTracker = new EntitySpinTracker();
    @Unique
    private EntityCircleTracker entityCircleTracker = new EntityCircleTracker();

    @ModifyReturnValue(
            method = "getProjectileDeflection",
            at = @At("RETURN")
    )
    private ProjectileDeflection katara$projectileReflection(ProjectileDeflection original) {
        if ((Object) this instanceof PlayerEntity player) {
            if (WaterCircleAbilityV2.INSTANCE.breakWaterCirclePiece(player)) {
                return ProjectileDeflection.REDIRECTED;
            }
        }
        return original;
    }

    @Override
    public @Nullable Job getKatara_waterHealingJob() {
        return waterHealingJob;
    }

    @Override
    public void setKatara_waterHealingJob(@Nullable Job job) {
        waterHealingJob = job;
    }

    @Override
    public @NotNull EntitySpinTracker getKatara_entitySpinTracker() {
        return entitySpinTracker;
    }

    @Override
    public @NotNull EntityCircleTracker getKatara_entityCircleTracker() {
        return entityCircleTracker;
    }
}
