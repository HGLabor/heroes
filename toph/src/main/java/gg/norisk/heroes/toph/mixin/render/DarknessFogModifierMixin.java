package gg.norisk.heroes.toph.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.toph.ability.SeismicSenseAbilityKt;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BackgroundRenderer.DarknessFogModifier.class)
public abstract class DarknessFogModifierMixin {
   /* @ModifyConstant(method = "applyStartEndModifier", constant = @Constant(floatValue = 15.0F))
    private float injected(float constant, BackgroundRenderer.FogData fogData, LivingEntity livingEntity, StatusEffectInstance statusEffectInstance, float f, float g) {
        if (livingEntity instanceof PlayerEntity player) {
            var value = SeismicSenseAbilityKt.handleSeismicSenseDarkness(player);
            System.out.println("Returning " + (value == -1f ? constant : value));
            return value == -1f ? constant : value;
        }
        return constant;
    } */

    @ModifyExpressionValue(
            method = "applyStartEndModifier",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F")
    )
    private float halveSpeed(float original, BackgroundRenderer.FogData fogData, LivingEntity livingEntity, StatusEffectInstance statusEffectInstance, float f, float g) {
        if (livingEntity instanceof PlayerEntity player) {
            var value = SeismicSenseAbilityKt.handleSeismicSenseDarkness(player, original);
            return value;
        }
        return original;
    }
}
