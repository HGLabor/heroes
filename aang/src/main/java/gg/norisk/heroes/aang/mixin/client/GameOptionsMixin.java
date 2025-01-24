package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.aang.ability.TornadoAbility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @ModifyReturnValue(
            method = "getPerspective",
            at = @At("RETURN")
    )
    private Perspective aang$tornadoStaticPerspective(Perspective original) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && TornadoAbility.INSTANCE.isTornadoMode(player)) {
            return Perspective.THIRD_PERSON_BACK;
        }
        return original;
    }
}
