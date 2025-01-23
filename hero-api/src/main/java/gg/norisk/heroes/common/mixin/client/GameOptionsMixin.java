package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.client.ui.screen.HeroSelectorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @ModifyReturnValue(method = "getPerspective", at = @At("RETURN"))
    private Perspective heroapi$GetPerspective(Perspective original) {
        if (MinecraftClient.getInstance().currentScreen instanceof HeroSelectorScreen) {
            return Perspective.THIRD_PERSON_FRONT;
        }
        return original;
    }
}