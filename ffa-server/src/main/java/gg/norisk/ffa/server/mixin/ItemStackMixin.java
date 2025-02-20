package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.KitEditor;
import gg.norisk.ffa.server.mechanics.SoupHealing;
import gg.norisk.ffa.server.mechanics.Tracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (!KitEditor.INSTANCE.isUHC()) {
            SoupHealing.INSTANCE.onPotentialSoupUse(user, itemStack.getItem(), cir, world, hand);
        }
        Tracker.INSTANCE.onTrackerUse(user, itemStack, world, hand);
    }
}
