package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class RotationUtils {
    public float wrapAngleTo360(float angle) {
        angle %= 360;
        if(angle < 0) angle += 360;
        return angle;
    }

    public float wrapAngleTo180(float angle) {
        angle = angle % 360.0f;

        while (angle >= 180) {
            angle -= 360.0f;
        }
        while (angle < -180.0f) {
            angle += 360.0f;
        };
        return angle;
    }

    public double wrapAngleTo180(double angle) {
        angle = angle % 360.0;

        while (angle >= 180) {
            angle -= 360.0;
        }
        while (angle < -180.0) {
            angle += 360.0;
        };
        return angle;
    }

    public Rotation getRotation(final Vec3 from, final Vec3 to) {
        double diffX = to.x() - from.x();
        double diffY = to.y() - from.y();
        double diffZ = to.z() - from.z();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static HitResult getBlockHitResult(double d, float yaw, float pitch, Vec3 eyePos) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return null;
        Vec3 vec32 = Minecraft.getInstance().player.calculateViewVector(pitch, yaw); // Reversed for some reason
        Vec3 vec33 = eyePos.add(vec32.x * d, vec32.y * d, vec32.z * d);
        return Minecraft.getInstance().level.clip(new ClipContext(eyePos, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.placementContext(Minecraft.getInstance().player)));
    }

    public static BlockHitResult collisionRayTrace(BlockPos pos, AABB box, Vec3 start, Vec3 end) {
        Vec3 localStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3 localEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());

        Optional<Vec3> hit = box.clip(localStart, localEnd);

        if (hit.isEmpty()) {
            return null;
        }

        Vec3 hitPosLocal = hit.get();
        Vec3 hitPosWorld = hitPosLocal.add(pos.getX(), pos.getY(), pos.getZ());

        Direction face = Direction.getApproximateNearest(hitPosLocal.x - box.getCenter().x, hitPosLocal.y - box.getCenter().y, hitPosLocal.z - box.getCenter().z);
        return new BlockHitResult(hitPosWorld, face, pos, false);
    }

    public Rotation getRotationAABB(final Vec3 from, final AABB to) {
        double randomX = to.minX == to.maxX ? to.maxX : ThreadLocalRandom.current().nextDouble(to.minX, to.maxX);
        double randomY = to.minY == to.maxY ? to.maxY : ThreadLocalRandom.current().nextDouble(to.minY, to.maxY);
        double randomZ = to.minZ == to.maxZ ? to.maxZ : ThreadLocalRandom.current().nextDouble(to.minZ, to.maxZ);

        double diffX = randomX - from.x();
        double diffY = randomY - from.y();
        double diffZ = randomZ - from.z();

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);

        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public Rotation getRotationAABBExact(final Vec3 from, final AABB to) {
        double diffX = to.maxX - from.x();
        double diffY = to.maxY - from.y();
        double diffZ = to.maxZ - from.z();

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);

        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }
}
