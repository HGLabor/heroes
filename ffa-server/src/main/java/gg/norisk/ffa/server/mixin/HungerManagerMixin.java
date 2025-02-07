package gg.norisk.ffa.server.mixin;

import gg.norisk.ffa.server.mechanics.KitEditor;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @ModifyConstant(method = "update", constant = @Constant(floatValue = 1f, ordinal = 0))
    private float injected(float constant) {
        if (KitEditor.INSTANCE.isUHC()) {
            return constant;
        }
        return 0.25f;
    }
}
