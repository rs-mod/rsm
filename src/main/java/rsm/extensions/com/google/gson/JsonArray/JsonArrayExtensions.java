package rsm.extensions.com.google.gson.JsonArray;

import com.google.gson.JsonPrimitive;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import com.google.gson.JsonArray;
import net.minecraft.world.phys.Vec3;

@Extension
public class JsonArrayExtensions {
  public static void add(@This JsonArray array, Vec3 vec) {
    array.add(new JsonPrimitive(vec.x + " " + vec.y + " " + vec.z));
  }
}
