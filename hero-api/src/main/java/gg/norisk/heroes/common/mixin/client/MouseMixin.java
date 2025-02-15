package gg.norisk.heroes.common.mixin.client;

import gg.norisk.heroes.client.events.ClientEvents;
import gg.norisk.heroes.common.events.BasicEventsKt;
import gg.norisk.heroes.common.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;", shift = At.Shift.BEFORE))
    private void hookMouseScroll(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
        BasicEventsKt.getMouseScrollEvent().invoke(new MouseScrollEvent(window, horizontal, vertical));
    }

    @Inject(
            method = "onMouseScroll(JDD)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Mouse;eventDeltaVerticalWheel:D",
                    ordinal = 6
            ),
            cancellable = true
    )
    private void updateZoom(CallbackInfo info) {
        var event = new ClientEvents.PreHotbarScrollEvent();
        ClientEvents.INSTANCE.getPreHotbarScrollEvent().invoke(event);
        if (event.isCancelled().get()) {
            info.cancel();
        }
    }
}
