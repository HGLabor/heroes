package gg.norisk.heroes.katara.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.heroes.katara.ability.WaterBendingAbility;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @ModifyReturnValue(
            method = "getArmPose",
            at = @At("RETURN")
    )
    private static BipedEntityModel.ArmPose getArmPoseInjection(BipedEntityModel.ArmPose original, AbstractClientPlayerEntity abstractClientPlayerEntity, Hand hand) {
        BipedEntityModel.ArmPose waterBendingPose = WaterBendingAbility.INSTANCE.getWaterBendingPose(abstractClientPlayerEntity, hand);
        if (waterBendingPose != null) {
            return waterBendingPose;
        }
        return original;
    }
}
