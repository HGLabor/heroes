package gg.norisk.heroes.toph.mixin;

import gg.norisk.heroes.toph.ability.SeismicSenseAbilityKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "hasOutline", at = @At("RETURN"), cancellable = true)
    private void injected(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        SeismicSenseAbilityKt.handleSeismicSenseOutline(entity, cir);
    }
}
