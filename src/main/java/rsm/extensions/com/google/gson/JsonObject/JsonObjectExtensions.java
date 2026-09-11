package rsm.extensions.com.google.gson.JsonObject;

import com.google.gson.JsonPrimitive;
import com.ricedotwho.rsm.type.Rotation;
import com.ricedotwho.rsm.utils.MathUtils;
import lombok.val;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.util.Objects;

@Extension
@SuppressWarnings("unused")
public class JsonObjectExtensions {
  public static void addPropertyWithDefault(@This JsonObject obj, String name, boolean bool) {
    if (bool) obj.addProperty(name, true);
  }

  public static boolean getOrDefault(@This JsonObject obj, String name, boolean defaultValue) {
    if (obj.has(name)) return obj.get(name).asBoolean;
    return defaultValue;
  }

  public static <T extends Number> T getOrDefault(@This JsonObject obj, String name, T defaultValue) throws IllegalArgumentException {
    if (!obj.has(name)) return defaultValue;

    return (T) switch (defaultValue) {
      case BigDecimal _ -> obj.get(name).getAsBigDecimal();
      case Integer _ -> obj.get(name).getAsInt();
      case Long _ -> obj.get(name).getAsLong();
      case Short _ -> obj.get(name).getAsShort();
      case Byte _ -> obj.get(name).getAsByte();
      case Double _ -> obj.get(name).getAsDouble();
      case Float _ -> obj.get(name).getAsFloat();
      default -> throw new IllegalArgumentException("Unsupported number type!");
    };
  }


  public static <T extends Number> void addPropertyWithDefault(@This JsonObject obj, String name, T value, T defaultValue) {
    if (Objects.equals(value, defaultValue)) return;
    obj.addProperty(name, value);
  }

  public static void addProperty(@This JsonObject obj, String name, Vec3 vec) {
    obj.add(name, new JsonPrimitive(vec.x + " " + vec.y + " " + vec.z));
  }

  public static void addProperty(@This JsonObject obj, String name, Rotation rotation) {
    obj.add(name, new JsonPrimitive(rotation.getYaw() + " " + rotation.getPitch()));
  }

  public static void addProperty(@This JsonObject obj, String name, AABB aabb) {
    val string = MathUtils.truncate(aabb.minX, 2) + " " +
            MathUtils.truncate(aabb.minY, 2) + " " +
            MathUtils.truncate(aabb.minZ, 2) + " " +
            MathUtils.truncate(aabb.maxX, 2) + " " +
            MathUtils.truncate(aabb.maxY, 2) + " " +
            MathUtils.truncate(aabb.maxZ, 2);
    obj.add(name, new JsonPrimitive(string));
  }

  public static void addProperty(@This JsonObject obj, String name, BlockPos blockPos) {
    obj.addProperty(name, "${blockPos.getX()} ${blockPos.getY()} ${blockPos.getZ()}");
  }


  public static void addProperty(@This JsonObject object, String name, Direction direction) {
    object.addProperty(name, direction.name());
  }


}
