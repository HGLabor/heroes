package gg.norisk.heroes.aang.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.aang.ability.AirScooterAbility;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import gg.norisk.heroes.aang.client.render.entity.feature.AirScooterFeatureRenderer;
import gg.norisk.heroes.aang.entity.TornadoEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    protected LivingEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void airballRenderer(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
    }

    @Shadow
    protected abstract boolean addFeature(FeatureRenderer<T, M> featureRenderer);

    @Shadow
    protected M model;

    @WrapOperation(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getScale()F")
    )
    private float redirectGetScaleWithLerpedScale(LivingEntity instance, Operation<Float> original, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (instance instanceof TornadoEntity tornado) {
            return tornado.getLerpedScale(g);
        } else {
            return original.call(instance);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initDataTrackerInjecetion(EntityRendererFactory.Context context, EntityModel<T> entityModel, float f, CallbackInfo ci) {
        this.addFeature(new AirScooterFeatureRenderer<>(this));
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/EntityModel;riding:Z", shift = At.Shift.AFTER))
    private void ridingInjection(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity player && AirScooterAbility.INSTANCE.isAirScooting(player)) {
            this.model.riding = true;
        }
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("STORE"), ordinal = 0)
    private boolean modifyIsVisibleForAang(boolean original, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (livingEntity instanceof PlayerEntity player) {
            return original && !SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(player);
        }
        return original;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("STORE"), ordinal = 1)
    private boolean modifyIsInVisibleToForAang(boolean original, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (livingEntity instanceof PlayerEntity player) {
            return original && SpiritualProjectionAbility.INSTANCE.isSpiritualTransparent(player);
        }
        return original;
    }
}
