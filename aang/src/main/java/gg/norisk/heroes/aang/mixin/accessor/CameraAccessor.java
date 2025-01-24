package gg.norisk.heroes.aang.mixin.accessor;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setRotation")
    void invokeSetRotation(float f, float g);

    @Invoker("setPos")
    void invokeSetPos(double d, double e, double f);
}

