package com.example.levmod.registry;

import com.example.levmod.LevMod;
import com.example.levmod.worldgen.feature.LevititeClusterFeature;
import com.example.levmod.worldgen.feature.LevititeOreClusterFeature;
import com.example.levmod.worldgen.feature.config.LevititeClusterConfig;
import com.example.levmod.worldgen.feature.config.LevititeOreClusterConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, LevMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, LevititeClusterFeature> LEVITITE_CLUSTER =
            FEATURES.register("levitite_cluster",
                    () -> new LevititeClusterFeature(LevititeClusterConfig.CODEC));

    public static final DeferredHolder<Feature<?>, LevititeOreClusterFeature> LEVITITE_ORE_CLUSTER =
            FEATURES.register("levitite_ore_cluster",
                    () -> new LevititeOreClusterFeature(LevititeOreClusterConfig.CODEC));
}
