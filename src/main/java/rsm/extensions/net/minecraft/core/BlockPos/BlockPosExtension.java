package rsm.extensions.net.minecraft.core.BlockPos;

import com.ricedotwho.rsm.utils.WorldUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Extension
@SuppressWarnings("unused")
public class BlockPosExtension {
  public static Vec3 toVec3(@This BlockPos pos) {
    return new Vec3(pos.x, pos.y, pos.z);
  }
  
  public static BlockPos plus(@This BlockPos pos, Vec3i other) {
    return pos.add(other);
  }

  public static BlockPos minus(@This BlockPos pos, Vec3i other) {
    return pos.subtract(other);
  }

  public static BlockPos times(@This BlockPos pos, int scaler) {
    return new BlockPos(pos.x * scaler, pos.y * scaler, pos.z * scaler);
  }

  public static BlockPos div(@This BlockPos pos, int denominator) {
    return new BlockPos(pos.x / denominator, pos.y / denominator, pos.z / denominator);
  }

  public static int compareTo(@This BlockPos vec, BlockPos other) {
    return Double.compare(vec.lengthSqr(), other.lengthSqr());
  }

  public static double lengthSqr(@This BlockPos vec) {
    return vec.x * vec.x + vec.y * vec.y + vec.z * vec.z;
  }

  public static double length(@This BlockPos vec) {
    return Math.sqrt(lengthSqr(vec));
  }

  public static BlockPos add(@This BlockPos pos, Vec3i other) {
    return new BlockPos(pos.x + other.x, pos.y + other.y, pos.z + other.z);
  }

  public static @Nullable String getSkullTexture(@This BlockPos pos) {
    return WorldUtils.getSkullTextureAt(pos);
  }

  public static @Nullable SkullBlockEntity getSkullAt(@This BlockPos pos) {
    return WorldUtils.getSkullAt(pos);
  }

  public static @Nullable Block getBlock(@This BlockPos pos) {
    return WorldUtils.getBlockAt(pos);
  }

  public static boolean isBlockOrDefault(@This BlockPos pos, boolean defaultValue, Block block) {
    return WorldUtils.isBlockOrDefault(pos, defaultValue, block);
  }

  public static boolean isBlockOrDefault(@This BlockPos pos, boolean defaultValue, Block... blocks) {
    return WorldUtils.isBlockOrDefault(pos, defaultValue, blocks);
  }
  public static @Nullable Boolean isBlock(@This BlockPos pos, Block block) {
    return WorldUtils.isBlock(pos, block);
  }

  public static @Nullable Boolean isBlock(@This BlockPos pos, Block... blocks) {
    return WorldUtils.isBlock(pos, blocks);
  }

  public static List<BlockPos> getHorizontals(@This BlockPos pos) {
    return List.of(pos.offset(1, 0, 0), pos.offset(-1, 0, 0), pos.offset(0, 0, 1), pos.offset(0, 0, -1));
  }

  public static AABB getAABB(@This BlockPos pos) {
    if (mc.level == null) return Shapes.block().bounds();
    BlockState state = mc.level.getBlockState(pos);
    VoxelShape shape = state.getShape(mc.level, pos);
    if (shape.isEmpty()) return Shapes.block().bounds().move(pos);
    return shape.bounds().move(pos);
  }
}