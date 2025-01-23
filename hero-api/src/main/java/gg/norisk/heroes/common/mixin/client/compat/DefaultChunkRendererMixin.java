package gg.norisk.heroes.common.mixin.client.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.norisk.heroes.client.ui.screen.HeroSelectorScreen;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public abstract class DefaultChunkRendererMixin extends ShaderChunkRenderer {
    public DefaultChunkRendererMixin(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @ModifyExpressionValue(
            remap = false,
            method = "render",
            at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z", remap = false)
    )
    private boolean ffa$blockFaceCulling(boolean original) {
        return original && !(MinecraftClient.getInstance().currentScreen instanceof HeroSelectorScreen);
    }
}
