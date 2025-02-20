package gg.norisk.heroes.common.mixin.client;

import gg.norisk.heroes.client.events.ClientEvents;
import net.minecraft.client.input.Scroller;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scroller.class)
public abstract class ScrollerMixin {
    @Inject(
            method = "update",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/input/Scroller;cumulHorizontal:D",
                    ordinal = 6
            ),
            cancellable = true
    )
    private void updateZoom(double horizontal, double vertical, CallbackInfoReturnable<Vector2i> cir) {
        var event = new ClientEvents.PreHotbarScrollEvent();
        ClientEvents.INSTANCE.getPreHotbarScrollEvent().invoke(event);
        if (event.isCancelled().get()) {
            cir.setReturnValue(new Vector2i(0,0));
        }
    }
}
