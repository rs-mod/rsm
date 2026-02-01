package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pair;
import lombok.experimental.UtilityClass;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.phys.AABB;

import java.util.List;

@UtilityClass
public class VertexRenderer {

    // ts from odin

    private final List<Pair<Integer, Integer>> edges = List.of(
            new Pair<>(0, 1), new Pair<>(1, 5), new Pair<>(5, 4), new Pair<>(4, 0),
            new Pair<>(3, 2), new Pair<>(2, 6),new Pair<>(6, 7), new Pair<>(7, 3),
            new Pair<>(0, 3), new Pair<>(1, 2), new Pair<>(5, 6), new Pair<>(4, 7)
    );

    public void renderOutlineBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Colour colour) {
        List<Float> corners = getCorners(aabb);
        for (Pair<Integer, Integer> pair : edges) {
            int i0 = pair.getFirst()  * 3;
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

            buffer.addVertex(pose, x0, y0, z0)
                    .setColor(colour.getRed(), colour.getBlue(), colour.getGreen(), colour.getAlpha())
                    .setNormal(pose, dx, dy, dz);
            buffer.addVertex(pose, x1, y1, z1)
                    .setColor(colour.getRed(), colour.getBlue(), colour.getGreen(), colour.getAlpha())
                    .setNormal(pose, dx, dy, dz);
        }
    }
    private List<Float> getCorners(AABB aabb) {
        float x0 = (float) aabb.minX;
        float y0 = (float) aabb.minY;
        float z0 = (float) aabb.minZ;

        float x1 = (float) aabb.maxX;
        float y1 = (float) aabb.maxY;
        float z1 = (float) aabb.maxZ;

        return List.of(
                x0, y0, z0,
                x1, y0, z0,
                x1, y1, z0,
                x0, y1, z0,
                x0, y0, z1,
                x1, y0, z1,
                x1, y1, z1,
                x0, y1, z1
        );
    }
}
