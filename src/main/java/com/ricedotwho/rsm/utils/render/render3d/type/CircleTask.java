package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;

@Getter
public class CircleTask extends RenderTask {
    private final Pos pos;
    private final float radius;
    private final Colour colour;
    private final int slices;

    public CircleTask(Pos pos, boolean depth, float radius, Colour colour, int slices) {
        super(RenderType.CIRCLE, depth);
        this.pos = pos;
        this.radius = radius;
        this.colour = colour;
        this.slices = slices;
    }
}