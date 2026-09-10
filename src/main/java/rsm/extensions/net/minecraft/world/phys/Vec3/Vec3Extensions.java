package rsm.extensions.net.minecraft.world.phys.Vec3;

import com.ricedotwho.rsm.utils.MathUtils;
import lombok.val;
import manifold.ext.rt.api.ComparableUsing;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.ricedotwho.rsm.type.Accessor.mc;

@Extension
@SuppressWarnings("unused")
public abstract class Vec3Extensions implements ComparableUsing<Vec3> {
  public static String toNiceString(@This Vec3 vec) {
    return  "x: " + vec.x
            + ", y:" + vec.y
            + ", z: " + vec.z;
  }
  public static String toChatString(@This Vec3 vec) {
    return vec.x
            + "," + vec.y
            + "," + vec.z;
  }

  public static @Nullable Block getBlock(@This Vec3 vec) {
    return vec.toBlockPos().getBlock();
  }

  public static boolean isBlockOrDefault(@This Vec3 vec, boolean defaultValue, Block block) {
    return vec.toBlockPos().isBlockOrDefault(defaultValue, block);
  }

  public static boolean isBlockOrDefault(@This Vec3 vec, boolean defaultValue, Block... blocks) {
    return vec.toBlockPos().isBlockOrDefault(defaultValue, blocks);
  }

  public static @Nullable Boolean isBlock(@This Vec3 vec, Block block) {
    return vec.toBlockPos().isBlock(block);
  }

  public static @Nullable Boolean isBlock(@This Vec3 vec, Block... blocks) {
    return vec.toBlockPos().isBlock(blocks);
  }


  public static BlockPos toBlockPos(@This Vec3 vec) {
    return new BlockPos(Mth.floor(vec.x), Mth.floor(vec.y), Mth.floor(vec.z));
  }

  public static double get(@This Vec3 vec, int index) {
    return switch (index) {
      case 0 -> vec.x;
      case 1 -> vec.y;
      case 2 -> vec.z;
      default -> -1;
    };
  }

  public static Vec3 snapToIncrement(@This Vec3 vec, final double increment) {
    return new Vec3(MathUtils.snapToIncrement(vec.x, increment), MathUtils.snapToIncrement(vec.y, increment), MathUtils.snapToIncrement(vec.z, increment));
  }

  public static Vec3 snapToIncrementIgnoreY(@This Vec3 vec, final double increment) {
    return new Vec3(MathUtils.snapToIncrement(vec.x, increment), vec.y, MathUtils.snapToIncrement(vec.z, increment));
  }

  public static Vec3 average(@This Vec3 vec, final Vec3 other) {
    return new Vec3(
            (vec.x + other.x) / 2,
            (vec.y + other.y) / 2,
            (vec.z + other.z) / 2
    );
  }

  public static Vec3 plus(@This Vec3 vec, Vec3 other) {
    return vec.add(other);
  }

  public static Vec3 minus(@This Vec3 vec, Vec3 other) {
    return vec.subtract(other);
  }

  public static Vec3 times(@This Vec3 vec, double scaler) {
    return vec.scale(scaler);
  }

  public static Vec3 div(@This Vec3 vec, double denominator) {
    val scaler = 1d / denominator;
    return vec.scale(scaler);
  }

  public static Vec3 rem(@This Vec3 vec, double denominator) {
    return new Vec3(vec.x % denominator, vec.y % denominator, vec.z % denominator);
  }

  public static Vec3 add(@This Vec3 vec, final Direction.Axis axis, final double factor) {
    return switch (axis) {
      case X -> new Vec3(vec.x + factor, vec.y, vec.z);
      case Y -> new Vec3(vec.x, vec.y + factor, vec.z);
      case Z -> new Vec3(vec.x, vec.y, vec.z + factor);
    };
  }
  
  public static Vec3 above(@This Vec3 vec) {
    return vec.add(0, 1, 0);
  }

  public static Vec3 multiply(@This Vec3 vec, final double factor) {
    return vec.multiply(factor, factor, factor);
  }

  public static Vec3 divide(@This Vec3 vec, final double x, final double y, final double z) {
    return new Vec3(vec.x / x, vec.y / y, vec.z / z);
  }

  public static Vec3 divide(@This Vec3 vec, final double f) {
    return vec.divide(f, f, f);
  }

  public static Vec3 sign(@This Vec3 vec) {
    return new Vec3(Math.signum(vec.x), Math.signum(vec.y), Math.signum(vec.z));
  }

  public static Vec3 floor(@This Vec3 vec) {
    return new Vec3(Mth.floor(vec.x), Mth.floor(vec.y), Mth.floor(vec.z));
  }

  public static Vec3 round(@This Vec3 vec) {
    return new Vec3(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
  }

  public static Vec3 round(@This Vec3 vec, final int places) {
    double factor = Math.pow(10, places);
    return new Vec3(Math.round(vec.x * factor) / factor, Math.round(vec.y * factor) / factor, Math.round(vec.z * factor) / factor);
  }

  public static Vec3 shift(@This Vec3 vec, Direction dir, double amount) {
    return switch (dir) {
      case UP -> vec.add(0, amount, 0);
      case DOWN -> vec.add(0, -amount, 0);
      case WEST -> vec.add(-amount, 0, 0);
      case SOUTH -> vec.add(0, 0, amount);
      case NORTH -> vec.add(0, 0, -amount);
      case EAST -> vec.add(amount, 0, 0);
      case null -> vec;
    };
  }

  public static AABB getAABB(@This Vec3 vec) {
    if (mc.level == null) return Shapes.block().bounds();
    BlockPos bp = vec.toBlockPos();
    BlockState state = mc.level.getBlockState(bp);
    VoxelShape shape = state.getShape(mc.level, bp);
    if (shape.isEmpty()) return Shapes.block().bounds().move(bp);
    return shape.bounds().move(bp);
  }
}