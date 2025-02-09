package gg.norisk.heroes.toph.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("children")
    Map<String, ModelPart> getChildren();

    @Accessor("cuboids")
    List<ModelPart.Cuboid> getCuboids();

    @Invoker("renderCuboids")
    void invokeRenderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int i, int j, int k);
}
