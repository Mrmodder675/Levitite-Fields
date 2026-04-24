package com.example.levmod.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record LevititeOreClusterConfig(
        int minSize,
        int maxSize,
        float maxRadius,
        BlockStateProvider stateProvider
) implements FeatureConfiguration {

    public static final Codec<LevititeOreClusterConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.intRange(1, 64).fieldOf("min_size").forGetter(LevititeOreClusterConfig::minSize),
                    Codec.intRange(1, 64).fieldOf("max_size").forGetter(LevititeOreClusterConfig::maxSize),
                    Codec.floatRange(0.1f, 8.0f).fieldOf("max_radius").forGetter(LevititeOreClusterConfig::maxRadius),
                    BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(LevititeOreClusterConfig::stateProvider)
            ).apply(instance, LevititeOreClusterConfig::new)
    );
}