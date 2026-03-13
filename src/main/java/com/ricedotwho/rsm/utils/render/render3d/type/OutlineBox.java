package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.world.phys.AABB;

public class OutlineBox extends RenderTask {
    private final AABB aabb;
    private final Colour colour;
    private final float width;

    public OutlineBox(AABB aabb, Colour colour, boolean depth) {
        this(aabb, colour, depth, 3f);
    }

    public OutlineBox(AABB aabb, Colour colour, boolean depth, float width) {
        super(RenderType.LINE, depth);
        this.aabb = aabb;
        this.colour = colour;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderOutlineBox(
                stack.last(),
                buffer,
                this.aabb,
                this.colour,
                this.width
        );
    }
}