package gg.norisk.heroes.katara.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public abstract class BufferBuilderStorageMixin {
    @Shadow
    private static void assignBufferBuilder(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> object2ObjectLinkedOpenHashMap, RenderLayer renderLayer) {
    }

    @Inject(method = "method_54639", at = @At("TAIL"))
    private void injected(Object2ObjectLinkedOpenHashMap object2ObjectLinkedOpenHashMap, CallbackInfo ci) {
        //TODO 1.21.4 assignBufferBuilder(object2ObjectLinkedOpenHashMap, HealingWaterFeatureRenderer.Companion.getLAYER());
    }
}
