package com.ricedotwho.rsm.render.render2d;

import org.joml.Matrix3x2fc;

import java.util.List;

/**
 * CPU-side geometry helpers for the GUI renderer.
 *
 * <p>All generated coordinates are in the mod's local coordinate space. The recorder bakes the
 * active transform into the produced vertices, so these helpers only deal with plain points.</p>
 */
final class GuiGeometry {
    private GuiGeometry() {
    }

    record Point(float x, float y) {
    }

    static float transformX(Matrix3x2fc matrix, float x, float y) {
        return matrix.m00() * x + matrix.m10() * y + matrix.m20();
    }

    static float transformY(Matrix3x2fc matrix, float x, float y) {
        return matrix.m01() * x + matrix.m11() * y + matrix.m21();
    }

    /**
     * Appends the outline of a rounded rectangle, going clockwise.
     *
     * <p>A fixed number of points is emitted for every corner. This keeps the point counts of the
     * outer and inner outlines in sync, which makes building outline rings trivial. Corners with a
     * radius of zero simply emit coincident points.</p>
     */
    static void roundedRect(List<Point> out, float x, float y, float w, float h, float[] radii, int segmentsPerCorner) {
        if (w < 0f) {
            x += w;
            w = -w;
        }
        if (h < 0f) {
            y += h;
            h = -h;
        }
        if (w <= 0f || h <= 0f) {

            return;
        }

        float maxRadius = Math.min(w, h) * 0.5f;
        float tl = radii == null ? 0f : Math.clamp(radii[0], 0f, maxRadius);
        float tr = radii == null ? 0f : Math.clamp(radii[1], 0f, maxRadius);
        float br = radii == null ? 0f : Math.clamp(radii[2], 0f, maxRadius);
        float bl = radii == null ? 0f : Math.clamp(radii[3], 0f, maxRadius);

        int segments = Math.max(1, segmentsPerCorner);

        // top-left, from left-middle to top-middle
        arc(out, x + tl, y + tl, tl, (float) Math.PI, (float) (Math.PI * 1.5), segments);
        // top-right, from top-middle to right-middle
        arc(out, x + w - tr, y + tr, tr, (float) (Math.PI * 1.5), (float) (Math.PI * 2), segments);
        // bottom-right, from right-middle to bottom-middle
        arc(out, x + w - br, y + h - br, br, 0f, (float) (Math.PI * 0.5), segments);
        // bottom-left, from bottom-middle to left-middle
        arc(out, x + bl, y + h - bl, bl, (float) (Math.PI * 0.5), (float) Math.PI, segments);
    }

    static void circle(List<Point> out, float centerX, float centerY, float radius, int segments) {
        if (radius <= 0f) return;
        int count = Math.max(6, segments);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / count;
            out.add(new Point(
                    centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius
            ));
        }
    }

    static void arc(List<Point> out, float centerX, float centerY, float radius, float startAngle, float endAngle, int segments) {
        int count = Math.max(1, segments);
        if (radius <= 0.0001f) {
            for (int i = 0; i <= count; i++) {
                out.add(new Point(centerX, centerY));
            }
            return;
        }

        for (int i = 0; i <= count; i++) {
            double angle = startAngle + (endAngle - startAngle) * ((double) i / count);
            out.add(new Point(
                    centerX + (float) Math.cos(angle) * radius,
                    centerY + (float) Math.sin(angle) * radius
            ));
        }
    }

    static int circleSegments(float radius) {
        return Math.clamp((int) Math.ceil(radius * 1.5f), 8, 64);
    }

    static int cornerSegments(float radius) {
        return Math.clamp((int) Math.ceil(radius * 0.75f), 2, 12);
    }

    static boolean samePoint(Point a, Point b) {
        return Math.abs(a.x - b.x) < 0.0001f && Math.abs(a.y - b.y) < 0.0001f;
    }
}
