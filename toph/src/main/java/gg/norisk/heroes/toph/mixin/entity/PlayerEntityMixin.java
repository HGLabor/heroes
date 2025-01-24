package gg.norisk.heroes.toph.mixin.entity;

import gg.norisk.heroes.toph.entity.ITophPlayer;
import kotlin.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ITophPlayer {
    private final Set<Pair<Long, BlockPos>> seismicBlocks = new HashSet<>();
    private final Set<Pair<Long, UUID>> seismicEntities = new HashSet<>();


    @NotNull
    @Override
    public Set<Pair<Long, BlockPos>> getSeismicBlocks() {
        return seismicBlocks;
    }

    @NotNull
    @Override
    public Set<Pair<Long, UUID>> getSeismicEntities() {
        return seismicEntities;
    }
}
