package gg.norisk.heroes.toph.mixin.entity;

import gg.norisk.heroes.common.networking.dto.AnimationInterpolator;
import gg.norisk.heroes.toph.entity.ITrappedEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements ITrappedEntity {
    @Nullable
    @Unique
    private AnimationInterpolator earthRotationAnimation;

    @Unique
    @Nullable
    @Override
    public AnimationInterpolator getEarthRotationAnimation() {
        return earthRotationAnimation;
    }

    @Unique
    @Override
    public void setEarthRotationAnimation(@Nullable AnimationInterpolator animationInterpolator) {
        earthRotationAnimation = animationInterpolator;
    }
}
