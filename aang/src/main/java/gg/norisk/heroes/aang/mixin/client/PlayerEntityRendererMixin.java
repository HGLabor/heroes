package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")
    )
    private RenderLayer makeSpiritualHandTransparent(Identifier identifier, Operation<RenderLayer> original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, ModelPart modelPart, ModelPart modelPart2) {
        if (SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(abstractClientPlayerEntity)) {
            return RenderLayer.getItemEntityTranslucentCull(identifier);
        } else {
            return original.call(identifier);
        }
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")
    )
    private RenderLayer makeSpiritualHandTransparent2(Identifier identifier, Operation<RenderLayer> original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, ModelPart modelPart, ModelPart modelPart2) {
        if (SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(abstractClientPlayerEntity)) {
            return RenderLayer.getItemEntityTranslucentCull(identifier);
        } else {
            return original.call(identifier);
        }
    }
}
