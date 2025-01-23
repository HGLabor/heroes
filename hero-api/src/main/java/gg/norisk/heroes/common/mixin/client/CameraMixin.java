package gg.norisk.heroes.common.mixin.client;

import gg.norisk.heroes.client.events.ClientEvents;
import gg.norisk.heroes.client.renderer.CameraShaker;
import gg.norisk.heroes.client.ui.OrthoCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyConstant(method = "update", constant = @Constant(floatValue = 4.0f))
    private float updateInjection(float constant) {
        var event = new ClientEvents.CameraClipToSpaceEvent(constant);
        ClientEvents.INSTANCE.getCameraClipToSpaceEvent().invoke(event);
        return (float) event.getValue();
    }

    @Inject(
            method = "update",
            at = @At(
                    // Inject before the call to clipToSpace
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
                    shift = At.Shift.BY,
                    by = 1
            )
    )
    void camerashake$onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        double x = CameraShaker.INSTANCE.getAvgX();
        double y = CameraShaker.INSTANCE.getAvgY();
        ((Camera) (Object)this).moveBy((float) .0, (float) y, (float) x);
    }

    @ModifyVariable(method = "moveBy", at = @At("HEAD"), index = 1, argsOnly = true)
    private float heroapi$moveByHeadX(float value) {
        return OrthoCamera.INSTANCE.isEnabled() ? 0.0f : value;
    }

    @ModifyVariable(method = "moveBy", at = @At("HEAD"), index = 3, argsOnly = true)
    private float heroapi$moveByHeadZ(float value) {
        return OrthoCamera.INSTANCE.isEnabled() ? 0.0f : value;
    }
}
