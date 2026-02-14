package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.world.phys.AABB;

public class FilledBox extends RenderTask {
    private final AABB aabb;
    private final Colour colour;

    public FilledBox(AABB aabb, Colour colour, boolean depth) {
        super(RenderType.FILLED, depth);
        this.aabb = aabb;
        this.colour = colour;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.addFilledBoxVertices(
                stack.last(),
                buffer,
                this.aabb,
                this.colour
        );
    }
}