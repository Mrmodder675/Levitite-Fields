package com.example.levmod.blockentity;

import com.example.levmod.registry.ModBlockEntities;
import com.example.levmod.registry.ModBlocks;
import dev.ryanhcode.sable.Sable;
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
    private static final ResourceLocation DEFAULT_TARGET = ResourceLocation.fromNamespaceAndPath("aeronautics", "levitite");
    private static final int MAX_ASSEMBLY_BLOCKS = 10_000;
    /** A spawner slot is filled back in when at least this many faces touch the cluster block. */
    private static final int FILL_NEIGHBOURS = 3;
    /**
     * Assembly (flood fill, block moves, a new physics body) is the expensive step, and every
     * spawner in a freshly loaded area fires on the same tick. At most this many assemble per
     * server tick; the rest retry over the next few ticks.
     */
    private static final int MAX_ASSEMBLIES_PER_TICK = 8;

    // Server thread only (the ticker is never registered client-side). All dimensions share
    // the server's game time, so this is one budget per server tick.
    private static long budgetGameTime = Long.MIN_VALUE;
    private static int assembliesThisTick;

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
        return BuiltInRegistries.BLOCK.getOptional(DEFAULT_TARGET).orElse(Blocks.AIR);
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

        ServerLevel serverLevel = (ServerLevel) level;
        Block target = be.resolveTargetBlock();

        // Already inside a sub-level: a neighbouring cluster's assembly carried this spawner
        // along, and Sable rebuilt its block entity from NBT. Assembling again here would move
        // the whole body into a new sub-level and snap it to a new random rotation, so the
        // spawner only settles into its cluster block.
        if (Sable.HELPER.getContaining(serverLevel, pos) != null) {
            be.hasTriggered = true;
            settle(serverLevel, pos, target);
            return;
        }

        if (!tryTakeAssemblySlot(serverLevel)) {
            be.tickDelay = serverLevel.random.nextInt(4);
            return;
        }
        be.hasTriggered = true;

        BlockPos anchor = findNeighbourAnchor(serverLevel, pos);
        if (anchor == null) {
            level.removeBlock(pos, false);
            return;
        }

        SubLevelAssemblyHelper.GatherResult result = SubLevelAssemblyHelper.gatherConnectedBlocks(
                anchor, serverLevel, MAX_ASSEMBLY_BLOCKS, null);

        if (result.assemblyState() != SubLevelAssemblyHelper.GatherResult.State.SUCCESS) {
            level.removeBlock(pos, false);
            return;
        }

        // Overlapping clusters share one connected body, so it can hold other, not yet
        // triggered spawners. Settle every spawner before assembly: none may be moved into
        // the sub-level.
        Block spawnerBlock = ModBlocks.LEVITITE_SPAWNER.get();
        for (BlockPos member : result.blocks()) {
            if (member.equals(pos) || !serverLevel.getBlockState(member).is(spawnerBlock)) continue;
            Block memberTarget = serverLevel.getBlockEntity(member) instanceof LevititeSpawnerBlockEntity other
                    ? other.resolveTargetBlock() : target;
            settle(serverLevel, member, memberTarget);
        }
        settle(serverLevel, pos, target);

        ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                serverLevel, anchor, result.blocks(), result.boundingBox());

        SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(serverLevel);
        if (system != null && !subLevel.isRemoved()) {
            double yaw   = serverLevel.random.nextDouble() * Math.PI * 2.0;
            double pitch = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;
            double roll  = (serverLevel.random.nextDouble() - 0.5) * Math.PI * 2.0;

            system.getPipeline().teleport(
                    subLevel,
                    subLevel.logicalPose().position(),
                    new Quaterniond().rotateY(yaw).rotateX(pitch).rotateZ(roll));
        }
    }

    private static boolean tryTakeAssemblySlot(ServerLevel level) {
        long now = level.getGameTime();
        if (now != budgetGameTime) {
            budgetGameTime = now;
            assembliesThisTick = 0;
        }
        if (assembliesThisTick >= MAX_ASSEMBLIES_PER_TICK) return false;
        assembliesThisTick++;
        return true;
    }

    /** Replaces a spawner with its cluster block when enough faces touch the cluster, else air. */
    private static void settle(ServerLevel level, BlockPos pos, Block target) {
        int neighbours = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(target) && ++neighbours >= FILL_NEIGHBOURS) {
                level.setBlock(pos, target.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
        level.removeBlock(pos, false);
    }

    private static BlockPos findNeighbourAnchor(ServerLevel level, BlockPos spawnerPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos candidate = spawnerPos.offset(dx, dy, dz);
                    if (!level.getBlockState(candidate).isAir()) return candidate;
                }
            }
        }
        return null;
    }
}
