package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

@SuppressWarnings("unused")
public class FilledOutlineBox extends RenderTask {
    private final AABB aabb;
    private final int fill;
    private final int line;
    private final float width;

    public FilledOutlineBox(AABB aabb, Color fill, Color line, boolean depth) {
        this(aabb, fill, line, depth, 3f);
    }

    public FilledOutlineBox(AABB aabb, int fill, int line, boolean depth) {
        this(aabb, fill, line, depth, 3f);
    }

    public FilledOutlineBox(BlockPos bp, Color fill, Color line, boolean depth) {
        this(bp, fill, line, depth, 3f);
    }

    public FilledOutlineBox(BlockPos bp, int fill, int line, boolean depth) {
        this(bp, fill, line, depth, 3f);
    }

    public FilledOutlineBox(AABB aabb, Color fill, Color line, boolean depth, float width) {
        this(aabb, fill.getARGB(), line.getARGB(), depth, width);
    }

    public FilledOutlineBox(AABB aabb, int fill, int line, boolean depth, float width) {
        super(RenderType.FILLED_OUTLINE, depth);
        this.aabb = aabb;
        this.fill = fill;
        this.line = line;
        this.width = width;
    }

    public FilledOutlineBox(BlockPos pos, Color fill, Color line, boolean depth, float width) {
        this(pos, fill.getARGB(), line.getARGB(), depth, width);
    }

    public FilledOutlineBox(BlockPos pos, int fill, int line, boolean depth, float width) {
        super(RenderType.FILLED_OUTLINE, depth);
        this.aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
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