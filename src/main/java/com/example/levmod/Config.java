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
    public final ModConfigSpec.DoubleValue levititeFieldsUpperChance;
    public final ModConfigSpec.DoubleValue levititeFieldsLowerChance;

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

        levititeFieldsLowerChance = builder
                .comment("How common should the Levitite Fields biome generate, do not put the lower value higher than the upper value\nLower value: ")
                .defineInRange("Levitite Fields Lower Chance", 0.1f, 0.0, 1.0);

        levititeFieldsUpperChance = builder
                .comment("Upper Value: ")
                .defineInRange("Levitite Fields Upper Chance", 0.5f, 0.0, 1.0f);


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