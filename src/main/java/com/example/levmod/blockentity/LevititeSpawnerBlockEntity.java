package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

public class LevititeSpawnerBlockEntity extends BlockEntity {

    private static final String TARGET_BLOCK_KEY = "target_block";

    private boolean hasTriggered = false;
    private int tickDelay = 10;

    @Nullable
    private Block targetBlock = null;

    public LevititeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITITE_SPAWNER.get(), pos, state);
    }

    /**
     * Called by the placing feature immediately after setBlock so the entity
     * knows which block to check for neighbours and fill back with.
     */
    public void setTargetBlock(Block block) {
        this.targetBlock = block;
        this.setChanged();
    }

    private Block resolveTargetBlock() {
        if (targetBlock != null) return targetBlock;
        // Fallback keeps existing worlds working if NBT is missing.
        return BuiltInRegistries.BLOCK
                .getOptional(ResourceLocation.tryParse("aeronautics:levitite"))
                .orElse(Blocks.AIR);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (targetBlock != null) {
            tag.putString(TARGET_BLOCK_KEY,
                    BuiltInRegistries.BLOCK.getKey(targetBlock).toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TARGET_BLOCK_KEY)) {
            ResourceLocation loc = ResourceLocation.tryParse(tag.getString(TARGET_BLOCK_KEY));
            if (loc != null) {
                targetBlock = BuiltInRegistries.BLOCK.getOptional(loc).orElse(null);
            }
        }
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
                anchor, serverLevel, 10_000, null);

        if (result.assemblyState() == SubLevelAssemblyHelper.GatherResult.State.SUCCESS) {
            Block target = be.resolveTargetBlock();

            int neighbors = 0;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(target)) {
                    neighbors++;
                }
            }

            if (neighbors >= 3) {
                level.setBlock(pos, target.defaultBlockState(), 3);
            } else {
                level.removeBlock(pos, false);
            }

            ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                    serverLevel, anchor, result.blocks(), result.boundingBox());

            SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(serverLevel);
            if (system != null) {
                double yaw   = serverLevel.random.nextDouble() * Math.PI * 2.0;
                double pitch = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;
                double roll  = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;

                system.getPipeline().teleport(
                        subLevel,
                        subLevel.logicalPose().position(),
                        new Quaterniond().rotateY(yaw).rotateX(pitch).rotateZ(roll));
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
                    if (!level.getBlockState(candidate).isAir()) return candidate;
                }
            }
        }
        return null;
    }
}