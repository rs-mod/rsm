package com.ricedotwho.rsm.utils;

import java.util.Random;

public class MathUtils {

    private static final Random theRandom = new Random();

    public static float nextFloat(float min, float max) {
        return theRandom.nextFloat() * (max - min) + min;
    }

    public static double nextDouble(double min, double max) {
        return theRandom.nextDouble() * (max - min) + min;
    }

    public static double wrappedDifference(double number1, double number2) {
        return Math.min(
                Math.abs(number1 - number2),
                Math.min(
                        Math.abs(number1 - 360) - Math.abs(number2 - 0),
                        Math.abs(number2 - 360) - Math.abs(number1 - 0)
                )
        );
    }

}