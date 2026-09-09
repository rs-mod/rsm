package rsm.extensions.net.minecraft.core.BlockPos;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Extension
@SuppressWarnings("unused")
public class BlockPosExtension {
  public static Vec3 asVec3(@This BlockPos pos) {
    return new Vec3(pos.x, pos.y, pos.z);
  }
}