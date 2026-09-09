package rsm.extensions.com.google.gson.JsonObject;

import com.google.gson.JsonPrimitive;
import com.ricedotwho.rsm.type.Rotation;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.google.gson.JsonObject;
import net.minecraft.world.phys.Vec3;

@Extension
public class JsonObjectExtensions {
  public static void addProperty(@This JsonObject obj, String name, Vec3 vec) {
    obj.add(name, new JsonPrimitive(vec.x + " " + vec.y + " " + vec.z));
  }

  public static void addProperty(@This JsonObject obj, String name, Rotation rotation) {
    obj.add(name, new JsonPrimitive(rotation.getYaw() + " " + rotation.getPitch()));
  }
}