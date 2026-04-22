package com.example.levmod;

import com.example.levmod.registry.ModBlockEntities;
import com.example.levmod.registry.ModBlocks;
import com.example.levmod.registry.ModFeatures;
import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.surface.SurfaceGeneration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(LevMod.MOD_ID)
public class LevMod {

    public static final String MOD_ID = "levmod";

    public static final ResourceKey<Biome> LEVITITE_FIELDS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "levitite_fields")
    );

    public LevMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        // Biome placement — no TerraBlender region class needed
        BiomePlacement.addOverworld(LEVITITE_FIELDS,
                new Climate.ParameterPoint(
                        Climate.Parameter.span(0.1f, 0.3f),   // temperature
                        Climate.Parameter.span(-0.3f, 0.0f),  // humidity
                        Climate.Parameter.span(0.3f, 1.0f),   // continentalness
                        Climate.Parameter.span(-0.2f, 0.2f),  // erosion
                        Climate.Parameter.point(0.0f),         // depth (surface)
                        Climate.Parameter.span(0.4f, 1.0f),   // weirdness
                        0L                                     // offset
                ));


        SurfaceRules.RuleSource stoneRule = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(LEVITITE_FIELDS),
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                                ),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.UNDER_FLOOR,
                                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                                )
                        )
                )
        );

        SurfaceGeneration.addOverworldSurfaceRules(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "surface_rules"),
                stoneRule
        );
    }
}
