package gg.norisk.heroes.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import gg.norisk.heroes.common.HeroesManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "loadDataPacks(Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/resource/DataConfiguration;ZZ)Lnet/minecraft/resource/DataConfiguration;", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private static void heroapi$forceDataPack(ResourcePackManager resourcePackManager, DataConfiguration dataConfiguration, boolean bl, boolean bl2, CallbackInfoReturnable<DataConfiguration> cir,
                                              @Local Set<String> set, @Local ResourcePackProfile profile
    ) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            HeroesManager.INSTANCE.getLogger().info("Found Datapack {}", profile.getId());
            if ("heroes".equalsIgnoreCase(profile.getId())) {
                set.add(profile.getId());
            }
        }
    }
}
