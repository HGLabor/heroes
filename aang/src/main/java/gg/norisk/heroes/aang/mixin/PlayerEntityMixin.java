package gg.norisk.heroes.aang.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.heroes.aang.ability.AirBallAbility;
import gg.norisk.heroes.aang.ability.AirScooterAbility;
import gg.norisk.heroes.aang.ability.LevitationAbility;
import gg.norisk.heroes.aang.ability.SpiritualProjectionAbility;
import gg.norisk.heroes.aang.entity.IAangPlayer;
import gg.norisk.heroes.aang.utils.CircleDetector3D;
import gg.norisk.heroes.aang.utils.PlayerRotationTracker;
import kotlinx.coroutines.Job;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IAangPlayer {
    @Shadow
    public abstract void remove(RemovalReason removalReason);

    @Unique
    private CircleDetector3D circleDetector;

    @Unique
    private PlayerRotationTracker rotationTracker;
    @Unique
    private List<Job> airScooterTasks = new ArrayList<>();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void airScooterTravelInjection(Vec3d vec3d, CallbackInfo ci) {
        var player = (PlayerEntity) (Object) this;
        if (AirScooterAbility.INSTANCE.isAirScooting(player)) {
            super.travel(AirScooterAbility.INSTANCE.handleTravel(player, vec3d));
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", shift = At.Shift.AFTER))
    private void handleAangTickAfterNoClip(CallbackInfo ci) {
        SpiritualProjectionAbility.INSTANCE.handleTick((PlayerEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleAangTick(CallbackInfo ci) {
        AirBallAbility.INSTANCE.handleTick((PlayerEntity) (Object) this);
        LevitationAbility.INSTANCE.handleTick((PlayerEntity) (Object) this);
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void handleFallAangInjection(float f, float g, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        AirScooterAbility.INSTANCE.handleFallDamage((PlayerEntity) (Object) this, f, g, damageSource, cir);
    }

    @Nullable
    @Override
    public CircleDetector3D getCircleDetector() {
        return circleDetector;
    }

    @Override
    public void setCircleDetector(CircleDetector3D circleDetector) {
        this.circleDetector = circleDetector;
    }

    @Nullable
    @Override
    public PlayerRotationTracker getRotationTracker() {
        return rotationTracker;
    }

    @Override
    public void setRotationTracker(PlayerRotationTracker rotationTracker) {
        this.rotationTracker = rotationTracker;
    }

    // DUMMY PLAYER STUFF

    @ModifyArgs(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private void getFakeDisplayName(Args args) {
        SpiritualProjectionAbility.INSTANCE.replaceNameWithOwner((PlayerEntity) (Object) this, args);
    }

    @WrapOperation(
            method = "isPartVisible",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getDataTracker()Lnet/minecraft/entity/data/DataTracker;")
    )
    private DataTracker aang$redirectIsModelPartVisible(PlayerEntity instance, Operation<DataTracker> original) {
        return SpiritualProjectionAbility.INSTANCE.replaceDataTrackerWithOwner(instance, original);
    }

    @Override
    public @NotNull List<Job> getAang_airScooterTasks() {
        return airScooterTasks;
    }
}
