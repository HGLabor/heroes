package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.CombatTag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements CombatTag.ICombatPlayer {
    @Unique
    private int ffaCombatTicks;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "attack", constant = @Constant(floatValue = 1.5f))
    private float injected(float constant) {
        return 1.15f;
    }

    @Override
    public int getFfa_combatTicks() {
        return ffaCombatTicks;
    }

    @Override
    public void setFfa_combatTicks(int i) {
        this.ffaCombatTicks = i;
    }
}
