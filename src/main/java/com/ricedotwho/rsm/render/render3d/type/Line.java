package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class Line extends RenderTask {
    private final Vec3 from;
    private final Vec3 to;
    private final int start;
    private final int end;
    private final float width;

    public Line(Vec3 from, Vec3 to, int start, int end, boolean depth) {
        this(from, to, start, end, depth, 3f);
    }

    public Line(Vec3 from, Vec3 to, int start, int end, boolean depth, float width) {
        super(RenderType.LINE, depth);
        this.from = from;
        this.to = to;
        this.start = start;
        this.end = end;
        this.width = width;
    }


    public Line(Vec3 from, Vec3 to, Color start, Color end, boolean depth) {
        this(from, to, start.getARGB(), end.getARGB(), depth, 3f);
    }

    public Line(Vec3 from, Vec3 to, Color start, Color end, boolean depth, float width) {
        this(from, to, start.getARGB(), end.getARGB(), depth, width);
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderLine(
                stack.last(),
                buffer,
                this.from,
                this.to.subtract(this.from),
                this.start,
                this.end,
                width
        );
    }
}