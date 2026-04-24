package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;

public class LevititeSpawnerBlockEntity extends BlockEntity {

    private boolean hasTriggered = false;
    private int tickDelay = 10;

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