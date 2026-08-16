package com.ricedotwho.rsm.render.render3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

@UtilityClass
public final class VertexRenderer {
    private final List<Pair<Integer, Integer>> squareEdges  = List.of(
            new Pair<>(0, 1), new Pair<>(1, 5),
            new Pair<>(5, 4), new Pair<>(4, 0),
            new Pair<>(3, 2), new Pair<>(2, 6),
            new Pair<>(6, 7), new Pair<>(7, 3),
            new Pair<>(0, 3), new Pair<>(1, 2),
            new Pair<>(5, 6), new Pair<>(4, 7)
    );

    private final int[][] rectEdges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}
    };

    private static final Int2ObjectMap<CircleData> CACHE = new Int2ObjectOpenHashMap<>();

    private static CircleData getCircle(int slices) {
        return CACHE.computeIfAbsent(slices, CircleData::new);
    }

    public void renderLine(PoseStack.Pose pose, VertexConsumer buffer, Vec3 start, Vec3 direction, Color startColor, Color endColor, float lineWidth) {
        renderLine(pose, buffer, start, direction, startColor.getARGB(), endColor.getARGB(), lineWidth);
    }

    public void renderLine(PoseStack.Pose pose, VertexConsumer buffer, Vec3 start, Vec3 direction, int startColor, int endColor, float lineWidth) {
        float endX = (float) (start.x() + direction.x());
        float endY = (float) (start.y() + direction.y());
        float endZ = (float) (start.z() + direction.z());
        float nx = (float) direction.x();
        float ny = (float) direction.y();
        float nz = (float) direction.z();
        buffer.addVertex(pose, (float) start.x(), (float) start.y(), (float) start.z()).setColor(startColor).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
        buffer.addVertex(pose, endX, endY, endZ).setColor(endColor).setNormal(pose, nx, ny, nz).setLineWidth(lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color, float lineWidth) {
        renderOutlineBox(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, color.getARGB(), lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float lineWidth) {
        renderOutlineBox(pose, buffer, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, color, lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color color, float lineWidth) {
        renderOutlineBox(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, color.getARGB(), lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, float lineWidth) {
        renderOutlineBox(pose, buffer, getCorners(minX, minY, minZ, maxX, maxY, maxZ), color, lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Color color, float lineWidth) {
        renderOutlineBox(pose, buffer, aabb, color.getARGB(), lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, int color, float lineWidth) {
        renderOutlineBox(pose, buffer, getCorners(aabb), color, lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, List<Float> corners, Color color, float lineWidth) {
        renderOutlineBox(pose, buffer, corners, color.getARGB(), lineWidth);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, List<Float> corners, int color, float lineWidth) {
        for (Pair<Integer, Integer> pair : squareEdges ) {
            int i0 = pair.getFirst() * 3;
            int i1 = pair.getSecond() * 3;
            float x0 = corners.get(i0);
            float y0 = corners.get(i0 + 1);
            float z0 = corners.get(i0 + 2);
            float x1 = corners.get(i1);
            float y1 = corners.get(i1 + 1);
            float z1 = corners.get(i1 + 2);
            float dx = x1 - x0;
            float dy = y1 - y0;
            float dz = z1 - z0;
            buffer.addVertex(pose, x0, y0, z0).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
            buffer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
        }
    }

    private List<Float> getCorners(AABB aabb) {
        float x0 = (float) aabb.minX;
        float y0 = (float) aabb.minY;
        float z0 = (float) aabb.minZ;
        float x1 = (float) aabb.maxX;
        float y1 = (float) aabb.maxY;
        float z1 = (float) aabb.maxZ;
        return List.of(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
    }

    private List<Float> getCorners(float x0, float y0, float z0, float x1, float y1, float z1) {
        return List.of(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Color color) {
        addFilledBoxVertices(pose, buffer, aabb, color.getARGB());
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, int color) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;
        addFilledBoxVertices(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, color);
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color) {
        addFilledBoxVertices(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, color.getARGB());
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color) {
        addFilledBoxVertices(pose, buffer, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, color);
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color color) {
        addFilledBoxVertices(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, color.getARGB());
    }

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        Matrix4f matrix = pose.pose();

        buffer.addVertex(matrix, minX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(color);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(color);
    }

    public void renderHorizontalRect(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, float width, Color color) {
        renderHorizontalRect(pose, buffer, aabb, width, color.getARGB());
    }

    public void renderHorizontalRect(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, float width, int color) {
        float x0 = (float) aabb.minX;
        float z0 = (float) aabb.minZ;
        float x1 = (float) aabb.maxX;
        float z1 = (float) aabb.maxZ;
        float y = (float) aabb.minY;
        float[] corners = {
                x0, y, z0,
                x1, y, z0,
                x1, y, z1,
                x0, y, z1
        };
        for (int[] e : rectEdges) {
            int i0 = e[0] * 3;
            int i1 = e[1] * 3;
            float xA = corners[i0];
            float yA = corners[i0 + 1];
            float zA = corners[i0 + 2];
            float xB = corners[i1];
            float yB = corners[i1 + 1];
            float zB = corners[i1 + 2];
            float dx = xB - xA;
            float dy = yB - yA;
            float dz = zB - zA;

            buffer.addVertex(pose, xA, yA, zA).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(width);
            buffer.addVertex(pose, xB, yB, zB).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(width);
        }
    }

    public void renderCircle(PoseStack.Pose pose, VertexConsumer buffer, Vec3 pos, float radius, Color color, int slices, float lineWidth) {
        renderCircle(pose, buffer, pos, radius, color.getARGB(), slices, lineWidth);
    }

    public void renderCircle(PoseStack.Pose pose, VertexConsumer buffer, Vec3 pos, float radius, int color, int slices, float lineWidth) {
        if (slices >= 3) {
            pose.translate((float)pos.x(), (float)pos.y(), (float)pos.z());
            float alpha = ((color >> 24) & 0xFF) / 255f;
            float red = ((color >> 16) & 0xFF) / 255f;
            float green = ((color >> 8) & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;
            circle(pose, buffer, radius, 0f, alpha, red, green, blue, slices, lineWidth);
            pose.translate((float)(-pos.x()), (float)(-pos.y()), (float)(-pos.z()));
        }
    }

    /// draw a circle with no translations
    public void circle(PoseStack.Pose pose, VertexConsumer buffer, float radius, float yOffset, float alpha, float red, float green, float blue, int slices, float lineWidth) {
        Matrix4f matrix = pose.pose();
        CircleData cache = getCircle(slices);

        float normalY = yOffset == 0.0F ? 0.0F : (yOffset > 0.0F ? 1.0F : -1.0F);

        for (int i = 0; i < slices; i++) {
            int next = (i + 1) % slices;

            float x1 = cache.x[i] * radius;
            float z1 = cache.z[i] * radius;
            float x2 = cache.x[next] * radius;
            float z2 = cache.z[next] * radius;

            float nx = cache.nx[i];
            float nz = cache.nz[i];

            buffer.addVertex(matrix, x1, yOffset, z1).setColor(red, green, blue, alpha).setNormal(nx, normalY, nz).setLineWidth(lineWidth);
            buffer.addVertex(matrix, x2, yOffset, z2).setColor(red, green, blue, alpha).setNormal(nx, normalY, nz).setLineWidth(lineWidth);
        }
    }

    public final class CircleData {
        public final float[] x;
        public final float[] z;
        public final float[] nx;
        public final float[] nz;

        public CircleData(int slices) {
            this.x = new float[slices];
            this.z = new float[slices];
            this.nx = new float[slices];
            this.nz = new float[slices];

            float step = (float) (Math.PI * 2.0) / slices;

            for (int i = 0; i < slices; i++) {
                float angle = i * step;
                x[i] = Mth.cos(angle);
                z[i] = Mth.sin(angle);
            }

            for (int i = 0; i < slices; i++) {
                int next = (i + 1) % slices;
                nx[i] = x[next] - x[i];
                nz[i] = z[next] - z[i];
            }
        }
    }

    public void renderRing(PoseStack.Pose pose, VertexConsumer buffer, Vec3 pos, float radius, Color color, int slices, int layers) {
        renderRing(pose, buffer, pos, radius, color.getARGB(), slices, layers);
    }

    public void renderRing(PoseStack.Pose pose, VertexConsumer buffer, Vec3 pos, float radius, int color, int slices, int layers) {
        if (slices >= 3) {
            pose.translate((float)pos.x(), (float)pos.y(), (float)pos.z());

            double h = (radius * 2) / 3.0;
            float oneOverLayers = 1.0f / layers;

            float red = ((color >> 16) & 0xFF) / 255f;
            float green = ((color >> 8) & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;

            for (int i = 0; i < layers; i++) {
                float yOffset = (float) ((h * i) / (float) layers);

                float t = 1.0f - (i * oneOverLayers);
                float alpha = t * t * t;
                if (alpha < 0.01f) continue;
                VertexRenderer.circle(pose, buffer, radius, yOffset, alpha, red, green, blue, slices, 3f);
            }

            pose.translate((float)(-pos.x()), (float)(-pos.y()), (float)(-pos.z()));
        }
    }
}