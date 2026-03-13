package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.VertexRenderer;
import net.minecraft.world.phys.AABB;

public class FilledOutlineBox extends RenderTask {
    private final AABB aabb;
    private final Colour fill;
    private final Colour line;
    private final float width;

    public FilledOutlineBox(AABB aabb, Colour fill, Colour line, boolean depth) {
        this(aabb, fill, line, depth, 3f);
    }

    public FilledOutlineBox(AABB aabb, Colour fill, Colour line, boolean depth, float width) {
        super(RenderType.FILLED_OUTLINE, depth);
        this.aabb = aabb;
        this.fill = fill;
        this.line = line;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        if (source.equals(RenderType.LINE)) {
            VertexRenderer.renderOutlineBox(
                    stack.last(),
                    buffer,
                    this.aabb,
                    this.line,
                    this.width
            );
        } else {
            VertexRenderer.addFilledBoxVertices(
                    stack.last(),
                    buffer,
                    this.aabb,
                    this.fill
            );
        }
    }
}