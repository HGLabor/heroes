package gg.norisk.ffa.server.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.ffa.server.mechanics.CombatTag;
import gg.norisk.ffa.server.mechanics.KitEditor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements CombatTag.ICombatPlayer {
    @Shadow
    @NotNull
    public abstract ItemStack getWeaponStack();

    @Shadow public abstract void remove(RemovalReason reason);

    @Unique
    private int ffaCombatTicks;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "attack", constant = @Constant(floatValue = 1.5f))
    private float injected(float constant) {
        if (KitEditor.INSTANCE.isUHC()) {
            return constant;
        }
        return 1.18f;
    }

    @WrapOperation(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", ordinal = 0)
    )
    private double axeDamageNerf(PlayerEntity instance, RegistryEntry<?> registryEntry, Operation<Double> original) {
        var originalValue = original.call(instance, registryEntry);
        if (KitEditor.INSTANCE.isUHC()) {
            return originalValue;
        } else {
            if (getWeaponStack().isIn(ItemTags.AXES)) {
                return originalValue / 4;
            } else {
                return originalValue;
            }
        }
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
