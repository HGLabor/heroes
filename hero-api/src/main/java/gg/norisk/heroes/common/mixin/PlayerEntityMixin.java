package gg.norisk.heroes.common.mixin;

import gg.norisk.heroes.common.hero.IHeroManagerKt;
import gg.norisk.heroes.common.hero.ability.AbstractAbility;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInjection(CallbackInfo ci) {
        var player = (PlayerEntity) (Object) this;
        var hero = IHeroManagerKt.getHero(player);
        if (hero == null) return;
        for (AbstractAbility<?> ability : hero.getAbilities().values()) {
            ability.onTick(player);
        }
    }
}
