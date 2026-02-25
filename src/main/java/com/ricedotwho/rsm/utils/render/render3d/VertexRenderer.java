package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
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
    private final List<Pair<Integer, Integer>> edges = List.of(
            new Pair<>(0, 1), new Pair<>(1, 5),
            new Pair<>(5, 4), new Pair<>(4, 0),
            new Pair<>(3, 2), new Pair<>(2, 6),
            new Pair<>(6, 7), new Pair<>(7, 3),
            new Pair<>(0, 3), new Pair<>(1, 2),
            new Pair<>(5, 6), new Pair<>(4, 7)
    );

    private static final Int2ObjectMap<CircleData> CACHE = new Int2ObjectOpenHashMap<>();

    private static CircleData getCircle(int slices) {
        return CACHE.computeIfAbsent(slices, CircleData::new);
    }



    public void renderLine(PoseStack.Pose pose, VertexConsumer buffer, Vec3 start, Vec3 direction, Colour startColor, Colour endColor) {
        float endX = (float) (start.x() + direction.x());
        float endY = (float) (start.y() + direction.y());
        float endZ = (float) (start.z() + direction.z());
        float nx = (float) direction.x();
        float ny = (float) direction.y();
        float nz = (float) direction.z();
        buffer.addVertex(pose, (float) start.x(), (float) start.y(), (float) start.z()).setColor(startColor.getRGB()).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, endX, endY, endZ).setColor(endColor.getRGB()).setNormal(pose, nx, ny, nz);
    }

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Colour colour) {
        List<Float> corners = getCorners(aabb);

        for (Pair<Integer, Integer> pair : edges) {
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
            buffer.addVertex(pose, x0, y0, z0).setColor(colour.getRGB()).setNormal(pose, dx, dy, dz);
            buffer.addVertex(pose, x1, y1, z1).setColor(colour.getRGB()).setNormal(pose, dx, dy, dz);
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

    public void addFilledBoxVertices(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Colour colour) {
        Matrix4f matrix = pose.pose();
        int col = colour.getRGB();
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;
        buffer.addVertex(matrix, minX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(col);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(col);
    }

    public void renderCircle(PoseStack.Pose pose, VertexConsumer buffer, Vec3 pos, float radius, Colour colour, int slices) {
        if (slices >= 3) {
            pose.translate((float)pos.x(), (float)pos.y(), (float)pos.z());
            circle(pose, buffer, radius, 0f, colour.getAlphaFloat(), colour.getRedFloat(), colour.getGreenFloat(), colour.getBlueFloat(), slices);
            pose.translate((float)(-pos.x()), (float)(-pos.y()), (float)(-pos.z()));
        }
    }

    /// draw a circle with no translations
    public void circle(PoseStack.Pose pose, VertexConsumer buffer, float radius, float yOffset, float alpha, float red, float green, float blue, int slices) {
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

            buffer.addVertex(matrix, x1, yOffset, z1).setColor(red, green, blue, alpha).setNormal(nx, normalY, nz);
            buffer.addVertex(matrix, x2, yOffset, z2).setColor(red, green, blue, alpha).setNormal(nx, normalY, nz);
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
}
