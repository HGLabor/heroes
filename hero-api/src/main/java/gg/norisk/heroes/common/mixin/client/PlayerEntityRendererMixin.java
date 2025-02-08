package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import gg.norisk.heroes.client.renderer.SkinUtils;
import gg.norisk.heroes.common.db.DatabaseManager;
import gg.norisk.heroes.common.hero.IHeroManagerKt;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

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

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At(value = "HEAD"))
    private void heroapi$renderBounty(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f, CallbackInfo ci) {
        renderBounty(this.dispatcher.getSquaredDistanceToCamera(abstractClientPlayerEntity), abstractClientPlayerEntity, matrixStack, vertexConsumerProvider, i, f);
    }

    @Unique
    private void renderBounty(double d, AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
        if (d < (double) 2000.0F) {
            int bounty = DatabaseManager.INSTANCE.getFfaBounty(abstractClientPlayerEntity);
            if (bounty > 0) {
                Vec3d vec3d = abstractClientPlayerEntity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, abstractClientPlayerEntity.getYaw(f));
                if (vec3d != null) {
                    matrixStack.push();
                    matrixStack.scale(0.5f, 0.5f, 0.5f);
                    matrixStack.translate(vec3d.x, vec3d.y + 0.125f, vec3d.z);
                    super.renderLabelIfPresent(abstractClientPlayerEntity, Text.empty().append("Bounty: ").append(String.valueOf(bounty)), matrixStack, vertexConsumerProvider, i, f);
                    matrixStack.pop();
                    matrixStack.translate(0.0F, 0.075F, 0.0F);
                }
            }
        }
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;")
    )
    private Identifier heroapi$redirectRenderArmSkinTexture(SkinTextures instance, Operation<Identifier> original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, ModelPart modelPart, ModelPart modelPart2) {
        return SkinUtils.INSTANCE.redirectCombinedSkin(original.call(instance), abstractClientPlayerEntity);
    }
}
