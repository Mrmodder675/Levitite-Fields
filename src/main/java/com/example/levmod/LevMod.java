package com.example.levmod;

//Levitite Fields feature size layer Y ranges
// Layer1 = 86 - 136
//
// Layer2 = 128 - 200
//
// Layer3 = 192 - 264
//
// Layer4 = 256 - 310


import com.example.levmod.registry.ModBlockEntities;
import com.example.levmod.registry.ModBlocks;
import com.example.levmod.registry.ModFeatures;
import com.mrmodder.yetanothermodlibrary.api.PackFinderHelper;
import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.sub.CriterionBuilder;
import com.terraformersmc.biolith.api.biome.sub.RatioTargets;
import com.terraformersmc.biolith.api.surface.SurfaceGeneration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(LevMod.MOD_ID)
@EventBusSubscriber
public class LevMod {

    public static final String MOD_ID = "levmod";

    public static final ResourceKey<Biome> PAINTED_LEVITITE_FIELDS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "painted_levitite_fields")
    );

    public static final ResourceKey<Biome> END_LEVITITE_FIELDS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "end_levitite_fields")
    );

    public static final ResourceKey<Biome> LEVITITE_FIELDS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "levitite_fields")
    );

    public LevMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(this::commonSetup);
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event)
    {
        PackFinderHelper.addPackFinderIfModLoaded(event, "aeronautics_dyeable_components", MOD_ID,
                "compat/dyeable_components",
                PackType.SERVER_DATA,
                "Levitite Fields dyeable components compat datapack",
                PackSource.WORLD,
                true,
                Pack.Position.TOP);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (Config.INSTANCE.generatePaintedLevititeFields.get() && ModList.get().isLoaded("aeronautics_dyeable_components"))
        {
            BiomePlacement.addSubOverworld(
                    Biomes.BADLANDS,
                    LevMod.PAINTED_LEVITITE_FIELDS,
                    CriterionBuilder.ratio(RatioTargets.CENTER, Config.INSTANCE.paintedLevititeFieldsLowerChance.get().floatValue(), Config.INSTANCE.paintedLevititeFieldsUpperChance.get().floatValue())
            );
        }

        if (Config.INSTANCE.generateEndLevititeFields.get()) {
            BiomePlacement.addSubEnd(
                    Biomes.SMALL_END_ISLANDS,
                    LevMod.END_LEVITITE_FIELDS,
                    CriterionBuilder.ratio(RatioTargets.CENTER, Config.INSTANCE.endLevititeFieldsLowerChance.get().floatValue(), Config.INSTANCE.endLevititeFieldsUpperChance.get().floatValue())
            );
            BiomePlacement.addSubEnd(
                    Biomes.THE_END,
                    LevMod.END_LEVITITE_FIELDS,
                    CriterionBuilder.ratio(RatioTargets.EDGE, Config.INSTANCE.endLevititeFieldsLowerChance.get().floatValue(), Config.INSTANCE.endLevititeFieldsUpperChance.get().floatValue())
            );
        }

        if (Config.INSTANCE.generateLevititeFields.get()) {
            BiomePlacement.addOverworld(LEVITITE_FIELDS,
                    new Climate.ParameterPoint(
                            Climate.Parameter.span(-0.15f, 0.25f),
                            Climate.Parameter.span(-0.2f, -0.05f),
                            Climate.Parameter.span(-0.5f, 1.0f),
                            Climate.Parameter.span(-0.4f, 0.4f),
                            Climate.Parameter.point(0.0f),
                            Climate.Parameter.span(-1.0f, 1.0f),
                            0L
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
}
