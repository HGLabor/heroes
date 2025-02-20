package gg.norisk.heroes.katara.mixin;

import gg.norisk.heroes.katara.event.FluidEvents;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void onScheduledTickInjection(ServerWorld world, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        var event = new FluidEvents.FluidEvent(world, pos, fluidState);
        FluidEvents.INSTANCE.getFluidTickEvent().invoke(event);
        if (event.isCancelled().get()) {
            ci.cancel();
        }
    }
}
