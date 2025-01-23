package gg.norisk.heroes.common.mixin;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = ResourcePackManager.class, priority = 2000)
public abstract class ResourcePackManagerMixin {
    @Mutable
    @Shadow
    @Final
    private Set<ResourcePackProvider> providers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void heroapi$resourcePack(ResourcePackProvider[] resources, CallbackInfo ci) {
        //this.providers.add(new HeroDataPackProvider(new SymlinkFinder(path -> true)));
    }
}
