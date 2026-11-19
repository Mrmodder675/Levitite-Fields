package com.example.levmod.config.data;

/**
 * Noise bands that decide which cluster size may generate where in the Levitite Fields
 * biome. Each overworld placed feature keeps a candidate only when
 * {@code levmod:levitite_fields_feature_noise} lies inside its band (see the
 * {@code yetaml:density_threshold} entries, which read these values as {@code $levmod:...}).
 *
 * <p>Measured 2026-11-19 on MC 1.21.1 (3 seeds, ~1.9M samples each, y 86..310): the
 * function spans about -1.17..1.17 (sd 0.28), although {@code minecraft:patch}'s theoretical
 * maximum is 2.904. Negative values (half the volume) stay empty, which keeps gaps between
 * fields. The positive half is split at its quartiles, so each size gets ~12.5% of the
 * volume: large clusters fill the cores of each noise blob, fading to sparse at the edges.</p>
 */
public class LevmodJavaVariables {
    public static double sparseLeviteClusterNoiseValueUpper = 0.09d;
    public static double sparseLeviteClusterNoiseValueLower = 0d;

    public static double smallLeviteClusterNoiseValueUpper = 0.19d;
    public static double smallLeviteClusterNoiseValueLower = 0.09d;

    public static double mediumLeviteClusterNoiseValueUpper = 0.325d;
    public static double mediumLeviteClusterNoiseValueLower = 0.19d;

    public static double largeLeviteClusterNoiseValueUpper = 2.9d;
    public static double largeLeviteClusterNoiseValueLower = 0.325d;
}
