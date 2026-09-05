package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@UtilityClass
@SuppressWarnings("unused")
public class MathUtils {
    public double truncate(double d, int decimalPlace) {
        val multiplier = Math.pow(10, decimalPlace);
        d *= multiplier;
        d = Math.floor(d);
        d /= multiplier;

        return d;
    }

    public double snapToIncrement(double value, double increment) {
        val remainer = value / increment;
        return Math.round(remainer) * increment;
    }

    public int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }


    public double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public float lerp(final float a, final float b, final float t) {
        return a + (b - a) * t;
    }
    public double lerp(final double a, final double b, final float t) {
        return a + (b - a) * t;
    }
    public int lerp(final int a, final int b, final float t) {
        return a + Mth.floor(t * (float)(a - b));
    }

    public static final double OUT_MAGIC_1 = 1.70158;
    public static final double OUT_MAGIC_2 = OUT_MAGIC_1 + 1;
    public static final double OUT_MAGIC_3 = OUT_MAGIC_1 * 1.525;

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
        return Math.clamp(d, min, max);
    }

}