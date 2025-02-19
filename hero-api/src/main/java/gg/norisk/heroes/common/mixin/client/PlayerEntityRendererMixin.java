package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.client.renderer.SkinUtils;
import gg.norisk.heroes.common.hero.IHeroManagerKt;
import gg.norisk.heroes.common.player.FFAPlayerKt;
import gg.norisk.utils.ext.EntityRenderStateExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {
    @Shadow protected abstract void setupTransforms(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, float f, float g);

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void getSkinTextureInjection(PlayerEntityRenderState playerEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (((EntityRenderStateExt) playerEntityRenderState).getNrc_entity() instanceof AbstractClientPlayerEntity player) {
            var hero = IHeroManagerKt.getHero(player);
            if (hero == null) return;
            var skin = hero.getInternalCallbacks().getGetSkin();
            if (skin != null) {
                cir.setReturnValue(skin.invoke(player));
            }
        }
    }

    @ModifyReturnValue(
            method = "getTexture(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/Identifier;",
            at = @At("RETURN")
    )
    private Identifier redirectHeroSkin(Identifier original, PlayerEntityRenderState playerEntityRenderState) {
        if (((EntityRenderStateExt) playerEntityRenderState).getNrc_entity() instanceof AbstractClientPlayerEntity player) {
            return SkinUtils.INSTANCE.redirectCombinedSkin(original, player);
        }
        return original;
    }

    @Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
    private void heroapi$renderBounty(PlayerEntityRenderState playerEntityRenderState, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (((EntityRenderStateExt) playerEntityRenderState).getNrc_entity() instanceof AbstractClientPlayerEntity player) {
            renderBounty(playerEntityRenderState, this.dispatcher.getSquaredDistanceToCamera(player), player, matrixStack, vertexConsumerProvider, i, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        }
    }

    @Unique
    private void renderBounty(PlayerEntityRenderState state, double d, AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
        if (d < (double) 2000.0F) {
            int bounty = FFAPlayerKt.getFfaBounty(abstractClientPlayerEntity);
            if (bounty > 0) {
                Vec3d vec3d = abstractClientPlayerEntity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, abstractClientPlayerEntity.getYaw(f));
                if (vec3d != null) {
                    matrixStack.push();
                    matrixStack.scale(0.5f, 0.5f, 0.5f);
                    matrixStack.translate(vec3d.x, vec3d.y + 0.125f, vec3d.z);
                    super.renderLabelIfPresent(state, Text.empty().append("Bounty: ").append(String.valueOf(bounty)), matrixStack, vertexConsumerProvider, i);
                    matrixStack.pop();
                    matrixStack.translate(0.0F, 0.075F, 0.0F);
                }
            }
        }
    }
}
