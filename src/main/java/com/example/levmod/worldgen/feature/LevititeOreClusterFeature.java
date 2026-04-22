package com.example.levmod.worldgen.feature;

import com.example.levmod.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LevititeOreClusterFeature extends Feature<NoneFeatureConfiguration> {

    public LevititeOreClusterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos pos  = ctx.origin();
        WorldGenLevel level = ctx.level();

        // Place the invisible spawner marker. The block entity fires on the
        // first server tick after chunk load, places the levitite sphere, and
        // assembles it into a Sable sub-level.
        level.setBlock(pos, ModBlocks.LEVITITE_ORE_SPAWNER.get().defaultBlockState(),
                Block.UPDATE_ALL);
        return true;
    }
}
