package gg.norisk.heroes.katara.mixin;

import gg.norisk.heroes.katara.event.FluidEvents;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void onScheduledTickInjection(World world, BlockPos blockPos, FluidState fluidState, CallbackInfo ci) {
        var event = new FluidEvents.FluidEvent(world, blockPos, fluidState);
        FluidEvents.INSTANCE.getFluidTickEvent().invoke(event);
        if (event.isCancelled().get()) {
            ci.cancel();
        }
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void appendStillProperties(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci) {
        //builder.add(FluidEvents.INSTANCE.getStatic());
    }
}
