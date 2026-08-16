package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.world.phys.Vec3;

public class Circle extends RenderTask {
    private final Vec3 pos;
    private final float radius;
    private final Color color;
    private final int slices;
    private final float width;

    public Circle(Vec3 pos, boolean depth, float radius, Color color, int slices) {
        this(pos, depth, radius, color, slices, 3f);
    }

    public Circle(Vec3 pos, boolean depth, float radius, Color color, int slices, float width) {
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.radius = radius;
        this.color = color;
        this.slices = slices;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderCircle(
                stack.last(),
                buffer,
                this.pos,
                this.radius,
                this.color,
                this.slices,
                width
        );
    }
}