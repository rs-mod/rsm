package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.world.phys.AABB;

public class Rectangle extends RenderTask {
    private final AABB aabb;
    private final float lineWidth;
    private final Colour colour;

    public Rectangle(AABB aabb, Colour colour, boolean depth) {
        this(aabb, colour, 3f, depth);
    }

    public Rectangle(AABB aabb, Colour colour, float lineWidth, boolean depth) {
        super(RenderType.LINE, depth);
        this.aabb = aabb;
        this.lineWidth = lineWidth;
        this.colour = colour;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderHorizontalRect(
                stack.last(),
                buffer,
                this.aabb,
                this.lineWidth,
                this.colour
        );
    }
}
