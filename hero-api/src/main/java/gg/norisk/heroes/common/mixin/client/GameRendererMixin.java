package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import gg.norisk.heroes.client.renderer.CameraShaker;
import gg.norisk.heroes.client.ui.OrthoCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void heroapi$onRender(RenderTickCounter renderTickCounter, boolean tick, CallbackInfo ci) {
        if (!client.skipGameRender && tick && client.world != null) {
            CameraShaker.INSTANCE.newFrame();
        }
    }

    @WrapWithCondition(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
    )
    private boolean heroapi$dontRenderHud(InGameHud instance, DrawContext drawContext, RenderTickCounter renderTickCounter) {
        return !OrthoCamera.INSTANCE.isEnabled();
    }

    @WrapWithCondition(
            method = "renderWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V")
    )
    private boolean heroapi$dontRenderHand(GameRenderer instance, Camera camera, float f, Matrix4f matrix4f) {
        return !OrthoCamera.INSTANCE.isEnabled();
    }


    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"
            )
    )
    private void heroapi$shakeHand(Camera camera, float f, Matrix4f matrix4f, CallbackInfo ci) {
        float x = (float) CameraShaker.INSTANCE.getAvgX();
        float y = (float) CameraShaker.INSTANCE.getAvgY();

        matrix4f.translate(x, -y, (float) .0); // opposite of camera
    }

    // TODO keine ahnung es will nxi so wie ich will T_T
    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lnet/minecraft/util/math/Vec3d;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
            ),
            index = 2
    )
    private Matrix4f heroapi$orthoFrustumProjMat(Matrix4f projMat) {
        if (OrthoCamera.INSTANCE.isEnabled()) {
            return OrthoCamera.INSTANCE.createOrthoMatrix(1.0F, 20.0F);
        }
        return projMat;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"

            ),
            index = 6
    )
    private Matrix4f heroapi$orthoProjMat(Matrix4f projMat, @Local(argsOnly = true) RenderTickCounter tickCounter) {
        if (OrthoCamera.INSTANCE.isEnabled()) {
            float tickDelta = tickCounter.getTickDelta(true);
            Matrix4f mat = OrthoCamera.INSTANCE.createOrthoMatrix(tickDelta, 0.0F);
            RenderSystem.setProjectionMatrix(mat, ProjectionType.ORTHOGRAPHIC);
            return mat;
        }
        return projMat;
    }

    @ModifyExpressionValue(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;",
                    remap = false
            )
    )
    private Quaternionf heroapi$modifyRotation(Quaternionf original, @Local(argsOnly = true) RenderTickCounter tickCounter) {
        if (!OrthoCamera.INSTANCE.isEnabled()) {
            return original;
        }
        return original.rotationXYZ(
                OrthoCamera.INSTANCE.handlePitch(original, tickCounter.getTickDelta(false)),
                OrthoCamera.INSTANCE.handleYaw(original, tickCounter.getTickDelta(false)),
                0.0F
        );
    }
}
