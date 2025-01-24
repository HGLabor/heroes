package gg.norisk.ffa.server.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastAttackTime")
    public int getLastAttackTime();

    @Accessor("lastAttackTime")
    public void setLastAttackTime(int value);

    @Accessor("attacking")
    public @Nullable LivingEntity getAttacking();

    @Accessor("attacking")
    public void setAttacking(@Nullable LivingEntity value);
}
