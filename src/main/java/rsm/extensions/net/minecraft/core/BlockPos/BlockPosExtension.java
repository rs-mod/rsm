package rsm.extensions.net.minecraft.core.BlockPos;

import com.ricedotwho.rsm.utils.WorldUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Extension
public class BlockPosExtension {
  public static Vec3 asVec3(@This BlockPos pos) {
    return new Vec3(pos.x, pos.y, pos.z);
  }

  public static @Nullable Block getBlock(@This BlockPos blockPos) {
    return WorldUtils.getBlockAt(blockPos);
  }

  public static boolean isBlockOrDefault(@This BlockPos blockPos, boolean defaultValue, Block block) {
    return WorldUtils.isBlockOrDefault(blockPos, defaultValue, block);
  }

  public static boolean isBlockOrDefault(@This BlockPos blockPos, boolean defaultValue, Block... blocks) {
    return WorldUtils.isBlockOrDefault(blockPos, defaultValue, blocks);
  }

  public static @Nullable Boolean isBlock(@This BlockPos blockPos, Block block) {
    return WorldUtils.isBlock(blockPos, block);
  }

  public static @Nullable Boolean isBlock(@This BlockPos blockPos, Block... blocks) {
    return WorldUtils.isBlock(blockPos, blocks);
  }

}