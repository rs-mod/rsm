package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import lombok.Getter;
import net.minecraft.world.phys.AABB;

public class OutlineBox extends RenderTask {
    private final AABB aabb;
    private final Colour colour;

    public OutlineBox(AABB aabb, Colour colour, boolean depth) {
        super(RenderType.LINE, depth);
        this.aabb = aabb;
        this.colour = colour;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderOutlineBox(
                stack.last(),
                buffer,
                this.aabb,
                this.colour
        );
    }
}