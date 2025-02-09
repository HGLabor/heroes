package gg.norisk.heroes.toph.mixin.entity;

import gg.norisk.heroes.toph.entity.ITophPlayer;
import io.netty.util.internal.ConcurrentSet;
import kotlin.Pair;
import kotlinx.coroutines.Job;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ITophPlayer {
    private final Set<Pair<Long, BlockPos>> seismicBlocks = ConcurrentHashMap.newKeySet();
    private final Set<Pair<Long, UUID>> seismicEntities = ConcurrentHashMap.newKeySet();

    @Unique
    private final List<Job> seismicTasks = new ArrayList<>();

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

    @Override
    public @NotNull List<Job> getToph_seismicTasks() {
        return seismicTasks;
    }
}
