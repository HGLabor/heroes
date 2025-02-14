package gg.norisk.ffa.server.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {
    @ModifyReturnValue(
            method = "isWorldAllowed",
            at = @At("RETURN")
    )
    private boolean disableNether(boolean original, World world) {
        if (world.getRegistryKey() == World.NETHER) {
            return false;
        }
        return original;
    }
}
