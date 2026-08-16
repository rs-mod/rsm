package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.render.render3d.VertexRenderer;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("unused")
public class OutlineShape extends RenderTask {
    private final BlockPos pos;
    private final VoxelShape shape;
    private final int color;
    private final float width;

    public OutlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth) {
        this(pos, shape, color, depth, 3f);
    }

    public OutlineShape(BlockPos pos, VoxelShape shape, int color, boolean depth, float width) {
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.shape = shape;
        this.color = color;
        this.width = width;
    }


    public OutlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth) {
        this(pos, shape, color.getARGB(), depth);
    }

    public OutlineShape(BlockPos pos, VoxelShape shape, Color color, boolean depth, float width) {
        this(pos, shape, color.getARGB(), depth, width);
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> VertexRenderer.renderOutlineBox(
                stack.last(),
                buffer,
                pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX,pos.getY() + maxY, pos.getZ() + maxZ,
                this.color,
                this.width
        ));
    }
}