package gg.norisk.heroes.aang.mixin.client;

import gg.norisk.heroes.aang.entity.DummyPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler implements ClientPlayPacketListener, TickablePacketListener {
    @Shadow private ClientWorld world;

    protected ClientPlayNetworkHandlerMixin(MinecraftClient minecraftClient, ClientConnection clientConnection, ClientConnectionState clientConnectionState) {
        super(minecraftClient, clientConnection, clientConnectionState);
    }

    @Inject(method = "createEntity", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false), cancellable = true)
    private void createFakePlayerInjection(EntitySpawnS2CPacket entitySpawnS2CPacket, CallbackInfoReturnable<Entity> cir) {
        DummyPlayer.Companion.handleDummyPlayerSpawn(entitySpawnS2CPacket, cir, this.world);
    }
}
