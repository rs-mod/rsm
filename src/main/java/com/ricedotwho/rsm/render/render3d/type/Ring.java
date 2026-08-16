package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class Ring extends RenderTask {
    private final Vec3 pos;
    private final float radius;
    private final int color;
    private final int slices;
    private final int layers;

    public Ring(Vec3 pos, boolean depth, float radius, int color) {
        this(pos, depth, radius, color, 64, 16);
    }

    public Ring(Vec3 pos, boolean depth, float radius, int color, int slices, int layers) {
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.radius = radius;
        this.color = color;
        this.slices = slices;
        this.layers = layers;
    }

    public Ring(Vec3 pos, boolean depth, float radius, Color color) {
        this(pos, depth, radius, color, 64, 16);
    }

    public Ring(Vec3 pos, boolean depth, float radius, Color color, int slices, int layers) {
        this(pos, depth, radius, color.getARGB(), slices, layers);
    }

    private int getFactor() {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        assert camera != null;
        double dist = camera.distanceToSqr(pos);
        if (dist > 64 * 64) {
            return 0;
        } else if (dist > 48 * 48) {
            return 8;
        } else if (dist > 32 * 32) {
            return 4;
        } else if (dist > 16 *16) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        int factor = getFactor();
        if (factor == 0) return;
        int slices = this.slices / factor;
        int layers = this.layers / factor;
        VertexRenderer.renderRing(
                stack.last(),
                buffer,
                this.pos,
                this.radius,
                this.color,
                slices,
                layers
        );
    }
}