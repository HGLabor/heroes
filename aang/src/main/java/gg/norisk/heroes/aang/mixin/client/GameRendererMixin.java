package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import gg.norisk.heroes.aang.ability.AirScooterAbility;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"), cancellable = true)
    private void renderHandBeforeSpiritual(Camera camera, float f, Matrix4f matrix4f, CallbackInfo ci, @Local MatrixStack matrixStack) {
        matrixStack.push();
        if (camera.getFocusedEntity() instanceof PlayerEntity player && SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(player)) {
            RenderSystem.setShaderColor(1f, 1f, 1f, SpiritualProjectionAbility.INSTANCE.getAlpha(player));
        }
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", shift = At.Shift.AFTER))
    private void renderHandAfterSpiritual(Camera camera, float f, Matrix4f matrix4f, CallbackInfo ci, @Local MatrixStack matrixStack) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrixStack.pop();
    }

    @ModifyExpressionValue(
            method = {"renderWorld", "renderHand"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0)
    )
    private <T> Object aang$disableViewBobbing(T original) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && AirScooterAbility.INSTANCE.isAirScooting(player)) {
            return false;
        }
        return original;
    }
}
