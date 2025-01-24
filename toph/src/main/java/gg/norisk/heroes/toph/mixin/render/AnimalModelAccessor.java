package gg.norisk.heroes.toph.mixin.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AnimalModel.class)
public interface AnimalModelAccessor {
    @Invoker("getBodyParts")
    Iterable<ModelPart> invokeGetBodyParts();
}
