package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

@SuppressWarnings("unused")
public class OutlineBox extends RenderTask {
    private final AABB aabb;
    private final int color;
    private final float width;

    public OutlineBox(AABB aabb, int color, boolean depth) {
        this(aabb, color, depth, 3f);
    }

    public OutlineBox(AABB aabb, int color, boolean depth, float width) {
        super(RenderType.LINE, depth);
        this.aabb = aabb;
        this.color = color;
        this.width = width;
    }

    public OutlineBox(BlockPos bp, int color, boolean depth) {
        this(new AABB(bp), color, depth);
    }

    public OutlineBox(AABB aabb, Color color, boolean depth) {
        this(aabb, color, depth, 3f);
    }

    public OutlineBox(AABB aabb, Color color, boolean depth, float width) {
        this(aabb, color.getARGB(), depth, width);
    }

    public OutlineBox(BlockPos bp, Color color, boolean depth) {
        this(new AABB(bp), color, depth);
    }


    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        VertexRenderer.renderOutlineBox(
                stack.last(),
                buffer,
                this.aabb,
                this.color,
                this.width
        );
    }
}