package com.example.levmod.worldgen.feature;

import com.example.levmod.registry.ModBlocks;
import com.example.levmod.worldgen.feature.config.LevititeOreClusterConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LevititeOreClusterFeature extends Feature<LevititeOreClusterConfig> {

    public LevititeOreClusterFeature(Codec<LevititeOreClusterConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LevititeOreClusterConfig> ctx) {
        BlockPos pos = ctx.origin();
        WorldGenLevel level = ctx.level();
        LevititeOreClusterConfig config = ctx.config();
        RandomSource random = ctx.random();

        int size = config.minSize() + random.nextInt(
                Math.max(1, config.maxSize() - config.minSize() + 1));

        float angle = random.nextFloat() * (float) Math.PI;
        double axisX = Math.sin(angle);
        double axisZ = Math.cos(angle);

        double x1 = pos.getX() + 0.5 + axisX * size / 8.0;
        double x2 = pos.getX() + 0.5 - axisX * size / 8.0;
        double z1 = pos.getZ() + 0.5 + axisZ * size / 8.0;
        double z2 = pos.getZ() + 0.5 - axisZ * size / 8.0;
        double y1 = pos.getY() + random.nextInt(3) - 1;
        double y2 = pos.getY() + random.nextInt(3) - 1;

        boolean placed = false;

        for (int i = 0; i < size; i++) {
            float t = (float) i / size;

            double cx = x1 + (x2 - x1) * t;
            double cy = y1 + (y2 - y1) * t;
            double cz = z1 + (z2 - z1) * t;

            double r = Math.max(config.maxRadius(),
                    (random.nextDouble() * size / 16.0)
                            * ((Math.sin(Math.PI * t) + 1.0) * 0.5 + 0.5));
            if (r < 0.1) continue;

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

                        // Guard against writing outside the generation region
                        if (!level.ensureCanWrite(candidate)) continue;

                        level.setBlock(candidate, config.stateProvider()
                                .getState(random, candidate), Block.UPDATE_ALL);
                        placed = true;
                    }
                }
            }
        }

        if (placed) {
            if (level.ensureCanWrite(pos)) {
                level.setBlock(pos, ModBlocks.LEVITITE_SPAWNER.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }

        return placed;
    }
}