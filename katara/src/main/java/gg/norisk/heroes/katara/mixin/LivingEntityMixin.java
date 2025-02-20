package gg.norisk.heroes.katara.mixin;

import gg.norisk.heroes.katara.KataraManager;
import gg.norisk.heroes.katara.entity.IceShardEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArgs(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    private void injected(Args args, ServerWorld world, DamageSource source, float amount) {
        if (source.getSource() instanceof IceShardEntity) {
            KataraManager.INSTANCE.getLogger().info("Shield auf Damage wurde halbiert");
            float damage = args.get(0);
            args.set(0, damage / 2f);
        }
    }
}
