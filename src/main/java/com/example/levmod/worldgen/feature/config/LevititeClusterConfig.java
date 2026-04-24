package com.example.levmod.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record LevititeClusterConfig(
        int minRadius,
        int maxRadius,
        BlockStateProvider stateProvider
) implements FeatureConfiguration {

    public static final Codec<LevititeClusterConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(1, 32).fieldOf("min_radius").forGetter(LevititeClusterConfig::minRadius),
                    Codec.intRange(1, 32).fieldOf("max_radius").forGetter(LevititeClusterConfig::maxRadius),
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(LevititeClusterConfig::stateProvider)
            ).apply(instance, LevititeClusterConfig::new)
    );
}