package com.example.levmod.mixin;

import com.example.levmod.LevMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(TheEndBiomeSource.class)
public class TheEndBiomeSourceMixin {

    @Unique
    private static final ThreadLocal<Holder<Biome>> PENDING_BIOME = new ThreadLocal<>();

    @Unique
    private @Nullable Holder<Biome> levmod_endBiome = null;

    @Unique
    private static final SimplexNoise PLACEMENT_NOISE =
            new SimplexNoise(new LegacyRandomSource(0xDEADBEEFL));

    @Inject(method = "create", at = @At("HEAD"))
    private static void levmod_captureHolder(
            HolderGetter<Biome> getter,
            CallbackInfoReturnable<TheEndBiomeSource> cir) {
        getter.get(LevMod.LEVITITE_FIELDS_END).ifPresent(PENDING_BIOME::set);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void levmod_storeHolder(
            Holder<Biome> end,
            Holder<Biome> highlands,
            Holder<Biome> midlands,
            Holder<Biome> islands,
            Holder<Biome> barrens,
            CallbackInfo ci) {
        this.levmod_endBiome = PENDING_BIOME.get();
        PENDING_BIOME.remove();
    }

    @Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
    private void levmod_addPossibleBiome(
            CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
        if (levmod_endBiome == null) return;
        cir.setReturnValue(Stream.concat(cir.getReturnValue(), Stream.of(levmod_endBiome)));
    }

    @Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
    private void levmod_injectBiome(
            int x, int y, int z,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        if (levmod_endBiome == null) return;
        if (!cir.getReturnValue().is(Biomes.END_HIGHLANDS)) return;

        double noise = PLACEMENT_NOISE.getValue(x * 0.075, 0, z * 0.075);
        if (noise > 0.2) {
            cir.setReturnValue(levmod_endBiome);
        }
    }
}