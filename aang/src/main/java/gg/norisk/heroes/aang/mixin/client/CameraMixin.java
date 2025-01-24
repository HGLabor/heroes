package gg.norisk.heroes.aang.mixin.client;

import gg.norisk.heroes.aang.ability.TornadoAbility;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    private void tornadoCameraMode(BlockView blockView, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
        TornadoAbility.INSTANCE.handleTornadoCamera((Camera) (Object) this, blockView, entity, bl, bl2, f);
    }
}
