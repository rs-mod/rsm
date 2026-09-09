package rsm.extensions.com.google.gson.JsonElement;

import com.ricedotwho.rsm.type.Rotation;
import lombok.val;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.google.gson.JsonElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Extension
@SuppressWarnings("unused")
public class JsonElementExtension {
  public static AABB getAABB(@This JsonElement element) {
    String[] parts = element.getAsString().trim().split("\\s+");
    if (parts.length != 6) {
      throw new IllegalArgumentException("Invalid AABB format: \"" + element.getAsString() + "\"");
    }

    val x1 = Double.parseDouble(parts[0]);
    val y1 = Double.parseDouble(parts[1]);
    val z1 = Double.parseDouble(parts[2]);
    val x2 = Double.parseDouble(parts[3]);
    val y2 = Double.parseDouble(parts[4]);
    val z2 = Double.parseDouble(parts[5]);
    return new AABB(x1, y1, z1, x2, y2, z2);
  }

  public static Vec3 getAsVec3(@This JsonElement element) {
    String[] parts = element.getAsString().trim().split("\\s+");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid Vec3 format: \"" + element.getAsString() + "\"");
    }

    double x = Double.parseDouble(parts[0]);
    double y = Double.parseDouble(parts[1]);
    double z = Double.parseDouble(parts[2]);
    return new Vec3(x, y, z);
  }

  public static Rotation getAsRotation(@This JsonElement element) throws IllegalArgumentException {
    String[] parts = element.getAsString().trim().split("\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid Rotation format: \"" + element.getAsString() + "\"");
    }
    float yaw = Float.parseFloat(parts[0]);
    float pitch = Float.parseFloat(parts[1]);
    return new Rotation(yaw, pitch);
  }

  public static BlockPos getAsBlockPos(@This JsonElement element) throws IllegalArgumentException {
    String[] parts = element.getAsString().trim().split("\\s+");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid Vec3 format: \"" + element.getAsString() + "\"");
    }

    int x = Integer.parseInt(parts[0]);
    int y = Integer.parseInt(parts[1]);
    int z = Integer.parseInt(parts[2]);
    return new BlockPos(x, y, z);
  }

  public static Direction getAsDirection(@This JsonElement element) {
    return Direction.valueOf(element.getAsString());
  }
}