package rsm.extensions.com.google.gson.JsonElement;

import com.ricedotwho.rsm.type.Rotation;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.google.gson.JsonElement;
import net.minecraft.world.phys.Vec3;

@Extension
@SuppressWarnings("unused")
public class JsonElementExtension {
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
}