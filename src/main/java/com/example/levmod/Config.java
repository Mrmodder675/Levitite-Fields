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
    public final ModConfigSpec.BooleanValue generateEndLevititeFields;
    public final ModConfigSpec.BooleanValue generatePaintedLevititeFields;

    public Config(ModConfigSpec.Builder builder) {
        generateLevititeFields = builder
                .comment("Should the Levitite Fields Biome generate. Default: true")
                .define("generateLevititeFields", true);

        generateEndLevititeFields = builder
                .comment("Should the End Levitite Fields Biome generate. Default: true")
                .define("generateEndLevititeFields", true);

        generatePaintedLevititeFields = builder
                .comment("Should the Painted Levitite Fields Biome generate. Default: true")
                .define("generatePaintedLevititeFields", true);
    }
}