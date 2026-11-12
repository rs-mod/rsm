package com.ricedotwho.rsm.render.render2d;

public record Corners(float topLeft, float topRight, float bottomLeft, float bottomRight) {
    public Corners(float radius) {
        this(radius, radius, radius, radius);
    }

    public static Corners NONE = new Corners(0F);

    public static Corners top(float radius) {
        return new Corners(radius, radius, 0f, 0f);
    }

    public static Corners bottom(float radius) {
        return new Corners(0f, 0f, radius, radius);
    }
}
