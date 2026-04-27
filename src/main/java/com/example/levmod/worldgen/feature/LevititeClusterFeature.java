package com.example.levmod.worldgen.feature;

import com.example.levmod.registry.ModBlocks;
import com.example.levmod.worldgen.feature.config.LevititeClusterConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LevititeClusterFeature extends Feature<LevititeClusterConfig> {

    public LevititeClusterFeature(Codec<LevititeClusterConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LevititeClusterConfig> ctx) {
        BlockPos center = ctx.origin();
        WorldGenLevel level = ctx.level();
        LevititeClusterConfig config = ctx.config();
        RandomSource random = ctx.random();

        int radius = config.minRadius() + random.nextInt(
                Math.max(1, config.maxRadius() - config.minRadius() + 1));
        int radiusSq = radius * radius;

        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) continue;
                    // Skip origin — spawner goes there
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos candidate = center.offset(dx, dy, dz).immutable();

                    if (!level.ensureCanWrite(candidate)) continue;

                    level.setBlock(candidate, config.stateProvider()
                            .getState(random, candidate), Block.UPDATE_ALL);
                    placed = true;
                }
            }
        }

        if (placed) {
            if (level.ensureCanWrite(center)) {
                level.setBlock(center, ModBlocks.LEVITITE_SPAWNER.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }

        return placed;
    }
}