package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import java.util.function.Supplier;

public class LevititeSpawnerBlockEntity extends BlockEntity {

    private boolean hasTriggered = false;
    private int tickDelay = 10;

    private static final Supplier<BlockState> LEVITITE_STATE = Suppliers.memoize(() -> 
        BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse("aeronautics:levitite"))
                .map(Block::defaultBlockState)
                .orElse(Blocks.AIR.defaultBlockState())
    );

    public LevititeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITITE_SPAWNER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            LevititeSpawnerBlockEntity be) {
        if (level.isClientSide || be.hasTriggered) return;
        if (be.tickDelay-- > 0) return;
        be.hasTriggered = true;

        ServerLevel serverLevel = (ServerLevel) level;

        BlockPos anchor = findNeighbourAnchor(serverLevel, pos);

        if (anchor == null) {
            level.removeBlock(pos, false);
            return;
        }

        SubLevelAssemblyHelper.GatherResult result = SubLevelAssemblyHelper.gatherConnectedBlocks(
                anchor,
                serverLevel,
                10_000,
                null
        );

        if (result.assemblyState() == SubLevelAssemblyHelper.GatherResult.State.SUCCESS) {
            Block targetBlock = LEVITITE_STATE.get().getBlock();

            int levititeNeighbors = 0;
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (level.getBlockState(neighborPos).is(targetBlock)) {
                    levititeNeighbors++;
                }
            }

            // If it matches 3 or more, replace with levitite; otherwise, clear it
            if (levititeNeighbors >= 3) {
                level.setBlock(pos, LEVITITE_STATE.get(), 3);
            } else {
                level.removeBlock(pos, false);
            }
            
            ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                    serverLevel,
                    anchor,
                    result.blocks(),
                    result.boundingBox()
            );

            SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(serverLevel);
            if (system != null) {
                double yaw   = serverLevel.random.nextDouble() * Math.PI * 2.0;
                double pitch = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;
                double roll  = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;

                Quaterniond orientation = new Quaterniond()
                        .rotateY(yaw)
                        .rotateX(pitch)
                        .rotateZ(roll);

                system.getPipeline().teleport(
                        subLevel,
                        subLevel.logicalPose().position(),
                        orientation
                );
            }
            return;
        }

        level.removeBlock(pos, false);
    }

    private static BlockPos findNeighbourAnchor(ServerLevel level, BlockPos spawnerPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos candidate = spawnerPos.offset(dx, dy, dz).immutable();
                    if (!level.getBlockState(candidate).isAir()) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}