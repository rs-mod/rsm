package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

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
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.radius = radius;
        this.colour = colour;
        this.slices = slices;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderCircle(
                stack.last(),
                buffer,
                this.pos,
                this.radius,
                this.colour,
                this.slices
        );
    }
}