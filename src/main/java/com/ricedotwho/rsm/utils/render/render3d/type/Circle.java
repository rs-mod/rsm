package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
public class Circle extends RenderTask {
    private final Vec3 pos;
    private final float radius;
    private final Colour colour;
    private final int slices;

    @Deprecated
    public Circle(Pos pos, boolean depth, float radius, Colour colour, int slices) {
        this(pos.asVec3(), depth, radius, colour, slices);
    }

    public Circle(Vec3 pos, boolean depth, float radius, Colour colour, int slices) {
        super(RenderType.CIRCLE, depth);
        this.pos = pos;
        this.radius = radius;
        this.colour = colour;
        this.slices = slices;
    }
}