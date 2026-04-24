package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LevititeSpawnerBlockEntity extends BlockEntity {

    private boolean hasTriggered = false;
    private int tickDelay = 10;

    public LevititeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITITE_SPAWNER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            LevititeSpawnerBlockEntity be) {
        if (level.isClientSide || be.hasTriggered) return;
        if (be.tickDelay-- > 0) return;
        be.hasTriggered = true;

        ServerLevel serverLevel = (ServerLevel) level;

        // Find any neighbouring solid block as the gather anchor.
        // The spawner is air-like so Sable won't include it — we need
        // a real levitite block to seed gatherConnectedBlocks from.
        BlockPos anchor = findNeighbourAnchor(serverLevel, pos);

        if (anchor == null) {
            // Nothing adjacent — cluster may have failed to place. Clean up.
            level.removeBlock(pos, false);
            return;
        }

        SubLevelAssemblyHelper.GatherResult result = SubLevelAssemblyHelper.gatherConnectedBlocks(
                anchor,
                serverLevel,
                10_000,
                null
        );

        if (result.assemblyState() == SubLevelAssemblyHelper.GatherResult.State.SUCCESS) {
            SubLevelAssemblyHelper.assembleBlocks(
                    serverLevel,
                    anchor,
                    result.blocks(),
                    result.boundingBox()
            );
        }

        // Remove the spawner itself — it was never part of the cluster.
        level.removeBlock(pos, false);
    }

    /**
     * Scans all 26 neighbours (3x3x3 minus self) for the first solid,
     * non-spawner block to use as the Sable gather origin.
     */
    @Nullable
    private static BlockPos findNeighbourAnchor(ServerLevel level, BlockPos spawnerPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos candidate = spawnerPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    if (!state.isAir() && !state.is(ModBlockEntities.LEVITITE_SPAWNER.get().getValidBlocks().iterator().next())) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}