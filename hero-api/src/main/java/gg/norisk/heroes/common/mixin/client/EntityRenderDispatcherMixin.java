package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.norisk.heroes.common.events.EntityEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private boolean onlyRenderIfAllowed(EntityRenderer<Entity> targetClass, Entity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        var event = new EntityEvents.EntityRendererEvent(entity, f, g, matrixStack, vertexConsumerProvider, i);
        EntityEvents.INSTANCE.getEntityRendererEvent().invoke(event);
        return !event.isCancelled().get();
    }
}
