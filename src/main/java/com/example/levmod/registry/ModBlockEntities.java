package com.example.levmod.registry;

import com.example.levmod.LevMod;
import com.example.levmod.blockentity.LevititeSpawnerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LevMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LevititeSpawnerBlockEntity>>
            LEVITITE_SPAWNER = BLOCK_ENTITIES.register("levitite_spawner",
                    () -> BlockEntityType.Builder
                            .of(LevititeSpawnerBlockEntity::new, ModBlocks.LEVITITE_SPAWNER.get())
                            .build(null));


}
