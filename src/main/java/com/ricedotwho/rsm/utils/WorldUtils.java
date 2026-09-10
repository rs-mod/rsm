package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ricedotwho.rsm.type.Accessor.mc;

@UtilityClass
@SuppressWarnings("unused")
public class WorldUtils {

    public @NotNull Level getLevel() {
        assert mc.level != null;
        return mc.level;
    }

    public List<BlockPos> getHorizontals(@NotNull BlockPos pos) {
        return List.of(pos.offset(1, 0, 0), pos.offset(-1, 0, 0), pos.offset(0, 0, 1), pos.offset(0, 0, -1));
    }

    public boolean hasChunk(@NotNull BlockPos pos) {
        return getLevel().hasChunk(pos.getX() >> 4, pos.getZ() >> 4) && !(getLevel().getChunk(pos.getX() >> 4, pos.getZ() >> 4) instanceof EmptyLevelChunk);
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        return getLevel().hasChunk(chunkX, chunkZ);
    }

    public ChunkAccess getChunkOrNull(@NotNull BlockPos pos) {
        return getChunkOrNull(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public ChunkAccess getChunk(@NotNull BlockPos pos) {
        return getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public @Nullable ChunkAccess getChunkOrNull(int chunkX, int chunkZ) {
        val level = getLevel();
        if (!level.hasChunk(chunkX, chunkZ)) return null;

        val chunk = level.getChunk(chunkX, chunkZ);
        if (chunk instanceof EmptyLevelChunk) return null;

        return level.getChunk(chunkX, chunkZ);
    }

    public ChunkAccess getChunk(int chunkX, int chunkZ) {
        return getLevel().getChunk(chunkX, chunkZ);
    }
    
    public @Nullable String getSkullTextureAt(@NotNull BlockPos pos) {
        val skull = getSkullAt(pos);
        if (skull == null) return null;
        val profile = skull.getOwnerProfile();
        if (profile == null) return null;

        return profile.partialProfile().id().toString();
    }

    public @Nullable SkullBlockEntity getSkullAt(BlockPos pos) {
        val entity = getLevel().getBlockEntity(pos);
        if (entity instanceof SkullBlockEntity skull) return skull;
        return null;
    }

    public @Nullable Block getBlockAt(@NotNull BlockPos pos) {
        val block = getLevel().getBlockState(pos).getBlock();
        if (block == Blocks.VOID_AIR) return null;
        return block;
    }

    public boolean isBlockOrDefault(@NotNull BlockPos pos, boolean defaultValue, @NotNull Block block) {
        val isBlock = isBlock(pos, block);
        if (isBlock == null) return defaultValue;
        return isBlock;
    }

    public boolean isBlockOrDefault(@NotNull BlockPos pos, boolean defaultValue, @NotNull Block... block) {
        val isBlock = isBlock(pos, block);
        if (isBlock == null) return defaultValue;
        return isBlock;
    }

    public @Nullable Boolean isBlock(@NotNull BlockPos pos, @NotNull Block block) {
        val blockAt = getLevel().getBlockState(pos).getBlock();
        if (blockAt == Blocks.VOID_AIR) return null;
        return blockAt == block;
    }

    public @Nullable Boolean isBlock(@NotNull BlockPos pos, @NotNull Block... blocks) {
        val blockAt = getLevel().getBlockState(pos).getBlock();
        if (blockAt == Blocks.VOID_AIR) return null;

        for (Block block : blocks) {
            if (blockAt == block) return true;
        }

        return false;
    }
}
