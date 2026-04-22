package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import com.example.levmod.registry.ModBlocks;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LevititeOreSpawnerBlockEntity extends BlockEntity {

    private boolean hasTriggered = false;
    private int tickDelay = 1;

    public LevititeOreSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITITE_ORE_SPAWNER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            LevititeOreSpawnerBlockEntity be) {
        if (level.isClientSide || be.hasTriggered) return;
        if (be.tickDelay-- > 0) return;
        be.hasTriggered = true;

        ServerLevel serverLevel = (ServerLevel) level;

        BlockPos clusterAnchor = placeOreBlob(serverLevel, pos);

        if (clusterAnchor == null) {
            level.removeBlock(pos, false);
            return;
        }

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
    }

    /**
     * Places an ore-blob shaped cluster of aeronautics:levitite centred on
     * {@code center}, replicating OreFeature's overlapping-ellipsoid algorithm.
     * Unlike vanilla ore placement, we place into air rather than replacing
     * stone, so the blob floats freely once assembled.
     *
     * @return any placed BlockPos to use as the Sable gather origin,
     *         or {@code null} if nothing was placed.
     */
    @Nullable
    private static BlockPos placeOreBlob(ServerLevel level, BlockPos center) {
        BlockState levitite = BuiltInRegistries.BLOCK
                .get(ResourceLocation.fromNamespaceAndPath("aeronautics", "levitite"))
                .defaultBlockState();

        // Vein size: 6–9 blocks, matching a small-to-medium ore vein
        int size = 6 + level.random.nextInt(4);

        // Random angle determines the orientation of the vein axis
        float angle = level.random.nextFloat() * (float) Math.PI;
        double axisX = Math.sin(angle);
        double axisZ = Math.cos(angle);

        // The vein runs between two endpoints offset from the centre
        double x1 = center.getX() + 0.5 + axisX * size / 8.0;
        double x2 = center.getX() + 0.5 - axisX * size / 8.0;
        double z1 = center.getZ() + 0.5 + axisZ * size / 8.0;
        double z2 = center.getZ() + 0.5 - axisZ * size / 8.0;
        double y1 = center.getY() + level.random.nextInt(3) - 1;
        double y2 = center.getY() + level.random.nextInt(3) - 1;

        BlockPos firstPlaced = null;

        for (int i = 0; i < size; i++) {
            float t = (float) i / size;

            // Interpolate along the vein axis
            double cx = x1 + (x2 - x1) * t;
            double cy = y1 + (y2 - y1) * t;
            double cz = z1 + (z2 - z1) * t;

            // Ellipsoid radius grows and shrinks along the vein (sine envelope),
            // producing the rounded ends characteristic of ore blobs
            double r = (level.random.nextDouble() * size / 16.0)
                    * ((Math.sin(Math.PI * t) + 1.0) * 0.5 + 0.5);
            if (r < 0.001) continue;

            int minX = (int) Math.floor(cx - r);
            int maxX = (int) Math.ceil(cx + r);
            int minY = (int) Math.floor(cy - r);
            int maxY = (int) Math.ceil(cy + r);
            int minZ = (int) Math.floor(cz - r);
            int maxZ = (int) Math.ceil(cz + r);

            for (int bx = minX; bx <= maxX; bx++) {
                double nx = (bx + 0.5 - cx) / r;
                if (nx * nx >= 1.0) continue;

                for (int by = minY; by <= maxY; by++) {
                    double ny = (by + 0.5 - cy) / r;
                    if (nx * nx + ny * ny >= 1.0) continue;

                    for (int bz = minZ; bz <= maxZ; bz++) {
                        double nz = (bz + 0.5 - cz) / r;
                        if (nx * nx + ny * ny + nz * nz >= 1.0) continue;

                        BlockPos candidate = new BlockPos(bx, by, bz).immutable();
                        level.setBlock(candidate, levitite, Block.UPDATE_ALL);
                        if (firstPlaced == null) firstPlaced = candidate;
                    }
                }
            }
        }

        return firstPlaced;
    }
}