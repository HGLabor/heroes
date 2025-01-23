package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.client.renderer.SkinUtils;
import gg.norisk.heroes.common.hero.IHeroManagerKt;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getSkinTextureInjection(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        var hero = IHeroManagerKt.getHero(player);
        if (hero == null) return;
        var skin = hero.getInternalCallbacks().getGetSkin();
        if (skin != null) {
            cir.setReturnValue(skin.invoke(player));
        }
    }

    @ModifyReturnValue(
            method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;",
            at = @At("RETURN")
    )
    private Identifier redirectHeroSkin(Identifier original, AbstractClientPlayerEntity abstractClientPlayerEntity) {
        return SkinUtils.INSTANCE.redirectCombinedSkin(original, abstractClientPlayerEntity);
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;")
    )
    private Identifier heroapi$redirectRenderArmSkinTexture(SkinTextures instance, Operation<Identifier> original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, ModelPart modelPart, ModelPart modelPart2) {
        return SkinUtils.INSTANCE.redirectCombinedSkin(original.call(instance), abstractClientPlayerEntity);
    }
}
