package gg.norisk.heroes.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gg.norisk.heroes.common.HeroesManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureFlags.class)
public abstract class FeatureFlagsMixin {
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/featuretoggle/FeatureManager$Builder;build()Lnet/minecraft/resource/featuretoggle/FeatureManager;"))
    private static void heroapi$featureFlag(CallbackInfo ci, @Local FeatureManager.Builder builder) {
        HeroesManager.heroesFlag = builder.addFlag(HeroesManager.INSTANCE.toId("heroes"));
    }
}
