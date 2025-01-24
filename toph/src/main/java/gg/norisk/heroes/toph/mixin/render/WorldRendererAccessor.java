package gg.norisk.heroes.toph.mixin.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Invoker("drawBlockOutline")
    void invokeDrawBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState);
}
