package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

@UtilityClass
public class MathUtils {

    private final Random theRandom = new Random();

    public float nextFloat(float min, float max) {
        return theRandom.nextFloat() * (max - min) + min;
    }

    public double nextDouble(double min, double max) {
        return theRandom.nextDouble() * (max - min) + min;
    }

    public double wrappedDifference(double number1, double number2) {
        return Math.min(
                Math.abs(number1 - number2),
                Math.min(
                        Math.abs(number1 - 360) - Math.abs(number2 - 0),
                        Math.abs(number2 - 360) - Math.abs(number1 - 0)
                )
        );
    }

    public Vec3 clamp(AABB aabb, Vec3 vec3) {
        return new Vec3(clamp(vec3.x, aabb.minX, aabb.maxX), clamp(vec3.y, aabb.minY, aabb.maxY), clamp(vec3.z, aabb.minZ, aabb.maxZ));
    }

    public double clamp(double d, double min, double max) {
        return Math.min(max, Math.max(d, min));
    }

}