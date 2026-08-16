package com.ricedotwho.rsm.ui.impl.animations;

import com.ricedotwho.rsm.ui.api.Animation;
import lombok.val;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;

public class CubicBezierAnimation extends Animation<Float> {
    private static final int INTERPOLATION_COUNT = 60;
    private static final int APPROXIMATION_ITERATIONS = 5;
    private static final float CALCULATION_INTERPOLATION_COUNT = INTERPOLATION_COUNT - 1f;
    private static final float EPSILON = 1e-6f;

    private static int hashBezierParams(float x1, float y1, float x2, float y2) {
        var result = Float.floatToIntBits(x1);
        result = 31 * result + Float.floatToIntBits(y1);
        result = 31 * result + Float.floatToIntBits(x2);
        return 31 * result + Float.floatToIntBits(y2);
    }

    private static HashMap<Integer, Float> generateCache(float x1, float y1, float x2, float y2) {
        val newCache = new HashMap<Integer, Float>();
        for (int i = 0; i < INTERPOLATION_COUNT; i++) {
            val x = i / ((float) INTERPOLATION_COUNT);
            val t = getT(x, x1, x2);
            val y = bezier(t, 0f, y1, y2, 1f);
            newCache.put(i, y);
        }
        return newCache;
    }

    private static final ConcurrentHashMap<Integer, HashMap<Integer, Float>> globalCache = new ConcurrentHashMap<>();
    private final float x1;
    private final float x2;
    private final float y1;
    private final float y2;

    @SuppressWarnings("unused")
    public CubicBezierAnimation(long duration, float x1, float y1, float x2, float y2) {
        super(duration);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        val hash = hashBezierParams(x1, y1, x2, y2);

        cache = globalCache.computeIfAbsent(hash, _ -> generateCache(x1, y1, x2, y2));
    }

    public CubicBezierAnimation(long duration) {
        super(duration);
        this.x1 = 0.4f;
        this.y1 = 0f;
        this.x2 = 0.2f;
        this.y2 = 1f;
        val hash = hashBezierParams(x1, y1, x2, y2);
        cache = globalCache.computeIfAbsent(hash, _ -> generateCache(x1, y1, x2, y2));
    }

    private final HashMap<Integer, Float> cache;

    @Override
    public Float get(Float start, Float end, boolean reverse) {
        if (!isAnimating()) return reverse ? start : end;

        val x = getPercent(reverse);

        val selector = Math.clamp((int) (x * CALCULATION_INTERPOLATION_COUNT), 0, INTERPOLATION_COUNT - 1);
        val interpolationProgress = (x * CALCULATION_INTERPOLATION_COUNT) - selector;

        val current = cache.get(selector);
        if (current == null) throw new RuntimeException("Failed to get cache at selector");

        float next;
        if (selector < INTERPOLATION_COUNT - 1) {
            val potentialNext = cache.get(selector + 1);
            next = potentialNext == null ? current : potentialNext;
        } else {
            next = current;
        }

        val animationValue = current + (next - current) * interpolationProgress;

        return start + (end - start) * animationValue;
    }

    private static float bezier(float t, float p0, float p1, float p2, float p3) {
        val u = 1 - t;
        return (u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3);
    }

    private static float bezierDerivative(float t, float p0, float p1, float p2, float p3) {
        val u = 1 - t;
        return (3 * u * u * (p1 - p0) + 6 * u * t * (p2 - p1) + 3 * t * t * (p3 - p2));
    }

    private static float getT(float x, float x1, float x2) {
        var t = x;
        for (int i = 1; i < APPROXIMATION_ITERATIONS; i++) {
            val xEstimate = bezier(t, 0f, x1, x2, 1f);
            val dx = bezierDerivative(t, 0f, x1, x2, 1f);
            if (abs(dx) < EPSILON) break;
            val tNext = t - (xEstimate - x) / dx;
            if (abs(tNext - t) < EPSILON) break;
            t = tNext;
        }
        return Math.clamp(t, 0f, 1f);
    }
}
