package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import gg.norisk.heroes.aang.entity.TornadoEntity;
import gg.norisk.utils.ext.EntityRenderStateExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements FeatureRendererContext<S, M> {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Shadow
    protected M model;

    @WrapOperation(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;baseScale:F")
    )
    private float redirectGetScaleWithLerpedScale(LivingEntityRenderState instance, Operation<Float> original) {
        if (((EntityRenderStateExt) instance).getNrc_entity() instanceof TornadoEntity tornado) {
            return tornado.getLerpedScale(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        } else {
            return original.call(instance);
        }
    }

    /*TODO @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/EntityModel;riding:Z", shift = At.Shift.AFTER))
    private void ridingInjection(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player && AirScooterAbility.INSTANCE.isAirScooting(player)) {
            this.model.riding = true;
        }
    }*/

    @ModifyVariable(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("STORE"), ordinal = 0)
    private boolean modifyIsVisibleForAang(boolean original, S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (((EntityRenderStateExt) livingEntityRenderState).getNrc_entity() instanceof PlayerEntity player) {
            return original && !SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(player);
        }
        return original;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("STORE"), ordinal = 1)
    private boolean modifyIsInVisibleToForAang(boolean original, S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (((EntityRenderStateExt) livingEntityRenderState).getNrc_entity() instanceof PlayerEntity player) {
            return original && SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(player);
        }
        return original;
    }
}
