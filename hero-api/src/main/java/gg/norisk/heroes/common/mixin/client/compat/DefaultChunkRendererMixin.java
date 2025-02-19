package gg.norisk.heroes.common.mixin.client.compat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.norisk.heroes.client.ui.OrthoCamera;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer.class, remap = false)
public abstract class DefaultChunkRendererMixin extends ShaderChunkRenderer {

    public DefaultChunkRendererMixin(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @ModifyExpressionValue(
            remap = false,
            method = "render",
            at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z", remap = false)
    )
    private boolean ffa$blockFaceCulling(boolean original) {
        return original && !(OrthoCamera.INSTANCE.isEnabled());
    }
}
