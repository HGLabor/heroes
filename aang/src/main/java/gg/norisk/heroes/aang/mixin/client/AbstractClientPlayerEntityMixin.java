package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
    public AbstractClientPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @ModifyReturnValue(
            method = "getSkinTextures",
            at = @At("RETURN")
    )
    private SkinTextures replaceSkinWithOwner(SkinTextures original) {
        return SpiritualProjectionAbility.INSTANCE.replaceSkinWithOwner((AbstractClientPlayerEntity) (Object) this, original);
    }
}
