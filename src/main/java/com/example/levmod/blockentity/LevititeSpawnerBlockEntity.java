package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import com.example.levmod.registry.ModBlocks;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class LevititeSpawnerBlockEntity extends BlockEntity {

    private boolean hasTriggered = false;
    // Wait one tick after chunk load before assembling, giving Sable time to
    // fully initialise its physics world for this region.
    private int tickDelay = 1;

    public LevititeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITITE_SPAWNER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            LevititeSpawnerBlockEntity be) {
        if (level.isClientSide || be.hasTriggered) return;
        if (be.tickDelay-- > 0) return;
        be.hasTriggered = true;

        ServerLevel serverLevel = (ServerLevel) level;

        // Place a spherical cluster of levitite centred on this block.
        // This call overwrites the spawner block itself, so no separate
        // removeBlock() is needed afterwards.
        BlockPos clusterAnchor = placeCluster(serverLevel, pos);

        if (clusterAnchor == null) {
            // Safety guard: nothing was placed, clean up the spawner.
            level.removeBlock(pos, false);
            return;
        }

        // Gather all connected solid blocks starting from the cluster anchor.
        SubLevelAssemblyHelper.GatherResult result = SubLevelAssemblyHelper.gatherConnectedBlocks(
                clusterAnchor,
                serverLevel,
                10_000,
                null
        );

        if (result.assemblyState() == SubLevelAssemblyHelper.GatherResult.State.SUCCESS) {
            SubLevelAssemblyHelper.assembleBlocks(
                    serverLevel,
                    clusterAnchor,
                    result.blocks(),
                    result.boundingBox()
            );
        }
        // Note: the spawner block was already overwritten by placeCluster(),
        // so there is nothing left to remove here.
    }

    /**
     * Places a spherical blob of levitite centred on {@code center}.
     * The centre position itself is included, which overwrites the spawner block.
     *
     * @return the first block position placed, used as the Sable gather origin,
     *         or {@code null} if nothing was placed (should never happen).
     */
    @Nullable
    private static BlockPos placeCluster(ServerLevel level, BlockPos center) {
        int radius = 0 + level.random.nextInt(4);
        int radiusSq = radius * radius;

        // Look up aeronautics:levitite at runtime
        BlockState levitite = BuiltInRegistries.BLOCK
                .get(ResourceLocation.fromNamespaceAndPath("aeronautics", "levitite"))
                .defaultBlockState();

        BlockPos firstPlaced = null;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) continue;

                    BlockPos candidate = center.offset(dx, dy, dz).immutable();
                    level.setBlock(candidate, levitite, Block.UPDATE_IMMEDIATE);

                    if (firstPlaced == null) firstPlaced = candidate;
                }
            }
        }

        return firstPlaced;
    }
}
