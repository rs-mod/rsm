package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.world.phys.Vec3;

public class Line extends RenderTask {
    private final Vec3 from;
    private final Vec3 to;
    private final Colour start;
    private final Colour end;

    public Line(Vec3 from, Vec3 to, Colour start, Colour end, boolean depth) {
        super(RenderType.LINE, depth);
        this.from = from;
        this.to = to;
        this.start = start;
        this.end = end;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderLine(
                stack.last(),
                buffer,
                this.from,
                this.to.subtract(this.from),
                this.start,
                this.end
        );
    }
}