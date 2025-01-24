package gg.norisk.heroes.katara.mixin;

import gg.norisk.heroes.katara.entity.IWaterBendingPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IWaterBendingPlayer {
    @Unique
    private final Set<BlockPos> waterPillarBlocks = new HashSet<>();
    @Unique
    private BlockPos waterPillarPos;

    @Override
    public @NotNull Set<BlockPos> getKatara_waterPillarBlocks() {
        return waterPillarBlocks;
    }

    @Override
    public @Nullable BlockPos getKatara_waterPillarOrigin() {
        return waterPillarPos;
    }

    @Override
    public void setKatara_waterPillarOrigin(@Nullable BlockPos blockPos) {
        waterPillarPos = blockPos;
    }
}
