package gg.norisk.heroes.toph.mixin.render;

import gg.norisk.heroes.toph.ability.SeismicSenseAbilityKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    protected abstract void setPostProcessor(Identifier id);

    @Inject(at = @At("HEAD"), method = "togglePostProcessorEnabled", cancellable = true)
    private void togglePostProcessorEnabledInjection(CallbackInfo ci) {
        SeismicSenseAbilityKt.handleSeismicSenseShader(ci);
    }

    @Inject(at = @At("TAIL"), method = "onCameraEntitySet")
    private void onCameraEntitySet(Entity entity, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && SeismicSenseAbilityKt.getHasSeismicSense(player)) {
            setPostProcessor(SeismicSenseAbilityKt.getSeismicSenseShader());
        }
    }
}
