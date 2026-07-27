package com.example.levmod;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final ModConfigSpec SPEC;
    public static final Config INSTANCE;

    static {
        Pair<Config, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(Config::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue generateLevititeFields;
    public final ModConfigSpec.ConfigValue<Float> levititeFieldsTemperatureModifier;
    public final ModConfigSpec.ConfigValue<Float> levititeFieldsHumidityModifier;
    public final ModConfigSpec.ConfigValue<Float> levititeFieldsContinetalnessModifier;
    public final ModConfigSpec.ConfigValue<Float> levititeFieldsErosionModifier;
    public final ModConfigSpec.ConfigValue<Float> levititeFieldsWeirdnessModifier;


    public final ModConfigSpec.BooleanValue generateEndLevititeFields;
    public final ModConfigSpec.DoubleValue endLevititeFieldsUpperChance;
    public final ModConfigSpec.DoubleValue endLevititeFieldsLowerChance;

    public final ModConfigSpec.BooleanValue generatePaintedLevititeFields;
    public final ModConfigSpec.DoubleValue paintedLevititeFieldsUpperChance;
    public final ModConfigSpec.DoubleValue paintedLevititeFieldsLowerChance;

    public Config(ModConfigSpec.Builder builder) {
        generateLevititeFields = builder
                .comment("Should the Levitite Fields Biome generate. Default: true")
                .define("Generate Levitite Fields", true);

        levititeFieldsTemperatureModifier = builder
                .comment("Due to how the Levitite Fields biome had to be set I had to allow you to modify the values that determine where it generates.\n" +
                        "In general shrinking the modifier numbers will shrink the amount and/or size of Levitite Fields biomes that generate")
                .define("Temperature Modifier", 1.0f);

        levititeFieldsHumidityModifier = builder
                .define("Humidity Modifier", 1.0f);

        levititeFieldsContinetalnessModifier = builder
                .define("Continetalness Modifier", 1.0f);

        levititeFieldsErosionModifier = builder
                .define("Erosion Modifier", 1.0f);

        levititeFieldsWeirdnessModifier = builder
                .define("Weirdness Modifier", 1.0f);


        generateEndLevititeFields = builder
                .comment("Should the End Levitite Fields Biome generate. Default: true")
                .define("Generate End Levitite Fields", true);

        endLevititeFieldsLowerChance = builder
                .comment("How common should the End Levitite Fields biome generate, do not put the lower value higher than the upper value\nLower value: ")
                .defineInRange("End Levitite Fields Lower Chance", 0.2f, 0.0, 1.0);

        endLevititeFieldsUpperChance = builder
                .comment("Upper Value: ")
                .defineInRange("End Levitite Fields Upper Chance", 0.75f, 0.0, 1.0f);


        generatePaintedLevititeFields = builder
                .comment("Should the Painted Levitite Fields Biome generate. Default: true")
                .define("Generate Painted Levitite Fields", true);

        paintedLevititeFieldsLowerChance = builder
                .comment("How common should the Painted Levitite Fields biome generate, do not put the lower value higher than the upper value\nLower value: ")
                .defineInRange("Painted Levitite Fields Lower Chance", 0.2f, 0.0 , 1.0);

        paintedLevititeFieldsUpperChance = builder
                .comment("Upper Value: ")
                .defineInRange("Painted Levitite Fields Upper Chance", 0.5f, 0.0, 1.0f);
    }
}