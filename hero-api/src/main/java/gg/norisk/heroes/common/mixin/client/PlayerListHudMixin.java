package gg.norisk.heroes.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gg.norisk.heroes.client.renderer.SkinUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;")
    )
    private Identifier heroapi$redirectCombinedSkin(SkinTextures instance, Operation<Identifier> original, @Local PlayerListEntry playerListEntry) {
        var player = client.world == null ? null : client.world.getPlayerByUuid(playerListEntry.getProfile().getId());
        return SkinUtils.INSTANCE.redirectCombinedSkin(original.call(instance), ((AbstractClientPlayerEntity) player));
    }
}
