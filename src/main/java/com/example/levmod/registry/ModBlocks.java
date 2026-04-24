package com.example.levmod.registry;

import com.example.levmod.LevMod;
import com.example.levmod.block.LevititeSpawnerBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LevMod.MOD_ID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(LevMod.MOD_ID);

    // ── Blocks ────────────────────────────────────────────────────────────────



    /** Invisible marker block placed by the worldgen feature. Never obtainable. */
    public static final DeferredBlock<LevititeSpawnerBlock> LEVITITE_SPAWNER =
            BLOCKS.register("levitite_spawner", LevititeSpawnerBlock::new);

}



